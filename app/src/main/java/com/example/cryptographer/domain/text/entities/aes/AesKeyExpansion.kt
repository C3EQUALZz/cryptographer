package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import com.example.cryptographer.domain.text.valueobjects.aes.AesRoundKey
import com.example.cryptographer.domain.text.valueobjects.aes.AesSBox
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * AES Key Expansion implementation.
 *
 * Generates round keys from the original cipher key.
 * Supports AES-128 (10 rounds), AES-192 (12 rounds), and AES-256 (14 rounds).
 *
 * Follows the Rijndael key expansion algorithm (FIPS 197).
 *
 * This is domain logic representing the AES key expansion algorithm.
 */
internal object AesKeyExpansion {
    private val logger = KotlinLogging.logger {}
    private const val WORDS_IN_BLOCK = 4 // 128-bit block = 4 words

    // Rcon (Round Constants) for key expansion
    private val RCON = byteArrayOf(
        0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40,
        0x80.toByte(), 0x1b.toByte(), 0x36.toByte(),
    )

    /**
     * Expands the cipher key into round keys.
     *
     * Uses domain value objects for type safety and validation.
     *
     * @param key Cipher key (16, 24, or 32 bytes)
     * @param numRounds Number of rounds value object
     * @return AesRoundKeys entity containing all round keys
     */
    fun expandKey(key: ByteArray, numRounds: AesNumRounds): AesRoundKeys {
        logger.trace { "Starting AES key expansion: keySize=${key.size} bytes, numRounds=${numRounds.rounds}" }

        require(key.size == 16 || key.size == 24 || key.size == 32) { "Invalid key size: ${key.size} bytes" }

        val nk = key.size / 4 // Number of 32-bit words in key
        val totalWords = WORDS_IN_BLOCK * (numRounds.rounds + 1) // Total words needed
        logger.trace { "Key expansion parameters: nk=$nk (words in key), totalWords=$totalWords (words needed)" }

        // Convert key bytes to words (32-bit integers)
        logger.trace { "Converting key bytes to 32-bit words" }
        val keyWords = IntArray(totalWords)
        for (i in 0 until nk) {
            keyWords[i] = bytesToWord(key, i * 4)
        }
        logger.trace { "Converted $nk initial words from key" }

        // Expand key words
        logger.trace { "Expanding key words: generating words from $nk to ${totalWords - 1}" }
        for (i in nk until totalWords) {
            var temp = keyWords[i - 1]

            if (i % nk == 0) {
                // Apply RotWord and SubWord, then XOR with Rcon
                logger.trace { "Applying RotWord and SubWord with Rcon for word $i" }
                temp = subWord(rotWord(temp)) xor (RCON[i / nk].toInt() and 0xFF shl 24)
            } else if (nk > 6 && i % nk == 4) {
                // AES-256 only: apply SubWord
                logger.trace { "Applying SubWord (AES-256) for word $i" }
                temp = subWord(temp)
            }

            keyWords[i] = keyWords[i - nk] xor temp
        }
        logger.trace { "Key word expansion completed: generated $totalWords words" }

        // Convert words to round keys (16 bytes each)
        logger.trace { "Converting words to round keys: ${numRounds.rounds + 1} round keys (16 bytes each)" }
        val roundKeys = mutableListOf<AesRoundKey>()
        for (i in 0 until numRounds.rounds + 1) {
            val roundKeyBytes = ByteArray(16)
            for (j in 0 until WORDS_IN_BLOCK) {
                val word = keyWords[i * WORDS_IN_BLOCK + j]
                roundKeyBytes[j * 4] = (word and 0xFF).toByte()
                roundKeyBytes[j * 4 + 1] = ((word shr 8) and 0xFF).toByte()
                roundKeyBytes[j * 4 + 2] = ((word shr 16) and 0xFF).toByte()
                roundKeyBytes[j * 4 + 3] = ((word shr 24) and 0xFF).toByte()
            }
            roundKeys.add(
                AesRoundKey.create(roundKeyBytes).getOrElse {
                    throw IllegalStateException("Failed to create round key at index $i")
                },
            )
        }

        logger.trace {
            "AES key expansion completed: generated ${roundKeys.size} round keys for ${numRounds.rounds} rounds"
        }
        return AesRoundKeys(roundKeys = roundKeys, numRounds = numRounds.rounds)
    }

    /**
     * Rotates a 32-bit word left by one byte (RotWord).
     */
    private fun rotWord(word: Int): Int {
        return ((word and 0xFFFFFF00.toInt()) ushr 8) or ((word and 0x000000FF) shl 24)
    }

    /**
     * Applies S-box substitution to each byte of a 32-bit word (SubWord).
     */
    private fun subWord(word: Int): Int {
        val b0 = (word and 0x000000FF)
        val b1 = (word and 0x0000FF00) ushr 8
        val b2 = (word and 0x00FF0000) ushr 16
        val b3 = (word and 0xFF000000.toInt()) ushr 24

        val s0 = AesSBox.getSBox(b0).toInt() and 0xFF
        val s1 = AesSBox.getSBox(b1).toInt() and 0xFF
        val s2 = AesSBox.getSBox(b2).toInt() and 0xFF
        val s3 = AesSBox.getSBox(b3).toInt() and 0xFF

        return s0 or (s1 shl 8) or (s2 shl 16) or (s3 shl 24)
    }

    /**
     * Converts 4 bytes (little-endian) to a 32-bit word.
     */
    private fun bytesToWord(bytes: ByteArray, offset: Int): Int {
        return (
            (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
            )
    }
}
