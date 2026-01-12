package com.example.cryptographer.domain.text.services.chacha20

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ChaCha20Core.
 * Tests are based on RFC 8439 test vectors.
 */
class ChaCha20CoreTest {

    @Test
    fun `createInitialState should create correct state structure`() {
        // Given - Test vector from RFC 8439
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { (it + 0x09).toByte() }
        val counter = 1

        // When
        val state = ChaCha20Core.createInitialState(key, nonce, counter)

        // Then
        assertEquals(64, state.size) // State must be 64 bytes

        // Check constants: "expand 32-byte k"
        assertEquals(0x65.toByte(), state[0]) // 'e'
        assertEquals(0x78.toByte(), state[1]) // 'x'
        assertEquals(0x70.toByte(), state[2]) // 'p'
        assertEquals(0x61.toByte(), state[3]) // 'a'
        assertEquals(0x6E.toByte(), state[4]) // 'n'
        assertEquals(0x64.toByte(), state[5]) // 'd'
        assertEquals(0x20.toByte(), state[6]) // ' '
        assertEquals(0x33.toByte(), state[7]) // '3'
        assertEquals(0x32.toByte(), state[8]) // '2'
        assertEquals(0x2D.toByte(), state[9]) // '-'
        assertEquals(0x62.toByte(), state[10]) // 'b'
        assertEquals(0x79.toByte(), state[11]) // 'y'
        assertEquals(0x74.toByte(), state[12]) // 't'
        assertEquals(0x65.toByte(), state[13]) // 'e'
        assertEquals(0x20.toByte(), state[14]) // ' '
        assertEquals(0x6B.toByte(), state[15]) // 'k'

        // Check key is copied correctly (offset 16-47)
        for (i in 0 until 32) {
            assertEquals(key[i], state[16 + i])
        }

        // Check counter is set correctly (offset 48-51, little-endian)
        assertEquals(1, state[48].toInt() and 0xFF)
        assertEquals(0, state[49].toInt() and 0xFF)
        assertEquals(0, state[50].toInt() and 0xFF)
        assertEquals(0, state[51].toInt() and 0xFF)

        // Check nonce is copied correctly (offset 52-63)
        for (i in 0 until 12) {
            assertEquals(nonce[i], state[52 + i])
        }
    }

    @Test
    fun `createInitialState should handle different counter values`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val nonce = ByteArray(12) { 0x00 }
        val counter = 0x01000000 // 16777216

        // When
        val state = ChaCha20Core.createInitialState(key, nonce, counter)

        // Then - Counter should be in little-endian format
        assertEquals(0x00, state[48].toInt() and 0xFF)
        assertEquals(0x00, state[49].toInt() and 0xFF)
        assertEquals(0x00, state[50].toInt() and 0xFF)
        assertEquals(0x01, state[51].toInt() and 0xFF)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createInitialState should throw for invalid key size`() {
        // Given
        val invalidKey = ByteArray(16) // Should be 32 bytes
        val nonce = ByteArray(12)
        val counter = 0

        // When
        ChaCha20Core.createInitialState(invalidKey, nonce, counter)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createInitialState should throw for invalid nonce size`() {
        // Given
        val key = ByteArray(32)
        val invalidNonce = ByteArray(8) // Should be 12 bytes
        val counter = 0

        // When
        ChaCha20Core.createInitialState(key, invalidNonce, counter)
    }

