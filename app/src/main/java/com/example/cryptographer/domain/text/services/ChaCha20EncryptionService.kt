package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Domain service for ChaCha20 encryption algorithm.
 * Supports ChaCha20-256 with 96-bit nonce.
 *
 * Note: ChaCha20 is available in Java 11+ and Android API 28+.
 * For older Android versions, consider using a library like BouncyCastle.
 */
class ChaCha20EncryptionService : DomainService() {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val ALGORITHM = "ChaCha20"
        private const val NONCE_LENGTH = 12 // bytes (96 bits) for ChaCha20-Poly1305
        private const val KEY_LENGTH = 32 // bytes (256 bits)
    }

    /**
     * Encrypts data using the provided ChaCha20 key.
     *
     * @param data Data to encrypt
     * @param key ChaCha20 encryption key
     * @return Result with encrypted data or error
     */
    fun encrypt(data: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return try {
            validateKey(key)

            logger.debug { "Starting ChaCha20 encryption: algorithm=${key.algorithm}, dataSize=${data.size} bytes" }
            val secretKey = SecretKeySpec(key.value, ALGORITHM)

            // Use ChaCha20-Poly1305 transformation (available in Java 11+ and Android API 28+)
            // For older versions, this will throw an exception which will be caught below
            val transformation = "$ALGORITHM-Poly1305/None/NoPadding"
            val cipher = Cipher.getInstance(transformation)

            // Generate random nonce (96 bits = 12 bytes)
            // For ChaCha20-Poly1305, we use IvParameterSpec with the nonce
            val nonce = ByteArray(NONCE_LENGTH)
            SecureRandom().nextBytes(nonce)

            val parameterSpec = IvParameterSpec(nonce)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val encryptedData = cipher.doFinal(data)

            logger.debug {
                "ChaCha20 encryption successful: algorithm=${key.algorithm}, " +
                    "encryptedSize=${encryptedData.size} bytes"
            }
            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = key.algorithm,
                    initializationVector = nonce,
                ),
            )
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "ChaCha20 encryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: NoSuchAlgorithmException) {
            logger.error(e) { "ChaCha20 encryption failed: algorithm not available in this environment" }
            Result.failure(
                DomainError(
                    "ChaCha20-Poly1305 is not available in this Java/Android environment. " +
                        "Requires Java 11+ or Android API 28+",
                    e,
                ),
            )
        } catch (e: DomainError) {
            logger.error(e) { "ChaCha20 encryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Decrypts data using the provided ChaCha20 key.
     *
     * @param encryptedText Encrypted data with nonce
     * @param key ChaCha20 encryption key
     * @return Result with decrypted data or error
     */
    fun decrypt(encryptedText: EncryptedText, key: EncryptionKey): Result<ByteArray> {
        return try {
            validateKey(key)

            if (encryptedText.initializationVector == null) {
                logger.warn { "ChaCha20 decryption failed: nonce is missing" }
                return Result.failure(
                    IllegalArgumentException(
                        "Nonce (Initialization Vector) is missing for ChaCha20 decryption",
                    ),
                )
            }

            logger.debug {
                "Starting ChaCha20 decryption: algorithm=${key.algorithm}, " +
                    "encryptedSize=${encryptedText.encryptedData.size} bytes"
            }
            val secretKey = SecretKeySpec(key.value, ALGORITHM)

            // Use ChaCha20-Poly1305 transformation (available in Java 11+ and Android API 28+)
            val transformation = "$ALGORITHM-Poly1305/None/NoPadding"
            val cipher = Cipher.getInstance(transformation)

            // For ChaCha20-Poly1305, we use IvParameterSpec with the nonce
            val parameterSpec = IvParameterSpec(encryptedText.initializationVector)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            val decryptedData = cipher.doFinal(encryptedText.encryptedData)

            logger.debug {
                "ChaCha20 decryption successful: algorithm=${key.algorithm}," +
                    " decryptedSize=${decryptedData.size} bytes"
            }
            Result.success(decryptedData)
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "ChaCha20 decryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: NoSuchAlgorithmException) {
            logger.error(e) { "ChaCha20 decryption failed: algorithm not available in this environment" }
            Result.failure(
                DomainError(
                    "ChaCha20-Poly1305 is not available in this Java/Android environment." +
                        " Requires Java 11+ or Android API 28+",
                    e,
                ),
            )
        } catch (e: DomainError) {
            logger.error(e) { "ChaCha20 decryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Generates a new ChaCha20 encryption key.
     *
     * @param algorithm ChaCha20 algorithm (CHACHA20_256)
     * @return Result with generated key or error
     */
    fun generateKey(algorithm: EncryptionAlgorithm): Result<EncryptionKey> {
        return try {
            when (algorithm) {
                EncryptionAlgorithm.CHACHA20_256 -> {
                    // Algorithm is supported, continue with generation
                }
                EncryptionAlgorithm.AES_128,
                EncryptionAlgorithm.AES_192,
                EncryptionAlgorithm.AES_256,
                -> throw UnsupportedAlgorithmError(
                    algorithm,
                    "ChaCha20EncryptionService",
                )
            }

            logger.debug { "Generating ChaCha20 encryption key: algorithm=$algorithm, keySize=256 bits" }

            // Generate 256-bit (32-byte) key
            val keyBytes = ByteArray(KEY_LENGTH)
            SecureRandom().nextBytes(keyBytes)

            logger.debug {
                "ChaCha20 key generated successfully: algorithm=$algorithm, keyLength=${keyBytes.size} bytes"
            }
            Result.success(
                EncryptionKey(
                    value = keyBytes,
                    algorithm = algorithm,
                ),
            )
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "ChaCha20 key generation failed: unsupported algorithm=$algorithm" }
            Result.failure(e)
        } catch (e: DomainError) {
            logger.error(e) { "ChaCha20 key generation failed: algorithm=$algorithm, error=${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Validates that the key matches the expected length for ChaCha20-256.
     */
    private fun validateKey(key: EncryptionKey) {
        // Check algorithm is supported
        when (key.algorithm) {
            EncryptionAlgorithm.CHACHA20_256 -> {
                // Algorithm is supported, continue with validation
            }
            EncryptionAlgorithm.AES_128,
            EncryptionAlgorithm.AES_192,
            EncryptionAlgorithm.AES_256,
            -> throw UnsupportedAlgorithmError(
                key.algorithm,
                "ChaCha20EncryptionService",
            )
        }

        // Check key length for ChaCha20-256 (must be 32 bytes = 256 bits)
        if (key.value.size != KEY_LENGTH) {
            throw IllegalArgumentException(
                "Invalid ChaCha20 key length. Expected $KEY_LENGTH bytes (256 bits), got ${key.value.size} bytes",
            )
        }
    }
}
