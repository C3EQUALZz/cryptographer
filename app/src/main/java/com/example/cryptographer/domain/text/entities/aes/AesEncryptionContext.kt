package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.common.entities.BaseEntity
import com.example.cryptographer.domain.text.valueobjects.aes.AesKeySize
import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import java.time.Instant
import java.util.UUID

/**
 * Entity representing AES encryption context.
 *
 * Encapsulates all parameters needed for AES encryption/decryption:
 * - Key size and algorithm
 * - Number of rounds
 * - Round keys (derived from cipher key)
 *
 * This is a Domain Entity following DDD principles - it has identity and lifecycle.
 */
class AesEncryptionContext(
    id: String = UUID.randomUUID().toString(),
    val keySize: AesKeySize,
    val numRounds: AesNumRounds,
    val roundKeys: AesRoundKeys,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
) : BaseEntity<String>(id, createdAt, updatedAt) {
    init {
        require(keySize.numRounds == numRounds.rounds) {
            "Key size rounds (${keySize.numRounds}) must match numRounds (${numRounds.rounds})"
        }
        require(roundKeys.numRounds == numRounds.rounds) {
            "Round keys rounds (${roundKeys.numRounds}) must match numRounds (${numRounds.rounds})"
        }
    }

    companion object {
        /**
         * Creates encryption context from key size.
         * Round keys must be provided separately through key expansion.
         */
        fun create(keySize: AesKeySize, roundKeys: AesRoundKeys): Result<AesEncryptionContext> {
            return AesNumRounds.create(keySize.algorithm).map { numRounds ->
                AesEncryptionContext(
                    keySize = keySize,
                    numRounds = numRounds,
                    roundKeys = roundKeys,
                )
            }
        }
    }

    override fun toString(): String {
        return "AesEncryptionContext(id=$id, algorithm=${keySize.algorithm}, rounds=${numRounds.rounds})"
    }
}
