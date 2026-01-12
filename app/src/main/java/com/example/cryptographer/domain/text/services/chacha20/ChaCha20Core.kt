package com.example.cryptographer.domain.text.services.chacha20

/**
 * Core ChaCha20 function implementation.
 * Implements the ChaCha20 block function as specified in RFC 8439.
 *
 * This class follows Single Responsibility Principle - it only handles
 * the core ChaCha20 block transformation.
 */
internal object ChaCha20Core {
    private const val STATE_SIZE = 16 // 16 words = 64 bytes
    private const val ROUNDS = 20

    /**
     * Performs ChaCha20 quarter round operation.
     * This is the basic building block of ChaCha20.
     *
     * @param state The state array (16 words)
     * @param a, b, c, d Indices in the state array
     */
    private fun quarterRound(state: IntArray, a: Int, b: Int, c: Int, d: Int) {
        state[a] = state[a] + state[b]
        state[d] = state[d] xor state[a]
        state[d] = state[d] shl 16 or (state[d] ushr 16)

        state[c] = state[c] + state[d]
        state[b] = state[b] xor state[c]
        state[b] = state[b] shl 12 or (state[b] ushr 20)

        state[a] = state[a] + state[b]
        state[d] = state[d] xor state[a]
        state[d] = state[d] shl 8 or (state[d] ushr 24)

        state[c] = state[c] + state[d]
        state[b] = state[b] xor state[c]
        state[b] = state[b] shl 7 or (state[b] ushr 25)
    }

    /**
     * Performs ChaCha20 block function.
     * Generates a keystream block from the initial state.
     *
     * @param input Initial state (16 words = 64 bytes)
     * @return Keystream block (64 bytes)
     */
    fun block(input: ByteArray): ByteArray {
        require(input.size == STATE_SIZE * 4) {
            "Input must be exactly ${STATE_SIZE * 4} bytes (16 words)"
        }

        // Convert input bytes to words (little-endian)
        val state = IntArray(STATE_SIZE)
        for (i in 0 until STATE_SIZE) {
            val offset = i * 4
            state[i] = (input[offset].toInt() and 0xFF) or
                ((input[offset + 1].toInt() and 0xFF) shl 8) or
                ((input[offset + 2].toInt() and 0xFF) shl 16) or
                ((input[offset + 3].toInt() and 0xFF) shl 24)
        }

        // Working state
        val workingState = state.copyOf()

        // Perform 20 rounds (10 column rounds + 10 diagonal rounds)
        repeat(ROUNDS / 2) {
            // Column rounds
            quarterRound(workingState, 0, 4, 8, 12)
            quarterRound(workingState, 1, 5, 9, 13)
            quarterRound(workingState, 2, 6, 10, 14)
            quarterRound(workingState, 3, 7, 11, 15)

            // Diagonal rounds
            quarterRound(workingState, 0, 5, 10, 15)
            quarterRound(workingState, 1, 6, 11, 12)
            quarterRound(workingState, 2, 7, 8, 13)
            quarterRound(workingState, 3, 4, 9, 14)
        }

        // Add original state to working state
        for (i in 0 until STATE_SIZE) {
            workingState[i] = workingState[i] + state[i]
        }

        // Convert words back to bytes (little-endian)
        val output = ByteArray(STATE_SIZE * 4)
        for (i in 0 until STATE_SIZE) {
            val offset = i * 4
            val word = workingState[i]
            output[offset] = (word and 0xFF).toByte()
            output[offset + 1] = ((word ushr 8) and 0xFF).toByte()
            output[offset + 2] = ((word ushr 16) and 0xFF).toByte()
            output[offset + 3] = ((word ushr 24) and 0xFF).toByte()
        }

        return output
    }

    /**
     * Creates the initial state for ChaCha20.
     * Format: constants (4 words) | key (8 words) | counter (1 word) | nonce (3 words)
     *
     * @param key 32-byte key (256 bits)
     * @param nonce 12-byte nonce (96 bits)
     * @param counter Block counter (32-bit)
     * @return Initial state (64 bytes)
     */
    fun createInitialState(key: ByteArray, nonce: ByteArray, counter: Int): ByteArray {
        require(key.size == 32) { "Key must be exactly 32 bytes" }
        require(nonce.size == 12) { "Nonce must be exactly 12 bytes" }

        val state = ByteArray(64)

        // Constants: "expand 32-byte k" (RFC 8439)
        state[0] = 0x65.toByte() // 'e'
        state[1] = 0x78.toByte() // 'x'
        state[2] = 0x70.toByte() // 'p'
        state[3] = 0x61.toByte() // 'a'
        state[4] = 0x6E.toByte() // 'n'
        state[5] = 0x64.toByte() // 'd'
        state[6] = 0x20.toByte() // ' '
        state[7] = 0x33.toByte() // '3'
        state[8] = 0x32.toByte() // '2'
        state[9] = 0x2D.toByte() // '-'
        state[10] = 0x62.toByte() // 'b'
        state[11] = 0x79.toByte() // 'y'
        state[12] = 0x74.toByte() // 't'
        state[13] = 0x65.toByte() // 'e'
        state[14] = 0x20.toByte() // ' '
        state[15] = 0x6B.toByte() // 'k'

        // Key (32 bytes = 8 words, starting at offset 16)
        key.copyInto(state, 16, 0, 32)

        // Counter (1 word, at offset 48)
        state[48] = (counter and 0xFF).toByte()
        state[49] = ((counter ushr 8) and 0xFF).toByte()
        state[50] = ((counter ushr 16) and 0xFF).toByte()
        state[51] = ((counter ushr 24) and 0xFF).toByte()

        // Nonce (12 bytes = 3 words, starting at offset 52)
        nonce.copyInto(state, 52, 0, 12)

        return state
    }
}
