package com.example.cryptographer.infrastructure.persistence.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for storing encryption keys.
 *
 * Keys are stored encrypted in the database using a master key from Android Keystore.
 * The encrypted key value and algorithm are stored as Base64 strings.
 */
@Entity(tableName = "encryption_keys")
data class KeyEntity(
    @PrimaryKey
    val id: String,
    /**
     * Encrypted key value (Base64 encoded).
     * The actual key bytes are encrypted using a master key from Android Keystore.
     */
    val encryptedKeyValue: String,
    /**
     * Algorithm name (e.g., "AES_256", "CHACHA20_256").
     */
    val algorithm: String,
    /**
     * Creation timestamp.
     */
    val createdAt: Long = Instant.now().toEpochMilli(),
    /**
     * Last update timestamp.
     */
    val updatedAt: Long = Instant.now().toEpochMilli(),
)
