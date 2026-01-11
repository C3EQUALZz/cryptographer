package com.example.cryptographer.domain.text.entities

import com.example.cryptographer.domain.common.entities.BaseEntity
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
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
) : BaseEntity<String>(id, createdAt, updatedAt)
