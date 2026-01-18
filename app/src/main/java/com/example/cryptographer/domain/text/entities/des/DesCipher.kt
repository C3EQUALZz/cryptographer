package com.example.cryptographer.domain.text.entities.des

/**
 * Core DES (Data Encryption Standard) implementation.
 *
 * DES operates on 64-bit blocks with a 56-bit key (64 bits with parity).
 * This is a pure implementation following the DES specification.
 *
 * Note: DES is considered insecure for modern applications due to its 56-bit key size.
 * This implementation is provided for educational purposes and compatibility.
 */
internal object DesCipher {
    private const val BLOCK_SIZE = 8 // bytes (64 bits)
    private const val KEY_SIZE = 8 // bytes (64 bits, but only 56 bits are used)
    private const val ROUNDS = 16

    // Initial Permutation (IP) table
    private val IP = intArrayOf(
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7,
    )

    // Final Permutation (FP) table (inverse of IP)
    private val FP = intArrayOf(
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25,
    )

    // Permuted Choice 1 (PC1) - selects 56 bits from 64-bit key
    private val PC1 = intArrayOf(
        57, 49, 41, 33, 25, 17, 9,
        1, 58, 50, 42, 34, 26, 18,
        10, 2, 59, 51, 43, 35, 27,
        19, 11, 3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15,
        7, 62, 54, 46, 38, 30, 22,
        14, 6, 61, 53, 45, 37, 29,
        21, 13, 5, 28, 20, 12, 4,
    )

    // Permuted Choice 2 (PC2) - selects 48 bits from 56-bit key
    private val PC2 = intArrayOf(
        14, 17, 11, 24, 1, 5,
        3, 28, 15, 6, 21, 10,
        23, 19, 12, 4, 26, 8,
        16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55,
        30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53,
        46, 42, 50, 36, 29, 32,
    )

    // Left shifts for key schedule
    private val KEY_SHIFTS = intArrayOf(1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1)

    // Expansion permutation (E) - expands 32 bits to 48 bits
    private val E = intArrayOf(
        32, 1, 2, 3, 4, 5,
        4, 5, 6, 7, 8, 9,
        8, 9, 10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        24, 25, 26, 27, 28, 29,
        28, 29, 30, 31, 32, 1,
    )

    // Permutation function (P) - permutes 32 bits
    private val P = intArrayOf(
        16, 7, 20, 21,
        29, 12, 28, 17,
        1, 15, 23, 26,
        5, 18, 31, 10,
        2, 8, 24, 14,
        32, 27, 3, 9,
        19, 13, 30, 6,
        22, 11, 4, 25,
    )

    // S-boxes (8 substitution boxes, each 4x16)
    private val S_BOXES = arrayOf(
        // S1
        arrayOf(
            intArrayOf(14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7),
            intArrayOf(0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8),
            intArrayOf(4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0),
            intArrayOf(15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13),
        ),
        // S2
        arrayOf(
            intArrayOf(15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10),
            intArrayOf(3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5),
            intArrayOf(0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15),
            intArrayOf(13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9),
        ),
        // S3
        arrayOf(
            intArrayOf(10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8),
            intArrayOf(13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1),
            intArrayOf(13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7),
            intArrayOf(1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12),
        ),
        // S4
        arrayOf(
            intArrayOf(7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15),
            intArrayOf(13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9),
            intArrayOf(10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4),
            intArrayOf(3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14),
        ),
        // S5
        arrayOf(
            intArrayOf(2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9),
            intArrayOf(14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6),
            intArrayOf(4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14),
            intArrayOf(11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3),
        ),
        // S6
        arrayOf(
            intArrayOf(12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11),
            intArrayOf(10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8),
            intArrayOf(9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6),
            intArrayOf(4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13),
        ),
        // S7
        arrayOf(
            intArrayOf(4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1),
            intArrayOf(13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6),
            intArrayOf(1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2),
            intArrayOf(6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12),
        ),
        // S8
        arrayOf(
            intArrayOf(13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7),
            intArrayOf(1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2),
            intArrayOf(7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8),
            intArrayOf(2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11),
        ),
    )

