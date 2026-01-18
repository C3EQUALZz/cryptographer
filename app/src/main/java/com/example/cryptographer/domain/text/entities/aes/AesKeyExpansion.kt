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
    private const val BYTES_PER_WORD = 4
    private const val BITS_IN_BYTE = 8
    private const val BYTE_MASK = 0xFF
    private const val ROUND_KEY_SIZE_BYTES = WORDS_IN_BLOCK * BYTES_PER_WORD
    private const val KEY_SIZE_128_BYTES = ROUND_KEY_SIZE_BYTES
    private const val KEY_SIZE_192_BYTES = 24
    private const val KEY_SIZE_256_BYTES = 32
    private const val WORD_MASK_24 = 0x00FFFFFF
    private const val AES_192_NK_WORDS = 6
    private const val AES_256_SUBWORD_INDEX = 4
    private const val MSB_SHIFT = BITS_IN_BYTE * (BYTES_PER_WORD - 1)
    private const val RCON_SHIFT = 0

    private const val RCON_00 = 0x00
    private const val RCON_01 = 0x01
    private const val RCON_02 = 0x02
    private const val RCON_04 = 0x04
    private const val RCON_08 = 0x08
    private const val RCON_10 = 0x10
    private const val RCON_20 = 0x20
    private const val RCON_40 = 0x40
    private const val RCON_80 = 0x80
    private const val RCON_1B = 0x1B
    private const val RCON_36 = 0x36

    // Rcon (Round Constants) for key expansion
    private val RCON = byteArrayOf(
        RCON_00.toByte(),
        RCON_01.toByte(),
        RCON_02.toByte(),
        RCON_04.toByte(),
        RCON_08.toByte(),
        RCON_10.toByte(),
        RCON_20.toByte(),
        RCON_40.toByte(),
        RCON_80.toByte(),
        RCON_1B.toByte(),
        RCON_36.toByte(),
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

        require(
            key.size == KEY_SIZE_128_BYTES ||
                key.size == KEY_SIZE_192_BYTES ||
                key.size == KEY_SIZE_256_BYTES,
        ) { "Invalid key size: ${key.size} bytes" }

        val nk = key.size / BYTES_PER_WORD // Number of 32-bit words in key
        val totalWords = WORDS_IN_BLOCK * (numRounds.rounds + 1) // Total words needed
        logger.trace { "Key expansion parameters: nk=$nk (words in key), totalWords=$totalWords (words needed)" }

        // Convert key bytes to words (32-bit integers)
        logger.trace { "Converting key bytes to 32-bit words" }
        val keyWords = IntArray(totalWords)
        for (i in 0 until nk) {
            keyWords[i] = bytesToWord(key, i * BYTES_PER_WORD)
        }
        logger.trace { "Converted $nk initial words from key" }

        // Expand key words
        logger.trace { "Expanding key words: generating words from $nk to ${totalWords - 1}" }
        for (i in nk until totalWords) {
            val temp = transformWord(i, nk, keyWords[i - 1])
            keyWords[i] = keyWords[i - nk] xor temp
        }
        logger.trace { "Key word expansion completed: generated $totalWords words" }

        // Convert words to round keys (16 bytes each)
        logger.trace { "Converting words to round keys: ${numRounds.rounds + 1} round keys (16 bytes each)" }
        val roundKeys = mutableListOf<AesRoundKey>()
        for (i in 0 until numRounds.rounds + 1) {
            val roundKeyBytes = ByteArray(ROUND_KEY_SIZE_BYTES)
            for (j in 0 until WORDS_IN_BLOCK) {
                val word = keyWords[i * WORDS_IN_BLOCK + j]
                writeWordToBytes(word, roundKeyBytes, j * BYTES_PER_WORD)
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

    private fun transformWord(index: Int, nk: Int, previousWord: Int): Int {
        return when {
            index % nk == 0 -> {
                // Apply RotWord and SubWord, then XOR with Rcon
                logger.trace { "Applying RotWord and SubWord with Rcon for word $index" }
                subWord(rotWord(previousWord)) xor (RCON[index / nk].toInt() and BYTE_MASK shl RCON_SHIFT)
            }
            nk > AES_192_NK_WORDS && index % nk == AES_256_SUBWORD_INDEX -> {
                // AES-256 only: apply SubWord
                logger.trace { "Applying SubWord (AES-256) for word $index" }
                subWord(previousWord)
            }
            else -> previousWord
        }
    }

    /**
     * Rotates a 32-bit word left by one byte (RotWord).
     */
    private fun rotWord(word: Int): Int {
        return ((word ushr BITS_IN_BYTE) and WORD_MASK_24) or ((word and BYTE_MASK) shl MSB_SHIFT)
    }

    /**
     * Applies S-box substitution to each byte of a 32-bit word (SubWord).
     */
    private fun subWord(word: Int): Int {
        var result = 0
        for (i in 0 until BYTES_PER_WORD) {
            val byteValue = (word ushr (i * BITS_IN_BYTE)) and BYTE_MASK
            val substituted = AesSBox.getSBox(byteValue).toInt() and BYTE_MASK
            result = result or (substituted shl (i * BITS_IN_BYTE))
        }
        return result
    }

    /**
     * Converts 4 bytes (little-endian) to a 32-bit word.
     */
    private fun bytesToWord(bytes: ByteArray, offset: Int): Int {
        var word = 0
        for (i in 0 until BYTES_PER_WORD) {
            word = word or ((bytes[offset + i].toInt() and BYTE_MASK) shl (i * BITS_IN_BYTE))
        }
        return word
    }

    /**
     * Writes a 32-bit word as 4 bytes (little-endian) into a buffer.
     */
    private fun writeWordToBytes(word: Int, buffer: ByteArray, offset: Int) {
        for (i in 0 until BYTES_PER_WORD) {
            buffer[offset + i] = ((word ushr (i * BITS_IN_BYTE)) and BYTE_MASK).toByte()
        }
    }
}
