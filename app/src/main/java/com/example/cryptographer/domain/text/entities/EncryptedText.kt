package com.example.cryptographer.domain.text.entities

import com.example.cryptographer.domain.common.entities.BaseEntity
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import java.time.Instant
import java.util.UUID

/**
 * Entity representing encrypted text.
 * Contains encrypted data and encryption metadata.
 */
class EncryptedText(
    id: String = UUID.randomUUID().toString(),
    val encryptedData: ByteArray,
    val algorithm: EncryptionAlgorithm,
    val initializationVector: ByteArray? = null,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) : BaseEntity<String>(id, createdAt, updatedAt) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedText) return false
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