    /**
     * Encrypts a single 64-bit block using DES.
     *
     * @param block 8-byte block to encrypt
     * @param key 8-byte DES key (56 bits effective)
     * @return Encrypted 8-byte block
     */
    fun encryptBlock(block: ByteArray, key: ByteArray): ByteArray {
        require(block.size == BLOCK_SIZE) { "Block must be exactly $BLOCK_SIZE bytes" }
        require(key.size == KEY_SIZE) { "Key must be exactly $KEY_SIZE bytes" }

        val roundKeys = generateRoundKeys(key)
        return processBlock(block, roundKeys, encrypt = true)
    }

    /**
     * Decrypts a single 64-bit block using DES.
     *
     * @param block 8-byte block to decrypt
     * @param key 8-byte DES key (56 bits effective)
     * @return Decrypted 8-byte block
     */
    fun decryptBlock(block: ByteArray, key: ByteArray): ByteArray {
        require(block.size == BLOCK_SIZE) { "Block must be exactly $BLOCK_SIZE bytes" }
        require(key.size == KEY_SIZE) { "Key must be exactly $KEY_SIZE bytes" }

        val roundKeys = generateRoundKeys(key)
        return processBlock(block, roundKeys, encrypt = false)
    }

    /**
     * Generates 16 round keys from the main key.
     */
    private fun generateRoundKeys(key: ByteArray): Array<ByteArray> {
        // Apply PC1 to get 56-bit key (remove parity bits)
        val key56 = permute(key, PC1, 64, 56)

        // Split into left and right halves (28 bits each)
        // key56 is 7 bytes (56 bits), we need to split into two 28-bit halves
        // Each 28-bit half is stored in 4 bytes (with 4 unused bits in the last byte)
        val left = ByteArray(4)
        val right = ByteArray(4)

        // Left half: first 28 bits (bits 0-27 from key56)
        // Copy first 3 bytes (24 bits)
        left[0] = key56[0]
        left[1] = key56[1]
        left[2] = key56[2]
        // Copy upper 4 bits from 4th byte (bits 24-27)
        left[3] = (key56[3].toInt() and 0xF0).toByte()

        // Right half: last 28 bits (bits 28-55 from key56)
        // Extract bits 28-55 and pack into 4 bytes
        // Bit 28-31: lower 4 bits of key56[3]
        // Bit 32-39: key56[4]
        // Bit 40-47: key56[5]
        // Bit 48-55: key56[6]
        right[0] = (((key56[3].toInt() and 0x0F) shl 4) or ((key56[4].toInt() and 0xF0) shr 4)).toByte()
        right[1] = (((key56[4].toInt() and 0x0F) shl 4) or ((key56[5].toInt() and 0xF0) shr 4)).toByte()
        right[2] = (((key56[5].toInt() and 0x0F) shl 4) or ((key56[6].toInt() and 0xF0) shr 4)).toByte()
        right[3] = ((key56[6].toInt() and 0x0F) shl 4).toByte()

        val roundKeys = Array(ROUNDS) { ByteArray(6) } // 48 bits = 6 bytes

        // Generate keys for each round
        for (round in 0 until ROUNDS) {
            // Left shift both halves
            leftShift(left, KEY_SHIFTS[round])
            leftShift(right, KEY_SHIFTS[round])

            // Combine and apply PC2
            val combined = left + right
            val roundKey = permute(combined, PC2, 56, 48)
            System.arraycopy(roundKey, 0, roundKeys[round], 0, 6)
        }

        return roundKeys
    }

    /**
     * Processes a block through DES encryption or decryption.
     */
    private fun processBlock(block: ByteArray, roundKeys: Array<ByteArray>, encrypt: Boolean): ByteArray {
        // Apply Initial Permutation
        var state = permute(block, IP, 64, 64)

        // Split into left and right halves (32 bits each)
        var left = state.sliceArray(0 until 4)
        var right = state.sliceArray(4 until 8)

        // 16 rounds of Feistel network
        for (round in 0 until ROUNDS) {
            val roundKey = if (encrypt) roundKeys[round] else roundKeys[ROUNDS - 1 - round]
            val newRight = feistelFunction(right, roundKey)
            val newLeft = xorBytes(left, newRight)

            left = right
            right = newLeft
        }

        // Combine (swap left and right after final round)
        val combined = right + left

        // Apply Final Permutation
        return permute(combined, FP, 64, 64)
    }

