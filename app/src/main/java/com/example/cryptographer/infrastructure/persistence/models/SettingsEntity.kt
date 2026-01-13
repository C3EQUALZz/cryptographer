package com.example.cryptographer.infrastructure.persistence.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for storing application settings.
 *
 * Settings are stored encrypted in the database using a master key from Android Keystore.
 * The encrypted values are stored as Base64 strings.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val key: String,
    /**
     * Encrypted setting value (Base64 encoded).
     * The actual value is encrypted using a master key from Android Keystore.
     */
    val encryptedValue: String,
    /**
     * Creation timestamp.
     */
    val createdAt: Long = Instant.now().toEpochMilli(),
    /**
     * Last update timestamp.
     */
    val updatedAt: Long = Instant.now().toEpochMilli(),
)
