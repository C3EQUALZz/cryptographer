package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.entities.chacha20.ChaCha20Poly1305Aead
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.chacha20.ChaCha20Key
import com.example.cryptographer.domain.text.valueobjects.chacha20.ChaCha20Nonce
import com.example.cryptographer.domain.text.valueobjects.chacha20.Poly1305Tag
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
        private const val NONCE_LENGTH = ChaCha20Nonce.SIZE_BYTES
        private const val KEY_LENGTH = ChaCha20Key.SIZE_BYTES
        private const val TAG_LENGTH = Poly1305Tag.SIZE_BYTES
    }

    private val emptyAad = ByteArray(0)

    /**
     * Encrypts data using the provided ChaCha20 key with Poly1305 authentication.
     *
     * @param data Data to encrypt
     * @param key ChaCha20 encryption key
     * @return Result with encrypted data (ciphertext + tag) and nonce, or error
     */
    fun encrypt(data: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return try {
            val keyMaterial = buildKeyMaterial(key)
            logger.debug { "Starting ChaCha20-Poly1305 encryption: dataSize=${data.size} bytes" }

            // Generate random nonce
            val nonce = ByteArray(NONCE_LENGTH)
            secureRandom.nextBytes(nonce)
            val nonceValue = ChaCha20Nonce.create(nonce).getOrThrow()

            // Encrypt and authenticate
            val (ciphertext, tag) = aead.encrypt(
                plaintext = data,
                key = keyMaterial,
                nonce = nonceValue,
                aad = emptyAad, // No additional authenticated data
            )

            // Combine ciphertext and tag: encryptedData = ciphertext || tag
            val tagBytes = tag.toByteArray()
            val encryptedData = ByteArray(ciphertext.size + tagBytes.size)
            System.arraycopy(ciphertext, 0, encryptedData, 0, ciphertext.size)
            System.arraycopy(tagBytes, 0, encryptedData, ciphertext.size, tagBytes.size)

            logger.debug {
                "ChaCha20-Poly1305 encryption successful: " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "ciphertextSize=${ciphertext.size} bytes, " +
                    "tagSize=${tagBytes.size} bytes"
            }

            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = key.algorithm,
                    initializationVector = nonceValue.toByteArray(),
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
            val keyMaterial = buildKeyMaterial(key)
            val nonce = requireNonce(encryptedText)
            val encryptedData = encryptedText.encryptedData
            requireEncryptedDataLength(encryptedData)

            logger.debug {
                "Starting ChaCha20-Poly1305 decryption: " +
                    "encryptedSize=${encryptedData.size} bytes"
            }

            // Split encryptedData into ciphertext and tag
            val ciphertextSize = encryptedData.size - TAG_LENGTH
            val ciphertext = ByteArray(ciphertextSize)
            val tagBytes = ByteArray(TAG_LENGTH)
            System.arraycopy(encryptedData, 0, ciphertext, 0, ciphertextSize)
            System.arraycopy(encryptedData, ciphertextSize, tagBytes, 0, TAG_LENGTH)

            val tag = Poly1305Tag.create(tagBytes).getOrThrow()

            // Decrypt and verify
            val plaintext = aead.decrypt(
                ciphertext = ciphertext,
                tag = tag,
                key = keyMaterial,
                nonce = nonce,
                aad = emptyAad,
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
            ensureAlgorithmSupported(algorithm)
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

    private fun ensureAlgorithmSupported(algorithm: EncryptionAlgorithm) {
        if (algorithm != EncryptionAlgorithm.CHACHA20_256) {
            throw UnsupportedAlgorithmError(
                algorithm,
                "ChaCha20EncryptionService",
            )
        }
    }

    private fun buildKeyMaterial(key: EncryptionKey): ChaCha20Key {
        ensureAlgorithmSupported(key.algorithm)
        return ChaCha20Key.create(key.algorithm, key.value).getOrElse { error ->
            throw error
        }
    }

    private fun requireNonce(encryptedText: EncryptedText): ChaCha20Nonce {
        return ChaCha20Nonce.create(encryptedText.initializationVector).getOrElse { error ->
            logger.warn { "Decryption failed: ${error.message}" }
            throw error
        }
    }

    private fun requireEncryptedDataLength(encryptedData: ByteArray) {
        if (encryptedData.size < TAG_LENGTH) {
            logger.warn { "Decryption failed: encrypted data too short, size=${encryptedData.size}" }
            throw DomainError("Encrypted data is too short. Minimum size is $TAG_LENGTH bytes for tag")
        }
    }
}
