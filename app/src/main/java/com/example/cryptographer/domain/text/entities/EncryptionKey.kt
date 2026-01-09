package com.example.cryptographer.domain.text.entities

import com.example.cryptographer.domain.common.entities.BaseEntity
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import java.time.Instant
import java.util.UUID

/**
 * Entity representing an encryption key.
 * The key can be represented in various formats depending on the algorithm.
 */
class EncryptionKey(
    id: String = UUID.randomUUID().toString(),
    val value: ByteArray,
    val algorithm: EncryptionAlgorithm,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) : BaseEntity<String>(id, createdAt, updatedAt) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptionKey) return false
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
