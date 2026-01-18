package com.example.cryptographer.domain.text.entities.tdes

import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesIv
import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesKey

/**
 * CBC mode for 3DES block cipher.
 */
internal object TripleDesCbcMode {
    private const val BLOCK_SIZE = 8

    fun encrypt(data: ByteArray, key: TripleDesKey, iv: TripleDesIv): ByteArray {
        require(data.size % BLOCK_SIZE == 0) { "Data must be padded to block size" }

        val result = ByteArray(data.size)
        var previousBlock = iv.toByteArray()

        for (i in data.indices step BLOCK_SIZE) {
            val block = data.sliceArray(i until (i + BLOCK_SIZE))
            val xored = xorBlocks(block, previousBlock)
            val encrypted = TripleDesEdeCipher.encryptBlock(xored, key)
            System.arraycopy(encrypted, 0, result, i, BLOCK_SIZE)
            previousBlock = encrypted
        }

        return result
    }

    fun decrypt(data: ByteArray, key: TripleDesKey, iv: TripleDesIv): ByteArray {
        require(data.size % BLOCK_SIZE == 0) { "Data size must be multiple of block size" }

        val result = ByteArray(data.size)
        var previousBlock = iv.toByteArray()

        for (i in data.indices step BLOCK_SIZE) {
            val block = data.sliceArray(i until (i + BLOCK_SIZE))
            val decrypted = TripleDesEdeCipher.decryptBlock(block, key)
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
