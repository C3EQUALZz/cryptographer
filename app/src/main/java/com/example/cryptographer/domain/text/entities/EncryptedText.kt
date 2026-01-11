package com.example.cryptographer.domain.text.entities

import com.example.cryptographer.domain.common.entities.BaseEntity
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
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
    updatedAt: Instant = Instant.now(),
) : BaseEntity<String>(id, createdAt, updatedAt)
