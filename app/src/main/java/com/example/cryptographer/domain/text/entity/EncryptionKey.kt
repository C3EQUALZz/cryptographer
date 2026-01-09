package com.example.cryptographer.domain.text.entity

/**
 * Entity representing an encryption key.
 * The key can be represented in various formats depending on the algorithm.
 */
data class EncryptionKey(
    val value: ByteArray,
    val algorithm: EncryptionAlgorithm
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionKey

        if (!value.contentEquals(other.value)) return false
        if (algorithm != other.algorithm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        return result
    }
}

/**
 * Enumeration of supported encryption algorithms.
 */
enum class EncryptionAlgorithm {
    AES_128,
    AES_192,
    AES_256
}

