package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.entities.tdes.TripleDesCbcMode
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.tdes.Pkcs5Padding
import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesIv
import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesKey
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.GeneralSecurityException
import java.security.SecureRandom

/**
 * Domain service for Triple DES (3DES) encryption algorithm.
 * Supports 3DES-112 and 3DES-168.
 *
 * Uses custom implementation of 3DES following the standard specification.
 * 3DES-112 uses two keys (K1, K2, K1), 3DES-168 uses three keys (K1, K2, K3).
 */
class TripleDesEncryptionService : DomainService() {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val IV_LENGTH = 8 // bytes (64 bits, same as DES block size)
    }

    /**
     * Encrypts data using the provided 3DES key.
     *
     * @param data Data to encrypt
     * @param key 3DES encryption key
     * @return Result with encrypted data and IV, or error
     */
    fun encrypt(data: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return try {
            logger.debug { "Starting 3DES encryption: dataSize=${data.size} bytes, algorithm=${key.algorithm}" }

            val keyMaterial = buildKeyMaterial(key)

            // Generate random IV
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val ivValue = TripleDesIv.create(iv).getOrThrow()

            // Pad data before encryption
            val paddedData = Pkcs5Padding.pad(data)

            // Encrypt using CBC mode
            val encryptedData = TripleDesCbcMode.encrypt(paddedData, keyMaterial, ivValue)

            logger.debug {
                "3DES encryption successful: " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "algorithm=${key.algorithm}"
            }

            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = key.algorithm,
                    initializationVector = ivValue.toByteArray(),
                ),
            )
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "Encryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: DomainError) {
            logger.error(e) { "Encryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(e)
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Encryption failed: security error" }
            Result.failure(DomainError("Encryption failed: ${e.message}", e))
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Encryption failed: invalid argument" }
            Result.failure(DomainError("Encryption failed: ${e.message}", e))
        }
    }

    /**
     * Decrypts data using the provided 3DES key and IV.
     *
     * @param encryptedText Encrypted data with IV
     * @param key 3DES encryption key
     * @return Result with decrypted data or error
     */
    fun decrypt(encryptedText: EncryptedText, key: EncryptionKey): Result<ByteArray> {
        return try {
            val keyMaterial = buildKeyMaterial(key)
            val iv = requireIv(encryptedText)
            val encryptedData = encryptedText.encryptedData
            requireEncryptedDataLength(encryptedData)

            logger.debug {
                "Starting 3DES decryption: " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "algorithm=${key.algorithm}"
            }

            // Decrypt using CBC mode
            val decryptedPadded = TripleDesCbcMode.decrypt(encryptedData, keyMaterial, iv)

            // Remove padding
            val decryptedData = Pkcs5Padding.unpad(decryptedPadded)

            logger.debug {
                "3DES decryption successful: " +
                    "decryptedSize=${decryptedData.size} bytes, " +
                    "algorithm=${key.algorithm}"
            }

            Result.success(decryptedData)
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "Decryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: DomainError) {
            logger.error(e) { "Decryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(e)
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Decryption failed: security error" }
            Result.failure(DomainError("Decryption failed: ${e.message}", e))
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Decryption failed: invalid argument" }
            Result.failure(DomainError("Decryption failed: ${e.message}", e))
        }
    }

    /**
     * Generates a new 3DES encryption key.
     *
     * @param algorithm 3DES algorithm (TDES_112 or TDES_168)
     * @return Result with generated key or error
     */
    fun generateKey(algorithm: EncryptionAlgorithm): Result<EncryptionKey> {
        return try {
            ensureAlgorithmSupported(algorithm)
            val keySize = TripleDesKey.expectedKeyLength(algorithm)
            logger.debug { "Generating 3DES key: algorithm=$algorithm, keySize=$keySize bytes" }

            val keyBytes = generateRandomKey(keySize)
            logger.debug {
                "Key generated successfully: algorithm=$algorithm, keyLength=${keyBytes.size} bytes"
            }

            Result.success(
                EncryptionKey(
                    value = keyBytes,
                    algorithm = algorithm,
                ),
            )
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "Key generation failed: unsupported algorithm=$algorithm" }
            Result.failure(e)
        } catch (e: DomainError) {
            logger.error(e) { "Key generation failed: algorithm=$algorithm, error=${e.message}" }
            Result.failure(e)
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Key generation failed: security error" }
            Result.failure(DomainError("Key generation failed: ${e.message}", e))
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Key generation failed: invalid argument" }
            Result.failure(DomainError("Key generation failed: ${e.message}", e))
        }
    }

    private fun generateRandomKey(keySize: Int): ByteArray {
        val keyBytes = ByteArray(keySize)
        SecureRandom().nextBytes(keyBytes)
        return keyBytes
    }

    private fun ensureAlgorithmSupported(algorithm: EncryptionAlgorithm) {
        if (algorithm != EncryptionAlgorithm.TDES_112 && algorithm != EncryptionAlgorithm.TDES_168) {
            throw UnsupportedAlgorithmError(algorithm, "TripleDesEncryptionService")
        }
    }

    private fun buildKeyMaterial(key: EncryptionKey): TripleDesKey {
        ensureAlgorithmSupported(key.algorithm)
        return TripleDesKey.create(key.algorithm, key.value).getOrElse { error ->
            throw error
        }
    }

    private fun requireIv(encryptedText: EncryptedText): TripleDesIv {
        return TripleDesIv.create(encryptedText.initializationVector).getOrElse { error ->
            logger.warn { "Decryption failed: ${error.message}" }
            throw error
        }
    }

    private fun requireEncryptedDataLength(encryptedData: ByteArray) {
        if (encryptedData.size < IV_LENGTH) {
            logger.warn { "Decryption failed: encrypted data too short, size=${encryptedData.size}" }
            throw DomainError("Encrypted data is too short. Minimum size is $IV_LENGTH bytes")
        }
    }
}
