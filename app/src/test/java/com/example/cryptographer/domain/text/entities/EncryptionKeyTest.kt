package com.example.cryptographer.domain.text.entities

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for EncryptionKey entity.
 */
class EncryptionKeyTest {

    @Test
    fun `encryption key should have correct properties`() {
        // Given
        val key = KeyFactory.createAes256(id = "test-key-id")

        // Then
        assertEquals("test-key-id", key.id)
        assertEquals(32, key.value.size) // AES-256 = 32 bytes
        assertEquals(EncryptionAlgorithm.AES_256, key.algorithm)
        assertNotNull(key.createdAt)
        assertNotNull(key.updatedAt)
    }

    @Test
    fun `encryption key should be equal when IDs are same`() {
        // Given
        val id = "same-id"
        val key1 = KeyFactory.createAes128(id = id)
        val key2 = KeyFactory.createAes192(id = id)

        // Then
        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
    }

    @Test
    fun `encryption key should not be equal when IDs are different`() {
        // Given
        val key1 = KeyFactory.createAes256(id = "id-1")
        val key2 = KeyFactory.createAes256(id = "id-2")

        // Then
        assertNotEquals(key1, key2)
    }

    @Test
    fun `key should have correct size for AES_128`() {
        // Given
        val key = KeyFactory.createAes128()

        // Then
        assertEquals(16, key.value.size)
        assertEquals(EncryptionAlgorithm.AES_128, key.algorithm)
    }

    @Test
    fun `key should have correct size for AES_192`() {
        // Given
        val key = KeyFactory.createAes192()

        // Then
        assertEquals(24, key.value.size)
        assertEquals(EncryptionAlgorithm.AES_192, key.algorithm)
    }

    @Test
    fun `key should have correct size for AES_256`() {
        // Given
        val key = KeyFactory.createAes256()

        // Then
        assertEquals(32, key.value.size)
        assertEquals(EncryptionAlgorithm.AES_256, key.algorithm)
    }
}
