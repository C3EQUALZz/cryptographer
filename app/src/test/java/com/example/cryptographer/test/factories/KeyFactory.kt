package com.example.cryptographer.test.factories

import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import java.time.Instant
import java.util.UUID

/**
 * Factory for creating test EncryptionKey entities.
 * Provides convenient methods for creating keys in tests.
 */
object KeyFactory {
    /**
     * Creates a test encryption key with default values.
     */
    fun create(
        id: String = UUID.randomUUID().toString(),
        algorithm: EncryptionAlgorithm = EncryptionAlgorithm.AES_256,
        keyBytes: ByteArray = ByteArray(32) { it.toByte() }, // 32 bytes for AES-256
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ): EncryptionKey {
        // Adjust key size based on algorithm
        val adjustedKeyBytes = when (algorithm) {
            EncryptionAlgorithm.AES_128 -> keyBytes.take(16).toByteArray()
            EncryptionAlgorithm.AES_192 -> keyBytes.take(24).toByteArray()
            EncryptionAlgorithm.AES_256 -> keyBytes.take(32).toByteArray()
            EncryptionAlgorithm.CHACHA20_256 -> keyBytes.take(32).toByteArray() // 256 bits = 32 bytes
        }
        
        return EncryptionKey(
            id = id,
            value = adjustedKeyBytes,
            algorithm = algorithm,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Creates an AES-128 key.
     */
    fun createAes128(id: String = UUID.randomUUID().toString()): EncryptionKey {
        return create(id = id, algorithm = EncryptionAlgorithm.AES_128, keyBytes = ByteArray(16) { it.toByte() })
    }

    /**
     * Creates an AES-192 key.
     */
    fun createAes192(id: String = UUID.randomUUID().toString()): EncryptionKey {
        return create(id = id, algorithm = EncryptionAlgorithm.AES_192, keyBytes = ByteArray(24) { it.toByte() })
    }

    /**
     * Creates an AES-256 key.
     */
    fun createAes256(id: String = UUID.randomUUID().toString()): EncryptionKey {
        return create(id = id, algorithm = EncryptionAlgorithm.AES_256, keyBytes = ByteArray(32) { it.toByte() })
    }

    /**
     * Creates a ChaCha20-256 key.
     */
    fun createChaCha20_256(id: String = UUID.randomUUID().toString()): EncryptionKey {
        return create(id = id, algorithm = EncryptionAlgorithm.CHACHA20_256, keyBytes = ByteArray(32) { it.toByte() })
    }
}

