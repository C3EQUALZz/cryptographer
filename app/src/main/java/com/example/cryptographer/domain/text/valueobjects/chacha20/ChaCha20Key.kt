package com.example.cryptographer.domain.text.valueobjects.chacha20

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * Value Object for validated ChaCha20 key material.
 */
class ChaCha20Key private constructor(
    val algorithm: EncryptionAlgorithm,
    private val bytes: ByteArray,
) : BaseValueObject() {

    companion object {
        const val SIZE_BYTES = 32
        private const val HASH_MULTIPLIER = 31

        fun create(algorithm: EncryptionAlgorithm, keyBytes: ByteArray): Result<ChaCha20Key> {
            val errorMessage = when {
                algorithm != EncryptionAlgorithm.CHACHA20_256 ->
                    "Unsupported ChaCha20 algorithm: $algorithm"
                keyBytes.size != SIZE_BYTES ->
                    "Invalid ChaCha20 key length. Expected $SIZE_BYTES bytes, got ${keyBytes.size} bytes"
                else -> null
            }

            return if (errorMessage == null) {
                Result.success(
                    ChaCha20Key(
                        algorithm = algorithm,
                        bytes = keyBytes.copyOf(),
                    ),
                )
            } else {
                Result.failure(DomainFieldError(errorMessage))
            }
        }
    }

    internal fun raw(): ByteArray = bytes

    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChaCha20Key) return false
        return algorithm == other.algorithm && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = algorithm.hashCode()
        result = HASH_MULTIPLIER * result + bytes.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ChaCha20Key(algorithm=$algorithm, size=${bytes.size})"
    }
}
