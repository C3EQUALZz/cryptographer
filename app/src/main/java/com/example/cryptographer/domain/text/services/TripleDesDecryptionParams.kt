package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * Parameters for 3DES decryption operation.
 * Used to reduce parameter count in methods.
 */
internal data class TripleDesDecryptionParams(
    val encryptedData: ByteArray,
    val key1: ByteArray,
    val key2: ByteArray,
    val key3: ByteArray?,
    val iv: ByteArray,
    val algorithm: EncryptionAlgorithm,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TripleDesDecryptionParams

        if (!areArraysEqual(other)) return false
        if (algorithm != other.algorithm) return false

        return true
    }

    private fun areArraysEqual(other: TripleDesDecryptionParams): Boolean {
        return encryptedData.contentEquals(other.encryptedData) &&
            key1.contentEquals(other.key1) &&
            key2.contentEquals(other.key2) &&
            areKey3Equal(other.key3) &&
            iv.contentEquals(other.iv)
    }

    private fun areKey3Equal(otherKey3: ByteArray?): Boolean {
        return when {
            key3 == null && otherKey3 == null -> true
            key3 != null && otherKey3 != null -> key3.contentEquals(otherKey3)
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = encryptedData.contentHashCode()
        result = 31 * result + key1.contentHashCode()
        result = 31 * result + key2.contentHashCode()
        result = 31 * result + (key3?.contentHashCode() ?: 0)
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        return result
    }
}
