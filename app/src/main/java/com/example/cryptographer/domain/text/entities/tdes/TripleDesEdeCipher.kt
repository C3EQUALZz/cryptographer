package com.example.cryptographer.domain.text.entities.tdes

import com.example.cryptographer.domain.text.entities.des.DesCipher
import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesKey

/**
 * 3DES EDE block cipher (Encrypt-Decrypt-Encrypt).
 */
internal object TripleDesEdeCipher {
    private const val BLOCK_SIZE = 8

    fun encryptBlock(block: ByteArray, key: TripleDesKey): ByteArray {
        require(block.size == BLOCK_SIZE) { "Block must be exactly $BLOCK_SIZE bytes" }
        return if (key.isTwoKey) {
            encryptBlock112(block, key.key1Raw(), key.key2Raw())
        } else {
            val key3 = requireNotNull(key.key3Raw()) { "Key3 must be provided for 3DES-168" }
            encryptBlock168(block, key.key1Raw(), key.key2Raw(), key3)
        }
    }

    fun decryptBlock(block: ByteArray, key: TripleDesKey): ByteArray {
        require(block.size == BLOCK_SIZE) { "Block must be exactly $BLOCK_SIZE bytes" }
        return if (key.isTwoKey) {
            decryptBlock112(block, key.key1Raw(), key.key2Raw())
        } else {
            val key3 = requireNotNull(key.key3Raw()) { "Key3 must be provided for 3DES-168" }
            decryptBlock168(block, key.key1Raw(), key.key2Raw(), key3)
        }
    }

    private fun encryptBlock112(block: ByteArray, key1: ByteArray, key2: ByteArray): ByteArray {
        var result = DesCipher.encryptBlock(block, key1)
        result = DesCipher.decryptBlock(result, key2)
        result = DesCipher.encryptBlock(result, key1)
        return result
    }

    private fun decryptBlock112(block: ByteArray, key1: ByteArray, key2: ByteArray): ByteArray {
        var result = DesCipher.decryptBlock(block, key1)
        result = DesCipher.encryptBlock(result, key2)
        result = DesCipher.decryptBlock(result, key1)
        return result
    }

    private fun encryptBlock168(block: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray): ByteArray {
        var result = DesCipher.encryptBlock(block, key1)
        result = DesCipher.decryptBlock(result, key2)
        result = DesCipher.encryptBlock(result, key3)
        return result
    }

    private fun decryptBlock168(block: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray): ByteArray {
        var result = DesCipher.decryptBlock(block, key3)
        result = DesCipher.encryptBlock(result, key2)
        result = DesCipher.decryptBlock(result, key1)
        return result
    }
}
