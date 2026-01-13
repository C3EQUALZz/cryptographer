package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.text.services.crypto.des.DesCore

/**
 * Helper object for 3DES block encryption/decryption operations.
 * Extracted to reduce function count in TripleDesEncryptionService.
 */
internal object TripleDesBlockHelper {
    /**
     * Encrypts a single block using 3DES-112.
     */
    fun encryptBlock112(block: ByteArray, key1: ByteArray, key2: ByteArray): ByteArray {
        var result = DesCore.encryptBlock(block, key1)
        result = DesCore.decryptBlock(result, key2)
        result = DesCore.encryptBlock(result, key1)
        return result
    }

    /**
     * Decrypts a single block using 3DES-112.
     */
    fun decryptBlock112(block: ByteArray, key1: ByteArray, key2: ByteArray): ByteArray {
        var result = DesCore.decryptBlock(block, key1)
        result = DesCore.encryptBlock(result, key2)
        result = DesCore.decryptBlock(result, key1)
        return result
    }

    /**
     * Encrypts a single block using 3DES-168.
     */
    fun encryptBlock168(block: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray): ByteArray {
        var result = DesCore.encryptBlock(block, key1)
        result = DesCore.decryptBlock(result, key2)
        result = DesCore.encryptBlock(result, key3)
        return result
    }

    /**
     * Decrypts a single block using 3DES-168.
     */
    fun decryptBlock168(block: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray): ByteArray {
        var result = DesCore.decryptBlock(block, key3)
        result = DesCore.encryptBlock(result, key2)
        result = DesCore.decryptBlock(result, key1)
        return result
    }
}
