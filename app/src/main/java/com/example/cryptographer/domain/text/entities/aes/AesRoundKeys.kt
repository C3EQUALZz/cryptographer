package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.common.entities.BaseEntity
import com.example.cryptographer.domain.text.valueobjects.aes.AesRoundKey
import java.time.Instant
import java.util.UUID

/**
 * Entity representing AES round keys for encryption/decryption.
 *
 * Round keys are derived from the original cipher key through key expansion.
 * Contains all round keys needed for AES encryption/decryption operations.
 *
 * This is a Domain Entity following DDD principles - it has identity and lifecycle.
 */
class AesRoundKeys(
    id: String = UUID.randomUUID().toString(),
    val roundKeys: List<AesRoundKey>,
    val numRounds: Int,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
) : BaseEntity<String>(id, createdAt, updatedAt) {
    private companion object {
        private const val AES_128_ROUNDS = 10
        private const val AES_192_ROUNDS = 12
        private const val AES_256_ROUNDS = 14
    }

    init {
        require(roundKeys.size == numRounds + 1) {
            "Number of round keys must be numRounds + 1 (got ${roundKeys.size}, expected ${numRounds + 1})"
        }
        require(
            numRounds == AES_128_ROUNDS ||
                numRounds == AES_192_ROUNDS ||
                numRounds == AES_256_ROUNDS,
        ) {
            "Invalid number of rounds: $numRounds (must be $AES_128_ROUNDS, $AES_192_ROUNDS, or $AES_256_ROUNDS)"
        }
    }

    /**
     * Gets round key at specified index.
     */
    fun getRoundKey(index: Int): AesRoundKey {
        require(index in 0..numRounds) {
            "Round key index out of range: $index (must be 0..$numRounds)"
        }
        return roundKeys[index]
    }

    /**
     * Gets initial round key (for first AddRoundKey).
     */
    fun getInitialRoundKey(): AesRoundKey = roundKeys[0]

    /**
     * Gets final round key (for last AddRoundKey).
     */
    fun getFinalRoundKey(): AesRoundKey = roundKeys[numRounds]

    override fun toString(): String {
        return "AesRoundKeys(id=$id, rounds=$numRounds, keys=${roundKeys.size})"
    }
}
