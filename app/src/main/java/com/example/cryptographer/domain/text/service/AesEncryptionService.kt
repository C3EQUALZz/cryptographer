package com.example.cryptographer.domain.text.service

import com.example.cryptographer.domain.text.entity.EncryptedText
import com.example.cryptographer.domain.text.entity.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.util.Logger
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Domain service for AES encryption algorithm.
 * Supports AES-128, AES-192, and AES-256 with GCM mode.
 * 
 * Note: This is a standalone service without a common interface,
 * as different encryption algorithms may have different requirements
 * (keys, matrices, parameters, etc.).
 */
class AesEncryptionService {

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128 // bits
        private const val GCM_IV_LENGTH = 12 // bytes (96 bits)
    }

    /**
     * Encrypts data using the provided AES key.
     *
     * @param data Data to encrypt
     * @param key AES encryption key
     * @return Result with encrypted data or error
     */
    fun encrypt(data: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return try {
            validateKey(key)

            Logger.d("Starting encryption: algorithm=${key.algorithm}, dataSize=${data.size} bytes")
            val secretKey = SecretKeySpec(key.value, ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)

            // Generate random IV (Initialization Vector)
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val encryptedData = cipher.doFinal(data)

            Logger.d("Encryption successful: algorithm=${key.algorithm}, encryptedSize=${encryptedData.size} bytes")
            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = key.algorithm,
                    initializationVector = iv
                )
            )
        } catch (e: Exception) {
            Logger.e("Encryption failed: algorithm=${key.algorithm}, error=${e.message}", e)
            Result.failure(
                Exception("Encryption error: ${e.message}", e)
            )
        }
    }

    /**
     * Decrypts data using the provided AES key.
     *
     * @param encryptedText Encrypted data with IV
     * @param key AES encryption key
     * @return Result with decrypted data or error
     */
    fun decrypt(encryptedText: EncryptedText, key: EncryptionKey): Result<ByteArray> {
        return try {
            validateKey(key)

            if (encryptedText.initializationVector == null) {
                Logger.w("Decryption failed: IV is missing")
                return Result.failure(
                    IllegalArgumentException("IV (Initialization Vector) is missing for decryption")
                )
            }

            Logger.d("Starting decryption: algorithm=${key.algorithm}, encryptedSize=${encryptedText.encryptedData.size} bytes")
            val secretKey = SecretKeySpec(key.value, ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)

            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, encryptedText.initializationVector)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            val decryptedData = cipher.doFinal(encryptedText.encryptedData)

            Logger.d("Decryption successful: algorithm=${key.algorithm}, decryptedSize=${decryptedData.size} bytes")
            Result.success(decryptedData)
        } catch (e: Exception) {
            Logger.e("Decryption failed: algorithm=${key.algorithm}, error=${e.message}", e)
            Result.failure(
                Exception("Decryption error: ${e.message}", e)
            )
        }
    }

    /**
     * Generates a new AES encryption key for the specified algorithm.
     *
     * @param algorithm AES algorithm (AES_128, AES_192, AES_256)
     * @return Result with generated key or error
     */
    fun generateKey(algorithm: EncryptionAlgorithm): Result<EncryptionKey> {
        return try {
            val keySize = when (algorithm) {
                EncryptionAlgorithm.AES_128 -> 128
                EncryptionAlgorithm.AES_192 -> 192
                EncryptionAlgorithm.AES_256 -> 256
            }

            Logger.d("Generating encryption key: algorithm=$algorithm, keySize=$keySize bits")
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
            keyGenerator.init(keySize)
            val secretKey: SecretKey = keyGenerator.generateKey()

            Logger.d("Key generated successfully: algorithm=$algorithm, keyLength=${secretKey.encoded.size} bytes")
            Result.success(
                EncryptionKey(
                    value = secretKey.encoded,
                    algorithm = algorithm
                )
            )
        } catch (e: Exception) {
            Logger.e("Key generation failed: algorithm=$algorithm, error=${e.message}", e)
            Result.failure(
                Exception("Key generation error: ${e.message}", e)
            )
        }
    }

    /**
     * Validates that the key matches the expected length for its algorithm.
     */
    private fun validateKey(key: EncryptionKey) {
        // Check key length according to the algorithm
        val expectedKeyLength = when (key.algorithm) {
            EncryptionAlgorithm.AES_128 -> 16 // 128 bits = 16 bytes
            EncryptionAlgorithm.AES_192 -> 24 // 192 bits = 24 bytes
            EncryptionAlgorithm.AES_256 -> 32 // 256 bits = 32 bytes
        }

        if (key.value.size != expectedKeyLength) {
            throw IllegalArgumentException(
                "Invalid key length. Expected $expectedKeyLength bytes, got ${key.value.size} bytes"
            )
        }
    }
}

