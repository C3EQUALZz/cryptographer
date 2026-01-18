package com.example.cryptographer.domain.text.valueobjects.tdes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject

/**
 * Value Object for 3DES IV (64-bit block size).
 */
class TripleDesIv private constructor(
    val bytes: ByteArray,
) : BaseValueObject() {
    companion object {
        const val SIZE_BYTES = 8

        /**
         * Creates an IV from bytes (must be exactly 8 bytes).
         */
        fun create(bytes: ByteArray?): Result<TripleDesIv> {
            val errorMessage = when {
                bytes == null -> "IV (Initialization Vector) is missing for decryption"
                bytes.size != SIZE_BYTES ->
                    "Invalid IV length. Expected $SIZE_BYTES bytes, got ${bytes.size} bytes"
                else -> null
            }

            return if (errorMessage == null) {
                Result.success(TripleDesIv(checkNotNull(bytes).copyOf()))
            } else {
                Result.failure(DomainFieldError(errorMessage))
            }
        }
    }

    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TripleDesIv) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "TripleDesIv(size=${bytes.size})"
    }
}
