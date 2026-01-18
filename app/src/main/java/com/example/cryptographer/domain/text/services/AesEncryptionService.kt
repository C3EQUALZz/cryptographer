package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.entities.aes.AesGcmMode
import com.example.cryptographer.domain.text.entities.aes.AesKeyExpansion
import com.example.cryptographer.domain.text.entities.aes.AesRoundKeys
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.aes.AesKeySize
import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.SecureRandom

/**
 * Domain service for AES encryption algorithm.
 * Supports AES-128, AES-192, and AES-256 with GCM mode.
 *
 * Note: This is a standalone service without a common interface,
 * as different encryption algorithms may have different requirements
 * (keys, matrices, parameters, etc.).
 */
class AesEncryptionService : DomainService() {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val GCM_TAG_LENGTH = 16 // bytes (128 bits)
        private const val GCM_IV_LENGTH = 12 // bytes (96 bits)
        private const val BITS_IN_BYTE = 8

        // AES key sizes in bytes
        private const val AES_128_KEY_SIZE_BYTES = 16 // 128 bits = 16 bytes
        private const val AES_192_KEY_SIZE_BYTES = 24 // 192 bits = 24 bytes
        private const val AES_256_KEY_SIZE_BYTES = 32 // 256 bits = 32 bytes
    }

    private data class KeyContext(
        val roundKeys: AesRoundKeys,
        val numRounds: AesNumRounds,
    )

    private data class CiphertextAndTag(
        val ciphertext: ByteArray,
        val tag: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CiphertextAndTag

            if (!ciphertext.contentEquals(other.ciphertext)) return false
            if (!tag.contentEquals(other.tag)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ciphertext.contentHashCode()
            result = 31 * result + tag.contentHashCode()
            return result
        }
    }

    private val emptyAad = ByteArray(0)

    /**
     * Encrypts data using the provided AES key with GCM mode.
     *
     * Uses custom AES implementation without Java Cipher.
     *
     * @param data Data to encrypt
     * @param key AES encryption key
     * @return Result with encrypted data or error
     */
    fun encrypt(data: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return try {
            logger.info { "Starting AES encryption: algorithm=${key.algorithm}, dataSize=${data.size} bytes" }

            // Validate key
            validateKey(key)
            logger.debug { "Key validation passed: keySize=${key.value.size} bytes" }

            // Create value objects for validation and type safety
            val keySize = AesKeySize.create(key.algorithm).getOrElse {
                throw UnsupportedAlgorithmError(key.algorithm, "AesEncryptionService")
            }
            val numRounds = AesNumRounds.create(key.algorithm).getOrElse {
                throw UnsupportedAlgorithmError(key.algorithm, "AesEncryptionService")
            }
            logger.debug { "Value objects created: keySize=$keySize, numRounds=$numRounds" }

            // Validate key bytes match expected size
            keySize.validateKeyBytes(key.value).getOrThrow()
            logger.debug { "Key bytes validation passed" }

            // Expand key to round keys
            logger.debug { "Expanding key to round keys: numRounds=${numRounds.rounds}" }
            val roundKeys = AesKeyExpansion.expandKey(key.value, numRounds)
            logger.debug { "Key expansion completed: generated ${roundKeys.roundKeys.size} round keys" }

            // Generate random IV (Initialization Vector)
            logger.debug { "Generating random IV: ivLength=$GCM_IV_LENGTH bytes" }
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            logger.debug { "IV generated successfully" }

            // Encrypt using AES-GCM
            logger.debug { "Starting AES-GCM encryption: plaintextSize=${data.size} bytes, ivSize=${iv.size} bytes" }
            val (ciphertext, tag) = AesGcmMode.encrypt(
                AesGcmMode.EncryptParams(
                    plaintext = data,
                    iv = iv,
                    aad = emptyAad,
                    roundKeys = roundKeys,
                    numRounds = numRounds,
                ),
            )
            logger.debug {
                "AES-GCM encryption completed: " +
                    "ciphertextSize=${ciphertext.size} bytes, " +
                    "tagSize=${tag.size} bytes"
            }

            // Combine ciphertext and tag: encryptedData = ciphertext || tag
            val encryptedData = ByteArray(ciphertext.size + tag.size)
            System.arraycopy(ciphertext, 0, encryptedData, 0, ciphertext.size)
            System.arraycopy(tag, 0, encryptedData, ciphertext.size, tag.size)
            logger.debug { "Ciphertext and tag combined: totalSize=${encryptedData.size} bytes" }

            logger.info {
                "AES encryption completed successfully: algorithm=${key.algorithm}, " +
                    "plaintextSize=${data.size} bytes, " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "ciphertextSize=${ciphertext.size} bytes, " +
                    "tagSize=${tag.size} bytes, " +
                    "ivSize=${iv.size} bytes"
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
        }
    }

    /**
     * Decrypts data using the provided AES key and verifies GCM authentication tag.
     *
     * Uses custom AES implementation without Java Cipher.
     *
     * @param encryptedText Encrypted data (ciphertext || tag) with IV
     * @param key AES encryption key
     * @return Result with decrypted data or error if authentication fails
     */
    fun decrypt(encryptedText: EncryptedText, key: EncryptionKey): Result<ByteArray> {
        return try {
            logger.info {
                "Starting AES decryption: " +
                    "algorithm=${key.algorithm}, " +
                    "encryptedSize=${encryptedText.encryptedData.size} bytes"
            }

            val iv = requireInitializationVector(encryptedText)
            val encryptedData = encryptedText.encryptedData
            requireEncryptedDataLength(encryptedData)

            val keyContext = buildKeyContext(key)
            val (ciphertext, tag) = splitCiphertextAndTag(encryptedData)
            logger.debug {
                "Encrypted data split: " +
                    "ciphertextSize=${ciphertext.size} bytes, " +
                    "tagSize=${tag.size} bytes, " +
                    "ivSize=${iv.size} bytes"
            }

            // Decrypt and verify using AES-GCM
            val decryptedData = decryptAndVerify(ciphertext, tag, iv, emptyAad, keyContext)

            logger.info {
                "AES decryption completed successfully: algorithm=${key.algorithm}, " +
                    "encryptedSize=${encryptedData.size} bytes, " +
                    "ciphertextSize=${ciphertext.size} bytes, " +
                    "tagSize=${tag.size} bytes, " +
                    "decryptedSize=${decryptedData.size} bytes"
            }
            Result.success(decryptedData)
        } catch (e: UnsupportedAlgorithmError) {
            logger.error(e) { "Decryption failed: unsupported algorithm=${key.algorithm}" }
            Result.failure(e)
        } catch (e: DomainError) {
            logger.error(e) { "Decryption failed: algorithm=${key.algorithm}, error=${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Generates a new AES encryption key for the specified algorithm.
     *
     * Uses SecureRandom directly instead of KeyGenerator, following the same pattern
     * as ChaCha20 and TripleDES implementations.
     *
     * @param algorithm AES algorithm (AES_128, AES_192, AES_256)
     * @return Result with generated key or error
     */
    fun generateKey(algorithm: EncryptionAlgorithm): Result<EncryptionKey> {
        return try {
            logger.info { "Starting AES key generation: algorithm=$algorithm" }

            val keySizeBytes = when (algorithm) {
                EncryptionAlgorithm.AES_128 -> AES_128_KEY_SIZE_BYTES
                EncryptionAlgorithm.AES_192 -> AES_192_KEY_SIZE_BYTES
                EncryptionAlgorithm.AES_256 -> AES_256_KEY_SIZE_BYTES
                else -> throw UnsupportedAlgorithmError(
                    algorithm,
                    "AesEncryptionService",
                )
            }
            logger.debug {
                "Key size determined: keySize=$keySizeBytes bytes (${keySizeBytes * BITS_IN_BYTE} bits)"
            }

            // Generate random key using SecureRandom (cryptographically secure random number generator)
            logger.debug { "Generating random key bytes using SecureRandom" }
            val keyBytes = ByteArray(keySizeBytes)
            SecureRandom().nextBytes(keyBytes)
            logger.debug { "Random key bytes generated successfully" }

            logger.info {
                "AES key generation completed successfully: algorithm=$algorithm, " +
                    "keySize=${keyBytes.size} bytes (${keyBytes.size * BITS_IN_BYTE} bits)"
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
        }
    }

    private fun requireInitializationVector(encryptedText: EncryptedText): ByteArray {
        val iv = encryptedText.initializationVector
        if (iv == null) {
            logger.warn { "Decryption failed: IV is missing" }
            throw DomainError("IV (Initialization Vector) is missing for decryption")
        }
        return iv
    }

    private fun requireEncryptedDataLength(encryptedData: ByteArray) {
        if (encryptedData.size < GCM_TAG_LENGTH) {
            logger.warn {
                "Decryption failed: encrypted data too short, " +
                    "size=${encryptedData.size}, " +
                    "expected at least $GCM_TAG_LENGTH bytes"
            }
            throw DomainError(
                "Encrypted data is too short. " +
                    "Minimum size is $GCM_TAG_LENGTH bytes for tag",
            )
        }
    }

    private fun buildKeyContext(key: EncryptionKey): KeyContext {
        validateKey(key)
        logger.debug { "Key validation passed: keySize=${key.value.size} bytes" }

        val keySize = AesKeySize.create(key.algorithm).getOrElse {
            throw UnsupportedAlgorithmError(key.algorithm, "AesEncryptionService")
        }
        val numRounds = AesNumRounds.create(key.algorithm).getOrElse {
            throw UnsupportedAlgorithmError(key.algorithm, "AesEncryptionService")
        }
        logger.debug { "Value objects created: keySize=$keySize, numRounds=$numRounds" }

        keySize.validateKeyBytes(key.value).getOrThrow()
        logger.debug { "Key bytes validation passed" }

        logger.debug { "Expanding key to round keys: numRounds=${numRounds.rounds}" }
        val roundKeys = AesKeyExpansion.expandKey(key.value, numRounds)
        logger.debug { "Key expansion completed: generated ${roundKeys.roundKeys.size} round keys" }

        return KeyContext(roundKeys = roundKeys, numRounds = numRounds)
    }

    private fun splitCiphertextAndTag(encryptedData: ByteArray): CiphertextAndTag {
        val ciphertextSize = encryptedData.size - GCM_TAG_LENGTH
        val ciphertext = ByteArray(ciphertextSize)
        val tag = ByteArray(GCM_TAG_LENGTH)
        System.arraycopy(encryptedData, 0, ciphertext, 0, ciphertextSize)
        System.arraycopy(encryptedData, ciphertextSize, tag, 0, GCM_TAG_LENGTH)
        return CiphertextAndTag(ciphertext = ciphertext, tag = tag)
    }

    private fun decryptAndVerify(
        ciphertext: ByteArray,
        tag: ByteArray,
        iv: ByteArray,
        aad: ByteArray,
        keyContext: KeyContext,
    ): ByteArray {
        logger.debug { "Starting AES-GCM decryption and authentication verification" }
        val decryptedData = AesGcmMode.decrypt(
            AesGcmMode.DecryptParams(
                ciphertext = ciphertext,
                tag = tag,
                iv = iv,
                aad = aad,
                roundKeys = keyContext.roundKeys,
                numRounds = keyContext.numRounds,
            ),
        )
        if (decryptedData == null) {
            logger.warn {
                "Decryption failed: authentication tag verification failed - data may have been tampered with"
            }
            throw DomainError(
                "Authentication failed: invalid tag. The data may have been tampered with.",
            )
        }
        logger.debug { "Authentication tag verification passed" }
        return decryptedData
    }

    /**
     * Validates that the key matches the expected length for its algorithm.
     */
    private fun validateKey(key: EncryptionKey) {
        // Check key length according to the algorithm
        val expectedKeyLength = when (key.algorithm) {
            EncryptionAlgorithm.AES_128 -> AES_128_KEY_SIZE_BYTES
            EncryptionAlgorithm.AES_192 -> AES_192_KEY_SIZE_BYTES
            EncryptionAlgorithm.AES_256 -> AES_256_KEY_SIZE_BYTES
            else -> throw UnsupportedAlgorithmError(
                key.algorithm,
                "AesEncryptionService",
            )
        }

        if (key.value.size != expectedKeyLength) {
            throw DomainError(
                "Invalid key length. Expected $expectedKeyLength bytes, got ${key.value.size} bytes",
            )
        }
    }
}
