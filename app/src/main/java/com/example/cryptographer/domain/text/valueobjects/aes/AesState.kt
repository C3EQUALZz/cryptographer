package com.example.cryptographer.domain.text.valueobjects.aes

import com.example.cryptographer.domain.common.values.BaseValueObject

/**
 * Value Object for AES state matrix (4x4 bytes = 16 bytes = 128 bits).
 *
 * Represents the internal state of AES during encryption/decryption.
 * AES state is a 4x4 matrix of bytes in column-major order.
 *
 * This is a Value Object following DDD principles - it's immutable.
 */
class AesState private constructor(
    private val state: Array<ByteArray>,
) : BaseValueObject() {
    companion object {
        private const val STATE_ROWS = 4
        private const val STATE_COLS = 4
        private const val BLOCK_SIZE = 16 // bytes (128 bits)

        /**
         * Creates AES state from block bytes (column-major order).
         *
         * @param block 16-byte block
         * @return AesState
         */
        fun fromBlock(block: ByteArray): AesState {
            require(block.size == BLOCK_SIZE) { "Block must be $BLOCK_SIZE bytes" }

            val state = Array(STATE_ROWS) { ByteArray(STATE_COLS) }
            for (c in 0 until STATE_COLS) {
                for (r in 0 until STATE_ROWS) {
                    state[r][c] = block[r + STATE_ROWS * c]
                }
            }
            return AesState(state)
        }
    }

    /**
     * Gets byte at specified position (row, column).
     */
    fun getByte(row: Int, col: Int): Byte {
        require(row in 0 until STATE_ROWS && col in 0 until STATE_COLS) {
            "Invalid position: row=$row, col=$col"
        }
        return state[row][col]
    }

    /**
     * Gets entire row as byte array.
     */
    fun getRow(row: Int): ByteArray {
        require(row in 0 until STATE_ROWS) { "Invalid row: $row" }
        return state[row].copyOf()
    }

    /**
     * Gets entire column as byte array.
     */
    fun getColumn(col: Int): ByteArray {
        require(col in 0 until STATE_COLS) { "Invalid column: $col" }
        val column = ByteArray(STATE_ROWS)
        for (r in 0 until STATE_ROWS) {
            column[r] = state[r][col]
        }
        return column
    }

    /**
     * Sets byte at specified position (returns new state - immutable).
     */
    fun setByte(row: Int, col: Int, value: Byte): AesState {
        require(row in 0 until STATE_ROWS && col in 0 until STATE_COLS) {
            "Invalid position: row=$row, col=$col"
        }
        val newState = state.map { it.copyOf() }.toTypedArray()
        newState[row][col] = value
        return AesState(newState)
    }

    /**
     * Sets entire row (returns new state - immutable).
     */
    fun setRow(row: Int, values: ByteArray): AesState {
        require(row in 0 until STATE_ROWS) { "Invalid row: $row" }
        require(values.size == STATE_COLS) { "Row must have $STATE_COLS bytes" }
        val newState = state.map { it.copyOf() }.toTypedArray()
        System.arraycopy(values, 0, newState[row], 0, STATE_COLS)
        return AesState(newState)
    }

    /**
     * Sets entire column (returns new state - immutable).
     */
    fun setColumn(col: Int, values: ByteArray): AesState {
        require(col in 0 until STATE_COLS) { "Invalid column: $col" }
        require(values.size == STATE_ROWS) { "Column must have $STATE_ROWS bytes" }
        val newState = state.map { it.copyOf() }.toTypedArray()
        for (r in 0 until STATE_ROWS) {
            newState[r][col] = values[r]
        }
        return AesState(newState)
    }

    /**
     * Converts state to block bytes (column-major order).
     */
    fun toBlock(): ByteArray {
        val block = ByteArray(BLOCK_SIZE)
        for (c in 0 until STATE_COLS) {
            for (r in 0 until STATE_ROWS) {
                block[r + STATE_ROWS * c] = state[r][c]
            }
        }
        return block
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AesState) return false
        for (i in 0 until STATE_ROWS) {
            if (!state[i].contentEquals(other.state[i])) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var hash = 0
        for (i in 0 until STATE_ROWS) {
            hash = hash * 31 + state[i].contentHashCode()
        }
        return hash
    }

    override fun toString(): String {
        return "AesState(4x4)"
    }
}
