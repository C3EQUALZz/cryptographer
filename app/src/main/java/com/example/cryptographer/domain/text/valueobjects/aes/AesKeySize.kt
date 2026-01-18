package com.example.cryptographer.domain.text.valueobjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Value Object for AES key size.
 *
 * Encapsulates AES key size validation and provides methods to determine
 * number of rounds and key size in bytes.
 *
 * Supports AES-128 (16 bytes, 10 rounds), AES-192 (24 bytes, 12 rounds),
 * and AES-256 (32 bytes, 14 rounds).
 *
 * This is a Value Object following DDD principles - it validates on creation and is immutable.
 */
class AesKeySize private constructor(
    val sizeBytes: Int,
    val algorithm: EncryptionAlgorithm,
) : BaseValueObject() {
    val numRounds: Int
        get() = when (algorithm) {
            EncryptionAlgorithm.AES_128 -> AES_128_ROUNDS
            EncryptionAlgorithm.AES_192 -> AES_192_ROUNDS
            EncryptionAlgorithm.AES_256 -> AES_256_ROUNDS
            else -> throw DomainFieldError("Unsupported AES algorithm: $algorithm")
        }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val AES_128_SIZE = 16 // bytes (128 bits)
        private const val AES_192_SIZE = 24 // bytes (192 bits)
        private const val AES_256_SIZE = 32 // bytes (256 bits)
        private const val AES_128_ROUNDS = 10
        private const val AES_192_ROUNDS = 12
        private const val AES_256_ROUNDS = 14

        /**
         * Creates an AES key size from encryption algorithm.
         *
         * @param algorithm AES algorithm (AES_128, AES_192, AES_256)
         * @return Result with AesKeySize if valid, or error if validation fails
         */
        fun create(algorithm: EncryptionAlgorithm): Result<AesKeySize> {
            val sizeBytes = when (algorithm) {
                EncryptionAlgorithm.AES_128 -> AES_128_SIZE
                EncryptionAlgorithm.AES_192 -> AES_192_SIZE
                EncryptionAlgorithm.AES_256 -> AES_256_SIZE
                else -> {
                    logger.error { "AES key size validation failed: unsupported algorithm=$algorithm" }
                    return Result.failure(
                        DomainFieldError("Unsupported AES algorithm: $algorithm"),
                    )
                }
            }

            logger.trace { "AES key size created: algorithm=$algorithm, sizeBytes=$sizeBytes bytes" }
            return Result.success(AesKeySize(sizeBytes, algorithm))
        }
    }

    /**
     * Validates that the provided key bytes match this key size.
     */
    fun validateKeyBytes(keyBytes: ByteArray): Result<Unit> {
        return if (keyBytes.size == sizeBytes) {
            logger.trace {
                "Key bytes validation passed: " +
                    "expected=$sizeBytes bytes, " +
                    "actual=${keyBytes.size} bytes, " +
                    "algorithm=$algorithm"
            }
            Result.success(Unit)
        } else {
            logger.error {
                "Key bytes validation failed: " +
                    "expected=$sizeBytes bytes, " +
                    "got=${keyBytes.size} bytes, " +
                    "algorithm=$algorithm"
            }
            Result.failure(
                DomainFieldError(
                    "Key size mismatch: " +
                        "expected $sizeBytes bytes, " +
                        "got ${keyBytes.size} bytes",
                ),
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AesKeySize) return false
        return sizeBytes == other.sizeBytes && algorithm == other.algorithm
    }

    override fun hashCode(): Int {
        return sizeBytes.hashCode() * 31 + algorithm.hashCode()
    }

    override fun toString(): String {
        return "AesKeySize(size=$sizeBytes bytes, algorithm=$algorithm, rounds=$numRounds)"
    }
}
