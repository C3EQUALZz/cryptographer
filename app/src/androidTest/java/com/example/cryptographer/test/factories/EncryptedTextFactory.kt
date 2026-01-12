package com.example.cryptographer.test.factories

import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import java.time.Instant
import java.util.UUID

/**
 * Factory for creating test EncryptedText entities.
 * Provides convenient methods for creating encrypted text objects in tests.
 */
object EncryptedTextFactory {
    /**
     * Creates a test EncryptedText entity with default values.
     */
    fun create(
        id: String = UUID.randomUUID().toString(),
        encryptedData: ByteArray = ByteArray(16) { it.toByte() },
        algorithm: EncryptionAlgorithm = EncryptionAlgorithm.AES_256,
        initializationVector: ByteArray? = ByteArray(12) { it.toByte() },
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): EncryptedText {
        return EncryptedText(
            id = id,
            encryptedData = encryptedData,
            algorithm = algorithm,
            initializationVector = initializationVector,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * Creates EncryptedText without IV.
     */
    fun createWithoutIv(
        id: String = UUID.randomUUID().toString(),
        encryptedData: ByteArray = ByteArray(16) { it.toByte() },
        algorithm: EncryptionAlgorithm = EncryptionAlgorithm.AES_256,
    ): EncryptedText {
        return create(id = id, encryptedData = encryptedData, algorithm = algorithm, initializationVector = null)
    }
}
