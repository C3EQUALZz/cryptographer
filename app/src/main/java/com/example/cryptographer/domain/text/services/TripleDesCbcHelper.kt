package com.example.cryptographer.domain.text.services

/**
 * Helper object for 3DES CBC mode operations.
 * Extracted to reduce function count in TripleDesEncryptionService.
 */
internal object TripleDesCbcHelper {
    private const val BLOCK_SIZE = 8 // DES block size

    /**
     * Encrypts data using 3DES-112 in CBC mode.
     * Data must already be padded to block size.
     */
    fun encryptCbc112(data: ByteArray, key1: ByteArray, key2: ByteArray, iv: ByteArray): ByteArray {
        require(data.size % BLOCK_SIZE == 0) { "Data must be padded to block size" }

        val result = ByteArray(data.size)
        var previousBlock = iv.copyOf()

        // Process each block
        for (i in data.indices step BLOCK_SIZE) {
            val block = data.sliceArray(i until (i + BLOCK_SIZE))

            // XOR with previous ciphertext (or IV for first block)
            val xored = xorBlocks(block, previousBlock)

            // Encrypt single block
            val encrypted = TripleDesBlockHelper.encryptBlock112(xored, key1, key2)
            System.arraycopy(encrypted, 0, result, i, BLOCK_SIZE)
            previousBlock = encrypted
        }

        return result
    }

    /**
     * Decrypts data using 3DES-112 in CBC mode.
     */
    fun decryptCbc112(data: ByteArray, key1: ByteArray, key2: ByteArray, iv: ByteArray): ByteArray {
        require(data.size % BLOCK_SIZE == 0) { "Data size must be multiple of block size" }

        val result = ByteArray(data.size)
        var previousBlock = iv.copyOf()

        // Process each block
        for (i in data.indices step BLOCK_SIZE) {
            val block = data.sliceArray(i until (i + BLOCK_SIZE))

            // Decrypt single block
            val decrypted = TripleDesBlockHelper.decryptBlock112(block, key1, key2)

            // XOR with previous ciphertext (or IV for first block)
            val xored = xorBlocks(decrypted, previousBlock)
            System.arraycopy(xored, 0, result, i, BLOCK_SIZE)

            previousBlock = block
        }

        return result
    }

    /**
     * Encrypts data using 3DES-168 in CBC mode.
     * Data must already be padded to block size.
     */
    fun encryptCbc168(data: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray, iv: ByteArray): ByteArray {
        require(data.size % BLOCK_SIZE == 0) { "Data must be padded to block size" }

        val result = ByteArray(data.size)
        var previousBlock = iv.copyOf()

        // Process each block
        for (i in data.indices step BLOCK_SIZE) {
            val block = data.sliceArray(i until (i + BLOCK_SIZE))

            // XOR with previous ciphertext (or IV for first block)
            val xored = xorBlocks(block, previousBlock)

            // Encrypt single block
            val encrypted = TripleDesBlockHelper.encryptBlock168(xored, key1, key2, key3)
            System.arraycopy(encrypted, 0, result, i, BLOCK_SIZE)
            previousBlock = encrypted
        }

        return result
    }

    /**
     * Decrypts data using 3DES-168 in CBC mode.
     */
    fun decryptCbc168(data: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray, iv: ByteArray): ByteArray {
        require(data.size % BLOCK_SIZE == 0) { "Data size must be multiple of block size" }

        val result = ByteArray(data.size)
        var previousBlock = iv.copyOf()

        // Process each block
        for (i in data.indices step BLOCK_SIZE) {
            val block = data.sliceArray(i until (i + BLOCK_SIZE))

            // Decrypt single block
            val decrypted = TripleDesBlockHelper.decryptBlock168(block, key1, key2, key3)

            // XOR with previous ciphertext (or IV for first block)
            val xored = xorBlocks(decrypted, previousBlock)
            System.arraycopy(xored, 0, result, i, BLOCK_SIZE)

            previousBlock = block
        }

        return result
    }

    private fun xorBlocks(block1: ByteArray, block2: ByteArray): ByteArray {
        val result = ByteArray(BLOCK_SIZE)
        for (i in 0 until BLOCK_SIZE) {
            result[i] = (block1[i].toInt() xor block2[i].toInt()).toByte()
        }
        return result
    }
}
