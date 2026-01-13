package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
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
            TripleDesValidationHelper.validateKey(key)

            logger.debug { "Starting 3DES encryption: dataSize=${data.size} bytes, algorithm=${key.algorithm}" }

            // Generate random IV
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)

            // Extract keys based on algorithm
            val (key1, key2, key3) = TripleDesValidationHelper.extractKeys(key)

            // Pad data before encryption
            val paddedData = TripleDesPaddingHelper.padData(data)

            // Encrypt using CBC mode
            val encryptedData = when (key.algorithm) {
                EncryptionAlgorithm.TDES_112 -> {
                    TripleDesCbcHelper.encryptCbc112(paddedData, key1, key2, iv)
                }

                EncryptionAlgorithm.TDES_168 -> {
                    TripleDesCbcHelper.encryptCbc168(paddedData, key1, key2, key3!!, iv)
                }

                else -> throw UnsupportedAlgorithmError(key.algorithm, "TripleDesEncryptionService")
            }

            logger.debug {
                "3DES encryption successful: " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "algorithm=${key.algorithm}"
            }

            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = key.algorithm,
                    initializationVector = iv,
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
            TripleDesValidationHelper.validateKey(key)
            val validationError = TripleDesValidationHelper.validateDecryptionInput(encryptedText)
            if (validationError != null) {
                return Result.failure(validationError)
            }

            val iv = encryptedText.initializationVector!!
            val encryptedData = encryptedText.encryptedData

            logger.debug {
                "Starting 3DES decryption: " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "algorithm=${key.algorithm}"
            }

            // Extract keys and decrypt
            val (key1, key2, key3) = TripleDesValidationHelper.extractKeys(key)
            val params = TripleDesDecryptionParams(encryptedData, key1, key2, key3, iv, key.algorithm)
            val decryptedPadded = performDecryption(params)

            // Remove padding
            val decryptedData = TripleDesPaddingHelper.removePadding(decryptedPadded)

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

    private fun performDecryption(params: TripleDesDecryptionParams): ByteArray {
        return when (params.algorithm) {
            EncryptionAlgorithm.TDES_112 -> TripleDesCbcHelper.decryptCbc112(
                params.encryptedData,
                params.key1,
                params.key2,
                params.iv,
            )

            EncryptionAlgorithm.TDES_168 -> TripleDesCbcHelper.decryptCbc168(
                params.encryptedData,
                params.key1,
                params.key2,
                params.key3!!,
                params.iv,
            )

            else -> throw UnsupportedAlgorithmError(params.algorithm, "TripleDesEncryptionService")
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
            if (!TripleDesValidationHelper.isAlgorithmSupported(algorithm)) {
                throw UnsupportedAlgorithmError(algorithm, "TripleDesEncryptionService")
            }

            val keySize = TripleDesValidationHelper.getExpectedKeyLength(algorithm)
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
}
