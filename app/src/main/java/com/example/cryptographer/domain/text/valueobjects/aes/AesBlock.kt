package com.example.cryptographer.domain.text.valueobjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Value Object for AES block (128 bits = 16 bytes).
 *
 * Encapsulates validation logic and ensures block size is correct.
 * AES operates on fixed 128-bit blocks.
 *
 * This is a Value Object following DDD principles - it validates on creation and is immutable.
 */
class AesBlock private constructor(
    val bytes: ByteArray,
) : BaseValueObject() {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val AES_BLOCK_SIZE = 16 // bytes (128 bits)

        /**
         * Creates an AES block from bytes.
         *
         * @param bytes Block bytes (must be exactly 16 bytes)
         * @return Result with AesBlock if valid, or error if validation fails
         */
        fun create(bytes: ByteArray): Result<AesBlock> {
            return when {
                bytes.size != AES_BLOCK_SIZE -> {
                    logger.error {
                        "AES block validation failed: " +
                            "expected $AES_BLOCK_SIZE bytes, " +
                            "got ${bytes.size} bytes"
                    }
                    Result.failure(
                        DomainFieldError(
                            "AES block must be exactly " +
                                "$AES_BLOCK_SIZE bytes, got ${bytes.size}",
                        ),
                    )
                }
                else -> {
                    Result.success(AesBlock(bytes.copyOf()))
                }
            }
        }

        /**
         * Creates a zero block (all bytes are 0x00).
         */
        fun createZero(): AesBlock {
            return AesBlock(ByteArray(AES_BLOCK_SIZE))
        }
    }

    /**
     * Converts block to byte array.
     */
    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AesBlock) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "AesBlock(size=${bytes.size})"
    }
}
