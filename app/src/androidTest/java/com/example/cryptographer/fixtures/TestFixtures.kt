package com.example.cryptographer.fixtures

import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

/**
 * Test fixtures for creating test data in Android tests.
 * Provides convenient methods for creating test entities with realistic data.
 */
object TestFixtures {

    private val random = SecureRandom()

    /**
     * Creates a test encryption key with random bytes.
     */
    fun createEncryptionKey(
        algorithm: EncryptionAlgorithm = EncryptionAlgorithm.CHACHA20_256,
        id: String = UUID.randomUUID().toString(),
    ): EncryptionKey {
        val keySize = when (algorithm) {
            EncryptionAlgorithm.AES_128 -> 16
            EncryptionAlgorithm.AES_192 -> 24
            EncryptionAlgorithm.AES_256 -> 32
            EncryptionAlgorithm.CHACHA20_256 -> 32
            EncryptionAlgorithm.TDES_112 -> 16
            EncryptionAlgorithm.TDES_168 -> 24
        }
        val keyBytes = ByteArray(keySize).apply {
            random.nextBytes(this)
        }
        return EncryptionKey(
            id = id,
            value = keyBytes,
            algorithm = algorithm,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }

    /**
     * Creates a test encryption key that is guaranteed to be different from another key.
     */
    fun createDifferentKey(
        algorithm: EncryptionAlgorithm = EncryptionAlgorithm.CHACHA20_256,
        otherKey: EncryptionKey,
    ): EncryptionKey {
        var newKey: EncryptionKey
        do {
            newKey = createEncryptionKey(algorithm)
        } while (newKey.value.contentEquals(otherKey.value))
        return newKey
    }

    /**
     * Creates random test data of specified size.
     */
    fun createRandomData(size: Int): ByteArray {
        val data = ByteArray(size)
        random.nextBytes(data)
        return data
    }

    /**
     * Creates test text data.
     */
    fun createTestText(content: String = "Test message"): ByteArray {
        return content.toByteArray(Charsets.UTF_8)
    }
}
