package com.example.cryptographer.domain.text.entity

/**
 * Entity representing encrypted text.
 * Contains encrypted data and encryption metadata.
 */
data class EncryptedText(
    val encryptedData: ByteArray,
    val algorithm: EncryptionAlgorithm,
    val initializationVector: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedText

        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (algorithm != other.algorithm) return false
        if (initializationVector != null) {
            if (other.initializationVector == null) return false
            if (!initializationVector.contentEquals(other.initializationVector)) return false
        } else if (other.initializationVector != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptedData.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + (initializationVector?.contentHashCode() ?: 0)
        return result
    }
}

