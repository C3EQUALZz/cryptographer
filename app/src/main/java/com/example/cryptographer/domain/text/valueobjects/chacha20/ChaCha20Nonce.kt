package com.example.cryptographer.domain.text.valueobjects.chacha20

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject

/**
 * Value Object for ChaCha20 nonce (96-bit).
 */
class ChaCha20Nonce private constructor(
    val bytes: ByteArray,
) : BaseValueObject() {
    companion object {
        const val SIZE_BYTES = 12

        fun create(bytes: ByteArray?): Result<ChaCha20Nonce> {
            val errorMessage = when {
                bytes == null -> "Nonce (initialization vector) is missing for decryption"
                bytes.size != SIZE_BYTES ->
                    "Invalid nonce length. Expected $SIZE_BYTES bytes, got ${bytes.size} bytes"
                else -> null
            }

            return if (errorMessage == null) {
                Result.success(ChaCha20Nonce(checkNotNull(bytes).copyOf()))
            } else {
                Result.failure(DomainFieldError(errorMessage))
            }
        }
    }

    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChaCha20Nonce) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "ChaCha20Nonce(size=${bytes.size})"
    }
}
