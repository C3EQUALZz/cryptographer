package com.example.cryptographer.domain.text.valueobjects.chacha20

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject

/**
 * Value Object for Poly1305 authentication tag (128-bit).
 */
class Poly1305Tag private constructor(
    val bytes: ByteArray,
) : BaseValueObject() {
    companion object {
        const val SIZE_BYTES = 16

        fun create(bytes: ByteArray): Result<Poly1305Tag> {
            val errorMessage = if (bytes.size != SIZE_BYTES) {
                "Invalid tag length. Expected $SIZE_BYTES bytes, got ${bytes.size} bytes"
            } else {
                null
            }

            return if (errorMessage == null) {
                Result.success(Poly1305Tag(bytes.copyOf()))
            } else {
                Result.failure(DomainFieldError(errorMessage))
            }
        }
    }

    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Poly1305Tag) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "Poly1305Tag(size=${bytes.size})"
    }
}
