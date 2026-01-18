package com.example.cryptographer.domain.text.valueobjects.tdes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * Value Object for validated 3DES key material.
 */
class TripleDesKey private constructor(
    val algorithm: EncryptionAlgorithm,
    private val keyBytes: ByteArray,
    private val key1: ByteArray,
    private val key2: ByteArray,
    private val key3: ByteArray?,
) : BaseValueObject() {

    val isTwoKey: Boolean
        get() = algorithm == EncryptionAlgorithm.TDES_112

    companion object {
        private const val DES_KEY_LENGTH = 8
        private const val TDES_112_KEY_LENGTH = 16
        private const val TDES_168_KEY_LENGTH = 24
        private const val KEY_PARTS_TWO = 2
        private const val KEY_PARTS_THREE = 3
        private const val HASH_MULTIPLIER = 31

        fun expectedKeyLength(algorithm: EncryptionAlgorithm): Int {
            return when (algorithm) {
                EncryptionAlgorithm.TDES_112 -> TDES_112_KEY_LENGTH
                EncryptionAlgorithm.TDES_168 -> TDES_168_KEY_LENGTH
                else -> throw DomainFieldError("Unsupported 3DES algorithm: $algorithm")
            }
        }

        /**
         * Creates a validated 3DES key from algorithm and bytes.
         */
        fun create(algorithm: EncryptionAlgorithm, keyBytes: ByteArray): Result<TripleDesKey> {
            val expectedLength = when (algorithm) {
                EncryptionAlgorithm.TDES_112 -> TDES_112_KEY_LENGTH
                EncryptionAlgorithm.TDES_168 -> TDES_168_KEY_LENGTH
                else -> null
            }

            val errorMessage = when {
                expectedLength == null -> "Unsupported 3DES algorithm: $algorithm"
                keyBytes.size != expectedLength ->
                    "Invalid 3DES key length. Expected $expectedLength bytes, got ${keyBytes.size} bytes"
                else -> null
            }

            return if (errorMessage == null) {
                val key1 = keyBytes.sliceArray(0 until DES_KEY_LENGTH)
                val key2 = keyBytes.sliceArray(DES_KEY_LENGTH until (DES_KEY_LENGTH * KEY_PARTS_TWO))
                val key3 = if (algorithm == EncryptionAlgorithm.TDES_168) {
                    keyBytes.sliceArray(
                        (DES_KEY_LENGTH * KEY_PARTS_TWO) until (DES_KEY_LENGTH * KEY_PARTS_THREE),
                    )
                } else {
                    null
                }

                Result.success(
                    TripleDesKey(
                        algorithm = algorithm,
                        keyBytes = keyBytes.copyOf(),
                        key1 = key1,
                        key2 = key2,
                        key3 = key3,
                    ),
                )
            } else {
                Result.failure(DomainFieldError(errorMessage))
            }
        }
    }

    internal fun key1Raw(): ByteArray = key1

    internal fun key2Raw(): ByteArray = key2

    internal fun key3Raw(): ByteArray? = key3

    fun toByteArray(): ByteArray = keyBytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TripleDesKey) return false
        return algorithm == other.algorithm && keyBytes.contentEquals(other.keyBytes)
    }

    override fun hashCode(): Int {
        var result = algorithm.hashCode()
        result = HASH_MULTIPLIER * result + keyBytes.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "TripleDesKey(algorithm=$algorithm, size=${keyBytes.size})"
    }
}
