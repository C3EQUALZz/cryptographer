package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Helper object for 3DES validation operations.
 * Extracted to reduce function count in TripleDesEncryptionService.
 */
internal object TripleDesValidationHelper {
    private val logger = KotlinLogging.logger {}

    private const val DES_KEY_LENGTH = 8 // bytes (64 bits, 56 bits effective)
    private const val TDES_112_KEY_LENGTH = 16 // bytes (2 DES keys)
    private const val TDES_168_KEY_LENGTH = 24 // bytes (3 DES keys)
    private const val IV_LENGTH = 8 // bytes (64 bits, same as DES block size)
    private const val KEY_MULTIPLIER_2 = 2
    private const val KEY_MULTIPLIER_3 = 3

    /**
     * Validates that the key matches the expected length for its algorithm.
     */
    fun validateKey(key: EncryptionKey) {
        if (!isAlgorithmSupported(key.algorithm)) {
            throw UnsupportedAlgorithmError(key.algorithm, "TripleDesEncryptionService")
        }

        val expectedKeyLength = getExpectedKeyLength(key.algorithm)
        if (key.value.size != expectedKeyLength) {
            throw DomainError(
                "Invalid 3DES key length. " +
                    "Expected $expectedKeyLength bytes, got ${key.value.size} bytes",
            )
        }
    }

    /**
     * Validates decryption input (IV and encrypted data).
     */
    fun validateDecryptionInput(encryptedText: EncryptedText): DomainError? {
        val ivError = validateIv(encryptedText.initializationVector)
        if (ivError != null) {
            return ivError
        }

        return validateEncryptedData(encryptedText.encryptedData)
    }

    private fun validateIv(iv: ByteArray?): DomainError? {
        val nullError = checkIvNull(iv)
        if (nullError != null) {
            return nullError
        }

        return checkIvLength(iv!!)
    }

    private fun checkIvNull(iv: ByteArray?): DomainError? {
        if (iv == null) {
            logger.warn { "Decryption failed: IV is missing" }
            return DomainError("IV (Initialization Vector) is missing for decryption")
        }
        return null
    }

    private fun checkIvLength(iv: ByteArray): DomainError? {
        if (iv.size != IV_LENGTH) {
            logger.warn { "Decryption failed: invalid IV length=${iv.size}, expected=$IV_LENGTH" }
            return DomainError("Invalid IV length. Expected $IV_LENGTH bytes, got ${iv.size} bytes")
        }
        return null
    }

    private fun validateEncryptedData(encryptedData: ByteArray): DomainError? {
        if (encryptedData.size < IV_LENGTH) {
            logger.warn { "Decryption failed: encrypted data too short, size=${encryptedData.size}" }
            return DomainError("Encrypted data is too short. Minimum size is $IV_LENGTH bytes")
        }

        return null
    }

    fun isAlgorithmSupported(algorithm: EncryptionAlgorithm): Boolean {
        return algorithm == EncryptionAlgorithm.TDES_112 || algorithm == EncryptionAlgorithm.TDES_168
    }

    fun getExpectedKeyLength(algorithm: EncryptionAlgorithm): Int {
        return when (algorithm) {
            EncryptionAlgorithm.TDES_112 -> TDES_112_KEY_LENGTH
            EncryptionAlgorithm.TDES_168 -> TDES_168_KEY_LENGTH
            else -> throw UnsupportedAlgorithmError(algorithm, "TripleDesEncryptionService")
        }
    }

    fun extractKeys(key: EncryptionKey): Triple<ByteArray, ByteArray, ByteArray?> {
        return when (key.algorithm) {
            EncryptionAlgorithm.TDES_112 -> {
                val key1 = key.value.sliceArray(0 until DES_KEY_LENGTH)
                val key2 = key.value.sliceArray(DES_KEY_LENGTH until (DES_KEY_LENGTH * 2))
                Triple(key1, key2, null)
            }

            EncryptionAlgorithm.TDES_168 -> {
                val key1 = key.value.sliceArray(0 until DES_KEY_LENGTH)
                val key2 = key.value.sliceArray(
                    DES_KEY_LENGTH
                        until
                        (DES_KEY_LENGTH * KEY_MULTIPLIER_2),
                )
                val key3 = key.value.sliceArray(
                    (DES_KEY_LENGTH * KEY_MULTIPLIER_2)
                        until
                        (DES_KEY_LENGTH * KEY_MULTIPLIER_3),
                )
                Triple(key1, key2, key3)
            }

            else -> throw UnsupportedAlgorithmError(key.algorithm, "TripleDesEncryptionService")
        }
    }
}
