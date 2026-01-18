package com.example.cryptographer.domain.text.valueobjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Value Object for AES number of rounds.
 *
 * Encapsulates AES round count validation and ensures round count is valid.
 * AES-128 uses 10 rounds, AES-192 uses 12 rounds, AES-256 uses 14 rounds.
 *
 * This is a Value Object following DDD principles - it validates on creation and is immutable.
 */
class AesNumRounds private constructor(
    val rounds: Int,
) : BaseValueObject() {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val AES_128_ROUNDS = 10
        private const val AES_192_ROUNDS = 12
        private const val AES_256_ROUNDS = 14

        /**
         * Creates number of rounds from encryption algorithm.
         *
         * @param algorithm AES algorithm (AES_128, AES_192, AES_256)
         * @return Result with AesNumRounds if valid, or error if validation fails
         */
        fun create(algorithm: EncryptionAlgorithm): Result<AesNumRounds> {
            val rounds = when (algorithm) {
                EncryptionAlgorithm.AES_128 -> AES_128_ROUNDS
                EncryptionAlgorithm.AES_192 -> AES_192_ROUNDS
                EncryptionAlgorithm.AES_256 -> AES_256_ROUNDS
                else -> {
                    logger.error { "AES number of rounds validation failed: unsupported algorithm=$algorithm" }
                    return Result.failure(
                        DomainFieldError("Unsupported AES algorithm: $algorithm"),
                    )
                }
            }

            logger.trace { "AES number of rounds created: algorithm=$algorithm, rounds=$rounds" }
            return Result.success(AesNumRounds(rounds))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AesNumRounds) return false
        return rounds == other.rounds
    }

    override fun hashCode(): Int {
        return rounds.hashCode()
    }

    override fun toString(): String {
        return "AesNumRounds(rounds=$rounds)"
    }
}
