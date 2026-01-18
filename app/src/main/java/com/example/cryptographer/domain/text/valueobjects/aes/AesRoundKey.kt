package com.example.cryptographer.domain.text.valueobjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Value Object for AES round key (128 bits = 16 bytes).
 *
 * Represents a single round key used in AES encryption/decryption.
 * Each round uses a 16-byte (128-bit) round key.
 *
 * This is a Value Object following DDD principles - it's immutable.
 */
class AesRoundKey private constructor(
    val bytes: ByteArray,
) : BaseValueObject() {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ROUND_KEY_SIZE = 16 // bytes (128 bits)

        /**
         * Creates a round key from bytes.
         *
         * @param bytes Round key bytes (must be exactly 16 bytes)
         * @return Result with AesRoundKey if valid, or error if validation fails
         */
        fun create(bytes: ByteArray): Result<AesRoundKey> {
            return when {
                bytes.size != ROUND_KEY_SIZE -> {
                    logger.error {
                        "AES round key validation failed: expected $ROUND_KEY_SIZE bytes, got ${bytes.size} bytes"
                    }
                    Result.failure(
                        DomainFieldError(
                            "AES round key must be exactly $ROUND_KEY_SIZE bytes, got ${bytes.size}",
                        ),
                    )
                }
                else -> {
                    Result.success(AesRoundKey(bytes.copyOf()))
                }
            }
        }
    }

    /**
     * Converts round key to byte array.
     */
    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AesRoundKey) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "AesRoundKey(size=${bytes.size})"
    }
}
