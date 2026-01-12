package com.example.cryptographer.domain.text.services.chacha20

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for ChaCha20Poly1305 AEAD implementation.
 * Tests encryption, decryption, and authentication.
 */
class ChaCha20Poly1305Test {

    @Test
    fun `encrypt and decrypt should work correctly`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = "Hello, ChaCha20-Poly1305!".toByteArray(Charsets.UTF_8)

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, tag) = aead.encrypt(plaintext)
        val decrypted = aead.decrypt(ciphertext, tag)

        // Then
        assertNotNull("Decryption should succeed", decrypted)
        assertArrayEquals("Decrypted should match plaintext", plaintext, decrypted!!)
    }

    @Test
    fun `encrypt should produce different ciphertext for same plaintext with different nonce`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val nonce1 = ByteArray(12) { 0x00 }
        val nonce2 = ByteArray(12) { 0x01 }
        val plaintext = "Test message".toByteArray(Charsets.UTF_8)

        // When
        val aead1 = ChaCha20Poly1305(key, nonce1)
        val (ciphertext1, tag1) = aead1.encrypt(plaintext)

        val aead2 = ChaCha20Poly1305(key, nonce2)
        val (ciphertext2, tag2) = aead2.encrypt(plaintext)

        // Then
        assertFalse("Ciphertexts should be different", ciphertext1.contentEquals(ciphertext2))
        assertFalse("Tags should be different", tag1.contentEquals(tag2))
    }

    @Test
    fun `decrypt should return null for invalid tag`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = "Test message".toByteArray(Charsets.UTF_8)

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, _) = aead.encrypt(plaintext)
        val invalidTag = ByteArray(16) { 0xFF.toByte() }

        // Then
        val decrypted = aead.decrypt(ciphertext, invalidTag)
        assertNull("Decryption should fail with invalid tag", decrypted)
    }

    @Test
    fun `decrypt should return null for tampered ciphertext`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = "Test message".toByteArray(Charsets.UTF_8)

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, tag) = aead.encrypt(plaintext)
        val tamperedCiphertext = ciphertext.copyOf()
        tamperedCiphertext[0] = (tamperedCiphertext[0].toInt() xor 0xFF).toByte()

        // Then
        val decrypted = aead.decrypt(tamperedCiphertext, tag)
        assertNull("Decryption should fail with tampered ciphertext", decrypted)
    }

    @Test
    fun `encrypt and decrypt should work with empty plaintext`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = ByteArray(0)

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, tag) = aead.encrypt(plaintext)
        val decrypted = aead.decrypt(ciphertext, tag)

        // Then
        assertNotNull("Decryption should succeed", decrypted)
        assertArrayEquals("Decrypted should match plaintext", plaintext, decrypted!!)
    }

    @Test
    fun `encrypt and decrypt should work with associated data`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = "Secret message".toByteArray(Charsets.UTF_8)
        val associatedData = "Metadata".toByteArray(Charsets.UTF_8)

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, tag) = aead.encrypt(plaintext, associatedData)
        val decrypted = aead.decrypt(ciphertext, tag, associatedData)

        // Then
        assertNotNull("Decryption should succeed", decrypted)
        assertArrayEquals("Decrypted should match plaintext", plaintext, decrypted!!)
    }

    @Test
    fun `decrypt should return null when associated data does not match`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = "Secret message".toByteArray(Charsets.UTF_8)
        val associatedData1 = "Metadata1".toByteArray(Charsets.UTF_8)
        val associatedData2 = "Metadata2".toByteArray(Charsets.UTF_8)

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, tag) = aead.encrypt(plaintext, associatedData1)
        val decrypted = aead.decrypt(ciphertext, tag, associatedData2)

        // Then
        assertNull("Decryption should fail with wrong associated data", decrypted)
    }

    @Test
    fun `encrypt should produce different ciphertexts for same plaintext`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = "Test".toByteArray(Charsets.UTF_8)

        // When - Encrypt same plaintext twice with same key and nonce
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext1, tag1) = aead.encrypt(plaintext)
        val (ciphertext2, tag2) = aead.encrypt(plaintext)

        // Then - Should produce same ciphertext (deterministic with same inputs)
        assertArrayEquals("Ciphertexts should be identical with same inputs", ciphertext1, ciphertext2)
        assertArrayEquals("Tags should be identical with same inputs", tag1, tag2)
    }

    @Test
    fun `encrypt should handle large plaintext`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = ByteArray(10000) { (it % 256).toByte() }

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, tag) = aead.encrypt(plaintext)
        val decrypted = aead.decrypt(ciphertext, tag)

        // Then
        assertNotNull("Decryption should succeed", decrypted)
        assertArrayEquals("Decrypted should match plaintext", plaintext, decrypted!!)
    }

    @Test
    fun `encrypt should handle plaintext that spans multiple blocks`() {
        // Given - Plaintext larger than 64 bytes (one ChaCha20 block)
        val key = ByteArray(32) { 0x00 }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = ByteArray(200) { it.toByte() }

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, tag) = aead.encrypt(plaintext)
        val decrypted = aead.decrypt(ciphertext, tag)

        // Then
        assertNotNull("Decryption should succeed", decrypted)
        assertArrayEquals("Decrypted should match plaintext", plaintext, decrypted!!)
    }

    @Test
    fun `tag should always be 16 bytes`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }
        val plaintexts = listOf(
            ByteArray(0),
            "Short".toByteArray(Charsets.UTF_8),
            ByteArray(100),
            ByteArray(1000),
        )

        // When & Then
        plaintexts.forEach { plaintext ->
            val aead = ChaCha20Poly1305(key, nonce)
            val (_, tag) = aead.encrypt(plaintext)
            assertEquals("Tag should always be 16 bytes", 16, tag.size)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw for invalid key size`() {
        // Given
        val invalidKey = ByteArray(16) // Should be 32 bytes
        val nonce = ByteArray(12)

        // When
        ChaCha20Poly1305(invalidKey, nonce)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw for invalid nonce size`() {
        // Given
        val key = ByteArray(32)
        val invalidNonce = ByteArray(8) // Should be 12 bytes

        // When
        ChaCha20Poly1305(key, invalidNonce)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decrypt should throw for invalid tag size`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val nonce = ByteArray(12) { 0x00 }
        val plaintext = "Test".toByteArray(Charsets.UTF_8)

        // When
        val aead = ChaCha20Poly1305(key, nonce)
        val (ciphertext, _) = aead.encrypt(plaintext)
        val invalidTag = ByteArray(8) // Should be 16 bytes

        // Then
        aead.decrypt(ciphertext, invalidTag)
    }

    private fun assertFalse(message: String, condition: Boolean) {
        org.junit.Assert.assertFalse(message, condition)
    }
}
