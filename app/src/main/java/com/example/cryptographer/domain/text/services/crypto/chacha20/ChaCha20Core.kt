package com.example.cryptographer.domain.text.services.crypto.chacha20

/**
 * Core ChaCha20 block function implementation.
 *
 * Implements the ChaCha20 quarter round and block function as specified in RFC 8439.
 * This is a pure function with no side effects, following the Single Responsibility Principle.
 *
 * ChaCha20 uses a 512-bit (64-byte) block and operates on 32-bit words.
 * The block function takes:
 * - 256-bit (32-byte) key
 * - 32-bit (4-byte) block counter
 * - 96-bit (12-byte) nonce
 * - 128-bit (16-byte) constant
 */
internal object ChaCha20Core {
    private const val BLOCK_SIZE = 64 // bytes (512 bits)
    private const val STATE_SIZE = 16 // 32-bit words

    // Constants from RFC 8439: "expand 32-byte k"
    private val CONSTANTS = byteArrayOf(
        0x65, 0x78, 0x70, 0x61, // "expa"
        0x6E, 0x64, 0x20, 0x33, // "nd 3"
        0x32, 0x2D, 0x62, 0x79, // "2-by"
        0x74, 0x65, 0x20, 0x6B, // "te k"
    )

    /**
     * Performs a ChaCha20 quarter round on four 32-bit words.
     *
     * @param a First word index
     * @param b Second word index
     * @param c Third word index
     * @param d Fourth word index
     * @param state State array (16 words)
     */
    private fun quarterRound(a: Int, b: Int, c: Int, d: Int, state: IntArray) {
        state[a] = state[a].plus(state[b]) and 0xFFFFFFFF.toInt()
        state[d] = state[d] xor state[a]
        state[d] = (state[d] shl 16) or (state[d] shr 16)

        state[c] = state[c].plus(state[d]) and 0xFFFFFFFF.toInt()
        state[b] = state[b] xor state[c]
        state[b] = (state[b] shl 12) or (state[b] shr 20)

        state[a] = state[a].plus(state[b]) and 0xFFFFFFFF.toInt()
        state[d] = state[d] xor state[a]
        state[d] = (state[d] shl 8) or (state[d] shr 24)

        state[c] = state[c].plus(state[d]) and 0xFFFFFFFF.toInt()
        state[b] = state[b] xor state[c]
        state[b] = (state[b] shl 7) or (state[b] shr 25)
    }

    /**
     * Performs the ChaCha20 block function.
     *
     * Generates 64 bytes of key stream from:
     * - key: 32 bytes (256 bits)
     * - counter: 4 bytes (32 bits, block counter)
     * - nonce: 12 bytes (96 bits)
     *
     * @param key 32-byte key
     * @param counter Block counter (32-bit)
     * @param nonce 12-byte nonce
     * @return 64-byte key stream block
     */
    fun block(key: ByteArray, counter: Int, nonce: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes" }
        require(nonce.size == 12) { "Nonce must be 12 bytes" }

        // Initialize state
        val state = IntArray(STATE_SIZE)

        // First row: constants
        state[0] = bytesToInt(CONSTANTS, 0)
        state[1] = bytesToInt(CONSTANTS, 4)
        state[2] = bytesToInt(CONSTANTS, 8)
        state[3] = bytesToInt(CONSTANTS, 12)

        // Second row: key
        state[4] = bytesToInt(key, 0)
        state[5] = bytesToInt(key, 4)
        state[6] = bytesToInt(key, 8)
        state[7] = bytesToInt(key, 12)

        // Third row: key (continued)
        state[8] = bytesToInt(key, 16)
        state[9] = bytesToInt(key, 20)
        state[10] = bytesToInt(key, 24)
        state[11] = bytesToInt(key, 28)

        // Fourth row: counter and nonce
        state[12] = counter
        state[13] = bytesToInt(nonce, 0)
        state[14] = bytesToInt(nonce, 4)
        state[15] = bytesToInt(nonce, 8)

        // Save initial state
        val workingState = state.copyOf()

        // 20 rounds: 10 column rounds followed by 10 diagonal rounds
        repeat(10) {
            // Column rounds
            quarterRound(0, 4, 8, 12, workingState)
            quarterRound(1, 5, 9, 13, workingState)
            quarterRound(2, 6, 10, 14, workingState)
            quarterRound(3, 7, 11, 15, workingState)

            // Diagonal rounds
            quarterRound(0, 5, 10, 15, workingState)
            quarterRound(1, 6, 11, 12, workingState)
            quarterRound(2, 7, 8, 13, workingState)
            quarterRound(3, 4, 9, 14, workingState)
        }

        // Add initial state to working state
        for (i in 0 until STATE_SIZE) {
            workingState[i] = workingState[i].plus(state[i]) and 0xFFFFFFFF.toInt()
        }

        // Convert state to byte array
        val output = ByteArray(BLOCK_SIZE)
        for (i in 0 until STATE_SIZE) {
            intToBytes(workingState[i], output, i * 4)
        }

        return output
    }

    /**
     * Converts 4 bytes (little-endian) to a 32-bit integer.
     */
    private fun bytesToInt(bytes: ByteArray, offset: Int): Int {
        return (
            (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
            )
    }

    /**
     * Converts a 32-bit integer to 4 bytes (little-endian).
     */
    private fun intToBytes(value: Int, bytes: ByteArray, offset: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }
}
