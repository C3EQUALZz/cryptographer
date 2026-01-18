package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.text.valueobjects.aes.AesBlock
import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import com.example.cryptographer.domain.text.valueobjects.aes.AesSBox
import com.example.cryptographer.domain.text.valueobjects.aes.AesState
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Core AES (Advanced Encryption Standard) implementation.
 *
 * Implements AES block encryption/decryption operations:
 * - SubBytes: Non-linear substitution using S-box
 * - ShiftRows: Cyclic shift of rows
 * - MixColumns: Linear transformation of columns
 * - AddRoundKey: XOR with round key
 *
 * AES operates on 128-bit blocks with keys of 128, 192, or 256 bits.
 * This follows the Rijndael algorithm specification (FIPS 197).
 *
 * This is domain logic representing the core AES cryptographic algorithm.
 * Uses domain entities (AesState, AesBlock, AesRoundKeys) for type safety and validation.
 */
internal object AesCore {
    private val logger = KotlinLogging.logger {}
    private const val STATE_ROWS = 4
    private const val STATE_COLS = 4
    private const val BYTE_MASK = 0xFF
    private const val HI_BIT_MASK = 0x80
    private const val LOW_BIT_MASK = 0x01
    private const val REDUCTION_POLYNOMIAL = 0x1B
    private const val GF_MUL_2 = 0x02
    private const val GF_MUL_3 = 0x03
    private const val BYTE_BITS = 8
    private const val SHIFT_BY_ONE = 1
    private const val INDEX_0 = 0
    private const val INDEX_1 = 1
    private const val INDEX_2 = 2
    private const val INDEX_3 = 3

    /**
     * Encrypts a single 128-bit block using AES.
     *
     * Uses domain entities for type safety and validation.
     *
     * @param block AES block to encrypt
     * @param roundKeys Round keys entity
     * @param numRounds Number of rounds value object
     * @return Encrypted AES block
     */
    fun encryptBlock(block: AesBlock, roundKeys: AesRoundKeys, numRounds: AesNumRounds): AesBlock {
        logger.trace {
            "Starting AES block encryption: numRounds=${numRounds.rounds}, " +
                "blockSize=${block.bytes.size} bytes"
        }

        require(roundKeys.numRounds == numRounds.rounds) {
            "Round keys rounds (${roundKeys.numRounds}) must match numRounds (${numRounds.rounds})"
        }

        // Convert block to state matrix (column-major order)
        logger.trace { "Converting block to state matrix (column-major order)" }
        var state = AesState.fromBlock(block.bytes)

        // Initial AddRoundKey
        logger.trace { "Applying initial AddRoundKey" }
        state = addRoundKey(state, roundKeys.getInitialRoundKey().bytes)

        // Main rounds
        logger.trace { "Processing main rounds: rounds 1 to ${numRounds.rounds - 1}" }
        for (round in 1 until numRounds.rounds) {
            state = subBytes(state)
            state = shiftRows(state)
            state = mixColumns(state)
            state = addRoundKey(state, roundKeys.getRoundKey(round).bytes)
        }

        // Final round (no MixColumns)
        logger.trace { "Processing final round (no MixColumns)" }
        state = subBytes(state)
        state = shiftRows(state)
        state = addRoundKey(state, roundKeys.getFinalRoundKey().bytes)

        // Convert state matrix back to block
        logger.trace { "Converting state matrix back to block" }
        val encryptedBytes = state.toBlock()

        logger.trace { "AES block encryption completed: encryptedBlockSize=${encryptedBytes.size} bytes" }
        return AesBlock.create(encryptedBytes).getOrThrow()
    }

    /**
     * SubBytes transformation - non-linear byte substitution using S-box.
     *
     * Uses AesState.getByte() and setByte() methods.
     */
    private fun subBytes(state: AesState): AesState {
        var result = state
        for (i in 0 until STATE_ROWS) {
            for (j in 0 until STATE_COLS) {
                val value = result.getByte(i, j).toInt() and BYTE_MASK
                result = result.setByte(i, j, AesSBox.getSBox(value))
            }
        }
        return result
    }

