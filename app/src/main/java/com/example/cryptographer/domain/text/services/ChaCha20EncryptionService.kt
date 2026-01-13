package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.services.crypto.chacha20poly1305.ChaCha20Poly1305Aead
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

    private val aead = ChaCha20Poly1305Aead()
    private val secureRandom = SecureRandom()

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

            logger.debug { "Starting ChaCha20-Poly1305 encryption: dataSize=${data.size} bytes" }

            // Generate random nonce
            val nonce = ByteArray(NONCE_LENGTH)
            secureRandom.nextBytes(nonce)

            // Encrypt and authenticate
            val (ciphertext, tag) = aead.encrypt(
                plaintext = data,
                key = key.value,
                nonce = nonce,
                aad = ByteArray(0), // No additional authenticated data
            )

            // Combine ciphertext and tag: encryptedData = ciphertext || tag
            val encryptedData = ByteArray(ciphertext.size + tag.size)
            System.arraycopy(ciphertext, 0, encryptedData, 0, ciphertext.size)
            System.arraycopy(tag, 0, encryptedData, ciphertext.size, tag.size)

            logger.debug {
                "ChaCha20-Poly1305 encryption successful: " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "ciphertextSize=${ciphertext.size} bytes, " +
                    "tagSize=${tag.size} bytes"
            }

            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = key.algorithm,
                    initializationVector = nonce,
                ),
            )
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "Encryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: DomainError) {
            logger.error(e) { "Encryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.error(e) { "Encryption failed: unexpected error" }
            Result.failure(DomainError("Encryption failed: ${e.message}", e))
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

            val result = when {
                encryptedText.initializationVector == null -> {
                    logger.warn { "Decryption failed: nonce is missing" }
                    Result.failure(
                        DomainError("Nonce (initialization vector) is missing for decryption"),
                    )
                }

                encryptedText.initializationVector.size != NONCE_LENGTH -> {
                    val nonceSize = encryptedText.initializationVector.size
                    logger.warn { "Decryption failed: invalid nonce length=$nonceSize, expected=$NONCE_LENGTH" }
                    Result.failure(
                        DomainError("Invalid nonce length. Expected $NONCE_LENGTH bytes, got $nonceSize bytes"),
                    )
                }

                encryptedText.encryptedData.size < TAG_LENGTH -> {
                    val encryptedSize = encryptedText.encryptedData.size
                    logger.warn { "Decryption failed: encrypted data too short, size=$encryptedSize" }
                    Result.failure(
                        DomainError("Encrypted data is too short. Minimum size is $TAG_LENGTH bytes for tag"),
                    )
                }

                else -> {
                    val nonce = encryptedText.initializationVector
                    val encryptedData = encryptedText.encryptedData

                    logger.debug {
                        "Starting ChaCha20-Poly1305 decryption: " +
                            "encryptedSize=${encryptedData.size} bytes"
                    }

                    // Split encryptedData into ciphertext and tag
                    val ciphertextSize = encryptedData.size - TAG_LENGTH
                    val ciphertext = ByteArray(ciphertextSize)
                    val tag = ByteArray(TAG_LENGTH)
                    System.arraycopy(encryptedData, 0, ciphertext, 0, ciphertextSize)
                    System.arraycopy(encryptedData, ciphertextSize, tag, 0, TAG_LENGTH)

                    // Decrypt and verify
                    val plaintext = aead.decrypt(
                        ciphertext = ciphertext,
                        tag = tag,
                        key = key.value,
                        nonce = nonce,
                        aad = ByteArray(0), // No additional authenticated data
                    )

                    if (plaintext == null) {
                        logger.warn { "Decryption failed: authentication tag verification failed" }
                        Result.failure(
                            DomainError("Authentication failed: invalid tag. The data may have been tampered with."),
                        )
                    } else {
                        logger.debug {
                            "ChaCha20-Poly1305 decryption successful: " +
                                "decryptedSize=${plaintext.size} bytes"
                        }
                        Result.success(plaintext)
                    }
                }
            }

            result
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "Decryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: DomainError) {
            logger.error(e) { "Decryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.error(e) { "Decryption failed: unexpected error" }
            Result.failure(DomainError("Decryption failed: ${e.message}", e))
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

            logger.debug { "Generating ChaCha20 key: algorithm=$algorithm, keySize=$KEY_LENGTH bytes" }

            // Generate random key
            val keyBytes = ByteArray(KEY_LENGTH)
            secureRandom.nextBytes(keyBytes)

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
        } catch (e: Exception) {
            logger.error(e) { "Key generation failed: unexpected error" }
            Result.failure(DomainError("Key generation failed: ${e.message}", e))
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
