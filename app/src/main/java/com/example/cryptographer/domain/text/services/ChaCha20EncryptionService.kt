package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.services.chacha20.ChaCha20Poly1305
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.SecureRandom

/**
 * Domain service for ChaCha20-Poly1305 encryption algorithm.
 * Supports ChaCha20-256 with 96-bit nonce and Poly1305 authentication.
 *
 * Uses custom implementation of ChaCha20-Poly1305 following RFC 8439.
 * This implementation follows SOLID principles with separate components
 * for ChaCha20 core, stream cipher, Poly1305 MAC, and AEAD scheme.
 */
class ChaCha20EncryptionService : DomainService() {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val NONCE_LENGTH = 12 // bytes (96 bits) for ChaCha20-Poly1305
        private const val KEY_LENGTH = 32 // bytes (256 bits)
        private const val TAG_LENGTH = 16 // bytes (128 bits) for Poly1305 tag
    }

    /**
     * Encrypts data using the provided ChaCha20 key with Poly1305 authentication.
     *
     * @param data Data to encrypt
     * @param key ChaCha20 encryption key
     * @return Result with encrypted data (ciphertext + tag) and nonce, or error
     */
    fun encrypt(data: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return try {
            validateKey(key)

            logger.debug {
                "Starting ChaCha20-Poly1305 encryption: " +
                    "algorithm=${key.algorithm}, " +
                    "dataSize=${data.size} bytes"
            }

            // Generate random nonce (96 bits = 12 bytes)
            val nonce = ByteArray(NONCE_LENGTH)
            SecureRandom().nextBytes(nonce)

            // Create ChaCha20-Poly1305 instance
            val aead = ChaCha20Poly1305(key.value, nonce)

            // Encrypt and compute authentication tag
            val (ciphertext, tag) = aead.encrypt(data)

            // Combine ciphertext and tag (ciphertext || tag)
            val encryptedData = ByteArray(ciphertext.size + tag.size)
            ciphertext.copyInto(encryptedData, 0, 0, ciphertext.size)
            tag.copyInto(encryptedData, ciphertext.size, 0, tag.size)

            logger.debug {
                "ChaCha20-Poly1305 encryption successful: algorithm=${key.algorithm}, " +
                    "encryptedSize=${encryptedData.size} bytes (ciphertext=${ciphertext.size}, tag=${tag.size})"
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
        } catch (e: Exception) {
            logger.error(e) { "ChaCha20 encryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(
                DomainError(
                    "ChaCha20-Poly1305 encryption failed: ${e.message}",
                    e,
                ),
            )
        }
    }

    /**
     * Decrypts data using the provided ChaCha20 key and verifies Poly1305 authentication tag.
     *
     * @param encryptedText Encrypted data (ciphertext || tag) with nonce
     * @param key ChaCha20 encryption key
     * @return Result with decrypted data or error if authentication fails
     */
    fun decrypt(encryptedText: EncryptedText, key: EncryptionKey): Result<ByteArray> {
        return try {
            validateKey(key)

            if (encryptedText.initializationVector == null) {
                logger.warn { "ChaCha20 decryption failed: nonce is missing" }
                return Result.failure(
                    DomainError(
                        "Nonce (Initialization Vector) is missing for ChaCha20 decryption",
                    ),
                )
            }

            val encryptedData = encryptedText.encryptedData
            if (encryptedData.size < TAG_LENGTH) {
                logger.warn { "ChaCha20 decryption failed: encrypted data too short" }
                return Result.failure(
                    DomainError(
                        "Encrypted data is too short. " +
                            "Expected at least $TAG_LENGTH bytes (tag), got ${encryptedData.size} bytes",
                    ),
                )
            }

            logger.debug {
                "Starting ChaCha20-Poly1305 decryption: algorithm=${key.algorithm}, " +
                    "encryptedSize=${encryptedData.size} bytes"
            }

            // Split ciphertext and tag
            val ciphertextSize = encryptedData.size - TAG_LENGTH
            val ciphertext = ByteArray(ciphertextSize)
            val tag = ByteArray(TAG_LENGTH)
            encryptedData.copyInto(ciphertext, 0, 0, ciphertextSize)
            encryptedData.copyInto(
                tag,
                0,
                ciphertextSize,
                encryptedData.size,
            )

            // Create ChaCha20-Poly1305 instance
            val aead = ChaCha20Poly1305(key.value, encryptedText.initializationVector)

            // Decrypt and verify authentication tag
            val decryptedData = aead.decrypt(ciphertext, tag)
            if (decryptedData == null) {
                logger.warn { "ChaCha20 decryption failed: authentication tag verification failed" }
                return Result.failure(
                    DomainError(
                        "ChaCha20-Poly1305 authentication failed. " +
                            "The data may have been tampered with or the key is incorrect.",
                    ),
                )
            }

            logger.debug {
                "ChaCha20-Poly1305 decryption successful: algorithm=${key.algorithm}," +
                    " decryptedSize=${decryptedData.size} bytes"
            }
            Result.success(decryptedData)
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "ChaCha20 decryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.error(e) { "ChaCha20 decryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(
                DomainError(
                    "ChaCha20-Poly1305 decryption failed: ${e.message}",
                    e,
                ),
            )
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
            throw DomainError(
                "Invalid ChaCha20 key length. " +
                    "Expected $KEY_LENGTH bytes (256 bits), got ${key.value.size} bytes",
            )
        }
    }
}