    /**
     * Feistel function (F function) used in each round.
     */
    private fun feistelFunction(right: ByteArray, roundKey: ByteArray): ByteArray {
        // Expand 32 bits to 48 bits
        val expanded = permute(right, E, 32, 48)

        // XOR with round key
        val xored = xorBytes(expanded, roundKey)

        // Apply S-boxes (8 S-boxes, each processes 6 bits -> 4 bits)
        val sboxOutput = ByteArray(4) // 32 bits = 4 bytes
        for (i in 0 until 8) {
            // Extract 6 bits for this S-box
            val byteIndex = i * 6 / 8
            val bitOffset = (i * 6) % 8
            val input6 = if (bitOffset <= 2) {
                // All 6 bits in one byte
                ((xored[byteIndex].toInt() and 0xFF) shr (2 - bitOffset)) and 0x3F
            } else {
                // Bits span two bytes
                val firstByte = (xored[byteIndex].toInt() and 0xFF) shl (bitOffset - 2)
                val secondByte = (xored[byteIndex + 1].toInt() and 0xFF) shr (10 - bitOffset)
                (firstByte or secondByte) and 0x3F
            }

            // Extract row (bits 0 and 5) and column (bits 1-4)
            val row = ((input6 and 0x20) shr 4) or (input6 and 0x01)
            val col = (input6 shr 1) and 0x0F
            val sboxValue = S_BOXES[i][row][col]

            // Write 4-bit value to output
            val outputByteIndex = i / 2
            if (i % 2 == 0) {
                sboxOutput[outputByteIndex] = (sboxValue shl 4).toByte()
            } else {
                sboxOutput[outputByteIndex] = (sboxOutput[outputByteIndex].toInt() or sboxValue).toByte()
            }
        }

        // Apply permutation P
        return permute(sboxOutput, P, 32, 32)
    }

    /**
     * Permutes bits according to a permutation table.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun permute(input: ByteArray, table: IntArray, inputBits: Int, outputBits: Int): ByteArray {
        val output = ByteArray((outputBits + 7) / 8)
        for (i in table.indices) {
            val bitPos = table[i] - 1 // Table is 1-indexed
            val inputByte = bitPos / 8
            val inputBit = 7 - (bitPos % 8)

            val bit = (input[inputByte].toInt() shr inputBit) and 0x01
            val outputByte = i / 8
            val outputBit = 7 - (i % 8)

            output[outputByte] = (output[outputByte].toInt() or (bit shl outputBit)).toByte()
        }
        return output
    }

    /**
     * XORs two byte arrays.
     */
    private fun xorBytes(a: ByteArray, b: ByteArray): ByteArray {
        require(a.size == b.size) { "Byte arrays must have the same size" }
        return ByteArray(a.size) { i -> (a[i].toInt() xor b[i].toInt()).toByte() }
    }

    /**
     * Left shifts a 28-bit array (stored in 4 bytes, but only 28 bits are used).
     */
    private fun leftShift(array: ByteArray, shifts: Int) {
        require(array.size == 4) { "Array must be 4 bytes for 28-bit value" }

        // Extract 28 bits from 4 bytes
        var bits = 0L
        bits = bits or ((array[0].toInt() and 0xFF).toLong() shl 20)
        bits = bits or ((array[1].toInt() and 0xFF).toLong() shl 12)
        bits = bits or ((array[2].toInt() and 0xFF).toLong() shl 4)
        bits = bits or ((array[3].toInt() and 0xFF).toLong() shr 4)
        bits = bits and 0x0FFFFFFF

        // Circular left shift
        bits = ((bits shl shifts) or (bits shr (28 - shifts))) and 0x0FFFFFFF

        // Write back to 4 bytes
        array[0] = ((bits shr 20) and 0xFF).toByte()
        array[1] = ((bits shr 12) and 0xFF).toByte()
        array[2] = ((bits shr 4) and 0xFF).toByte()
        array[3] = ((bits shl 4) and 0xF0).toByte()
    }
}