    /**
     * ShiftRows transformation - cyclic left shift of rows.
     * Row 0: no shift, Row 1: 1 byte, Row 2: 2 bytes, Row 3: 3 bytes.
     *
     * Uses AesState.getRow() and setRow() methods.
     */
    private fun shiftRows(state: AesState): AesState {
        var result = state

        // Row 0: no shift (no change needed)

        // Row 1: shift left by 1
        val row1 = result.getRow(INDEX_1)
        val shiftedRow1 = byteArrayOf(row1[INDEX_1], row1[INDEX_2], row1[INDEX_3], row1[INDEX_0])
        result = result.setRow(INDEX_1, shiftedRow1)

        // Row 2: shift left by 2 (swap 0<->2, 1<->3)
        val row2 = result.getRow(INDEX_2)
        val shiftedRow2 = byteArrayOf(row2[INDEX_2], row2[INDEX_3], row2[INDEX_0], row2[INDEX_1])
        result = result.setRow(INDEX_2, shiftedRow2)

        // Row 3: shift left by 3 (shift right by 1)
        val row3 = result.getRow(INDEX_3)
        val shiftedRow3 = byteArrayOf(row3[INDEX_3], row3[INDEX_0], row3[INDEX_1], row3[INDEX_2])
        result = result.setRow(INDEX_3, shiftedRow3)

        return result
    }

    /**
     * MixColumns transformation - multiplication of each column by a fixed matrix in GF(2^8).
     *
     * Uses AesState.getColumn() and setColumn() methods.
     */
    private fun mixColumns(state: AesState): AesState {
        var result = state
        for (c in 0 until STATE_COLS) {
            // Read column using getColumn()
            val column = result.getColumn(c)
            val s0 = column[INDEX_0].toInt() and BYTE_MASK
            val s1 = column[INDEX_1].toInt() and BYTE_MASK
            val s2 = column[INDEX_2].toInt() and BYTE_MASK
            val s3 = column[INDEX_3].toInt() and BYTE_MASK

            // Transform column values
            val newS0 = (gfMul(GF_MUL_2, s0) xor gfMul(GF_MUL_3, s1) xor s2 xor s3).toByte()
            val newS1 = (s0 xor gfMul(GF_MUL_2, s1) xor gfMul(GF_MUL_3, s2) xor s3).toByte()
            val newS2 = (s0 xor s1 xor gfMul(GF_MUL_2, s2) xor gfMul(GF_MUL_3, s3)).toByte()
            val newS3 = (gfMul(GF_MUL_3, s0) xor s1 xor s2 xor gfMul(GF_MUL_2, s3)).toByte()

            // Write transformed values back using setColumn()
            val newColumn = byteArrayOf(newS0, newS1, newS2, newS3)
            result = result.setColumn(c, newColumn)
        }
        return result
    }

    /**
     * AddRoundKey transformation - XOR state with round key.
     *
     * Uses AesState.getByte() and setByte() methods.
     */
    private fun addRoundKey(state: AesState, roundKey: ByteArray): AesState {
        var result = state
        for (c in 0 until STATE_COLS) {
            for (r in 0 until STATE_ROWS) {
                val currentByte = result.getByte(r, c).toInt() and BYTE_MASK
                val keyByte = roundKey[r + STATE_ROWS * c].toInt() and BYTE_MASK
                val newByte = (currentByte xor keyByte).toByte()
                result = result.setByte(r, c, newByte)
            }
        }
        return result
    }

    /**
     * Multiplication in GF(2^8) modulo irreducible polynomial x^8 + x^4 + x^3 + x + 1.
     */
    private fun gfMul(a: Int, b: Int): Int {
        var result = 0
        var aValue = a and BYTE_MASK
        var bValue = b and BYTE_MASK

        for (i in 0 until BYTE_BITS) {
            if ((bValue and LOW_BIT_MASK) != 0) {
                result = result xor aValue
            }
            val hiBitSet = (aValue and HI_BIT_MASK) != 0
            aValue = (aValue shl SHIFT_BY_ONE) and BYTE_MASK
            if (hiBitSet) {
                aValue = aValue xor REDUCTION_POLYNOMIAL // Irreducible polynomial: x^8 + x^4 + x^3 + x + 1
            }
            bValue = bValue shr SHIFT_BY_ONE
        }
        return result
    }
}