    @Test
    fun `block should produce correct keystream for RFC 8439 test vector`() {
        // Given - Test vector from RFC 8439 Section 2.3.2
        // Key: 00 01 02 ... 1f (32 bytes)
        val key = ByteArray(32) { it.toByte() }
        // Nonce: 00 00 00 09 00 00 00 4a 00 00 00 00
        val nonce = byteArrayOf(
            0x00, 0x00, 0x00, 0x09,
            0x00, 0x00, 0x00, 0x4a,
            0x00, 0x00, 0x00, 0x00
        )
        val counter = 1

        // Expected output from RFC 8439 Section 2.3.2
        // Calculated from final state: e4e7f110 15593bd1 1fdd0f50 c47120a3...
        // (serialized as little-endian bytes)
        val expectedKeystream = byteArrayOf(
            0x10.toByte(), 0xf1.toByte(), 0xe7.toByte(), 0xe4.toByte(),
            0xd1.toByte(), 0x3b.toByte(), 0x59.toByte(), 0x15.toByte(),
            0x50.toByte(), 0x0f.toByte(), 0xdd.toByte(), 0x1f.toByte(),
            0xa3.toByte(), 0x20.toByte(), 0x71.toByte(), 0xc4.toByte(),
            0xc7.toByte(), 0xd1.toByte(), 0xf4.toByte(), 0xc7.toByte(),
            0x33.toByte(), 0xc0.toByte(), 0x68.toByte(), 0x03.toByte(),
            0x04.toByte(), 0x22.toByte(), 0xaa.toByte(), 0x9a.toByte(),
            0xc3.toByte(), 0xd4.toByte(), 0x6c.toByte(), 0x4e.toByte(),
            0xd2.toByte(), 0x82.toByte(), 0x64.toByte(), 0x46.toByte(),
            0x07.toByte(), 0x9f.toByte(), 0xaa.toByte(), 0x09.toByte(),
            0x14.toByte(), 0xc2.toByte(), 0xd7.toByte(), 0x05.toByte(),
            0xd9.toByte(), 0x8b.toByte(), 0x02.toByte(), 0xa2.toByte(),
            0xb5.toByte(), 0x12.toByte(), 0x9c.toByte(), 0xd1.toByte(),
            0xde.toByte(), 0x16.toByte(), 0x4e.toByte(), 0xb9.toByte(),
            0xcb.toByte(), 0xd0.toByte(), 0x83.toByte(), 0xe8.toByte(),
            0xa2.toByte(), 0x50.toByte(), 0x3c.toByte(), 0x4e.toByte(),
        )

        // When
        val initialState = ChaCha20Core.createInitialState(key, nonce, counter)
        val keystream = ChaCha20Core.block(initialState)

        // Then
        assertEquals(64, keystream.size)
        assertArrayEquals("Keystream should match RFC 8439 test vector", expectedKeystream, keystream)
    }

    @Test
    fun `block should produce different keystreams for different counters`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val nonce = ByteArray(12) { 0x00 }

        // When
        val state1 = ChaCha20Core.createInitialState(key, nonce, 0)
        val keystream1 = ChaCha20Core.block(state1)

        val state2 = ChaCha20Core.createInitialState(key, nonce, 1)
        val keystream2 = ChaCha20Core.block(state2)

        // Then
        assertEquals(64, keystream1.size)
        assertEquals(64, keystream2.size)
        // Keystreams should be different
        var different = false
        for (i in keystream1.indices) {
            if (keystream1[i] != keystream2[i]) {
                different = true
                break
            }
        }
        assertTrue("Keystreams should be different for different counters", different)
    }

    @Test
    fun `block should produce different keystreams for different nonces`() {
        // Given
        val key = ByteArray(32) { 0x00 }
        val nonce1 = ByteArray(12) { 0x00 }
        val nonce2 = ByteArray(12) { 0x01 }
        val counter = 0

        // When
        val state1 = ChaCha20Core.createInitialState(key, nonce1, counter)
        val keystream1 = ChaCha20Core.block(state1)

        val state2 = ChaCha20Core.createInitialState(key, nonce2, counter)
        val keystream2 = ChaCha20Core.block(state2)

        // Then
        // Keystreams should be different
        var different = false
        for (i in keystream1.indices) {
            if (keystream1[i] != keystream2[i]) {
                different = true
                break
            }
        }
        assertTrue("Keystreams should be different for different nonces", different)
    }

    @Test
    fun `block should produce same keystream for same inputs`() {
        // Given
        val key = ByteArray(32) { 0x42 }
        val nonce = ByteArray(12) { 0x42 }
        val counter = 42

        // When
        val state1 = ChaCha20Core.createInitialState(key, nonce, counter)
        val keystream1 = ChaCha20Core.block(state1)

        val state2 = ChaCha20Core.createInitialState(key, nonce, counter)
        val keystream2 = ChaCha20Core.block(state2)

        // Then
        assertArrayEquals("Keystreams should be identical for same inputs", keystream1, keystream2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `block should throw for invalid input size`() {
        // Given
        val invalidInput = ByteArray(32) // Should be 64 bytes

        // When
        ChaCha20Core.block(invalidInput)
    }

    @Test
    fun `block should handle multiple blocks correctly`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { 0x00 }

        // When - Generate multiple blocks
        val block0 = ChaCha20Core.block(ChaCha20Core.createInitialState(key, nonce, 0))
        val block1 = ChaCha20Core.block(ChaCha20Core.createInitialState(key, nonce, 1))
        val block2 = ChaCha20Core.block(ChaCha20Core.createInitialState(key, nonce, 2))

        // Then - All blocks should be different
        assertFalse("Block 0 and 1 should be different", block0.contentEquals(block1))
        assertFalse("Block 1 and 2 should be different", block1.contentEquals(block2))
        assertFalse("Block 0 and 2 should be different", block0.contentEquals(block2))
    }

    private fun assertTrue(message: String, condition: Boolean) {
        org.junit.Assert.assertTrue(message, condition)
    }

    private fun assertFalse(message: String, condition: Boolean) {
        org.junit.Assert.assertFalse(message, condition)
    }
}
