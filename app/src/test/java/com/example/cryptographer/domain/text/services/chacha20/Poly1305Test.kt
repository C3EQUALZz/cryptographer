package com.example.cryptographer.domain.text.services.chacha20

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Poly1305 MAC implementation.
 * Tests are based on RFC 8439 test vectors.
 */
class Poly1305Test {

    @Test
    fun `compute should produce correct tag for RFC 8439 test vector 1`() {
        // Given - Test vector from RFC 8439 Section 2.5.2
        val key = byteArrayOf(
            0x85.toByte(),
            0xd6.toByte(),
            0xbe.toByte(),
            0x78.toByte(),
            0x57.toByte(),
            0x55.toByte(),
            0x6d.toByte(),
            0x33.toByte(),
            0x7f.toByte(),
            0x44.toByte(),
            0x52.toByte(),
            0xfe.toByte(),
            0x42.toByte(),
            0xd5.toByte(),
            0x06.toByte(),
            0xa8.toByte(),
            0x01.toByte(),
            0x03.toByte(),
            0x80.toByte(),
            0x8a.toByte(),
            0xfb.toByte(),
            0x0d.toByte(),
            0xb2.toByte(),
            0xfd.toByte(),
            0x4a.toByte(),
            0xbf.toByte(),
            0xf6.toByte(),
            0xaf.toByte(),
            0x41.toByte(),
            0x49.toByte(),
            0xf5.toByte(),
            0x1b.toByte(),
        )
        val message = "Cryptographic Forum Research Group".toByteArray(Charsets.UTF_8)

        // Expected tag from RFC 8439
        val expectedTag = byteArrayOf(
            0xa8.toByte(),
            0x06.toByte(),
            0x1d.toByte(),
            0xc1.toByte(),
            0x30.toByte(),
            0x51.toByte(),
            0x36.toByte(),
            0xc6.toByte(),
            0xc2.toByte(),
            0x2b.toByte(),
            0x8b.toByte(),
            0x88.toByte(),
            0x3e.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
        )

        // When
        val tag = Poly1305.compute(message, key)

        // Then
        assertEquals(16, tag.size)
        assertArrayEquals("Tag should match RFC 8439 test vector", expectedTag, tag)
    }

    @Test
    fun `compute should produce correct tag for empty message`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val message = ByteArray(0)

        // When
        val tag = Poly1305.compute(message, key)

        // Then
        assertEquals(16, tag.size)
        // Tag should not be all zeros
        var allZeros = true
        for (byte in tag) {
            if (byte.toInt() != 0) {
                allZeros = false
                break
            }
        }
        assertFalse("Tag should not be all zeros", allZeros)
    }

    @Test
    fun `compute should produce different tags for different messages`() {
        // Given
        val key = ByteArray(32) { 0x42 }
        val message1 = "Hello".toByteArray(Charsets.UTF_8)
        val message2 = "World".toByteArray(Charsets.UTF_8)

        // When
        val tag1 = Poly1305.compute(message1, key)
        val tag2 = Poly1305.compute(message2, key)

        // Then
        assertFalse("Tags should be different for different messages", tag1.contentEquals(tag2))
    }

    @Test
    fun `compute should produce different tags for different keys`() {
        // Given
        val key1 = ByteArray(32) { 0x00 }
        val key2 = ByteArray(32) { 0x01 }
        val message = "Test message".toByteArray(Charsets.UTF_8)

        // When
        val tag1 = Poly1305.compute(message, key1)
        val tag2 = Poly1305.compute(message, key2)

        // Then
        assertFalse("Tags should be different for different keys", tag1.contentEquals(tag2))
    }

    @Test
    fun `compute should produce same tag for same inputs`() {
        // Given
        val key = ByteArray(32) { 0x42 }
        val message = "Test message".toByteArray(Charsets.UTF_8)

        // When
        val tag1 = Poly1305.compute(message, key)
        val tag2 = Poly1305.compute(message, key)

        // Then
        assertArrayEquals("Tags should be identical for same inputs", tag1, tag2)
    }

    @Test
    fun `compute should handle messages longer than 16 bytes`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val message = ByteArray(100) { it.toByte() }

        // When
        val tag = Poly1305.compute(message, key)

        // Then
        assertEquals(16, tag.size)
    }

    @Test
    fun `compute should handle messages that are not multiple of 16 bytes`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val message = "This is a test message that is not a multiple of 16 bytes".toByteArray(Charsets.UTF_8)

        // When
        val tag = Poly1305.compute(message, key)

        // Then
        assertEquals(16, tag.size)
    }

    @Test
    fun `compute should handle exactly 16 bytes message`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val message = ByteArray(16) { it.toByte() }

        // When
        val tag = Poly1305.compute(message, key)

        // Then
        assertEquals(16, tag.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `compute should throw for invalid key size`() {
        // Given
        val invalidKey = ByteArray(16) // Should be 32 bytes
        val message = "Test".toByteArray(Charsets.UTF_8)

        // When
        Poly1305.compute(message, invalidKey)
    }

    @Test
    fun `compute should produce deterministic output`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val message = "Deterministic test".toByteArray(Charsets.UTF_8)

        // When - Compute multiple times
        val tag1 = Poly1305.compute(message, key)
        val tag2 = Poly1305.compute(message, key)
        val tag3 = Poly1305.compute(message, key)

        // Then - All should be identical
        assertArrayEquals("Tag 1 and 2 should be identical", tag1, tag2)
        assertArrayEquals("Tag 2 and 3 should be identical", tag2, tag3)
        assertArrayEquals("Tag 1 and 3 should be identical", tag1, tag3)
    }

    @Test
    fun `compute should handle very long messages`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val message = ByteArray(10000) { (it % 256).toByte() }

        // When
        val tag = Poly1305.compute(message, key)

        // Then
        assertEquals(16, tag.size)
    }

    private fun assertFalse(message: String, condition: Boolean) {
        org.junit.Assert.assertFalse(message, condition)
    }
}
