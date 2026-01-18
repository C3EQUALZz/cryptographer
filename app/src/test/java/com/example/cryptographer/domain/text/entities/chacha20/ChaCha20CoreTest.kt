package com.example.cryptographer.domain.text.entities.chacha20

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for ChaCha20Core.
 */
class ChaCha20CoreTest {

    @Test
    fun `block should match RFC 8439 test vector`() {
        // Given (RFC 8439, Section 2.3.2)
        val key = hexToBytes("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f")
        val nonce = hexToBytes("000000090000004a00000000")
        val counter = 1
        val expected = hexToBytes(
            "10f1e7e4d13b5915500fdd1fa32071c4" +
                "c7d1f4c733c068030422aa9ac3d46c4e" +
                "d2826446079faa0914c2d705d98b02a2" +
                "b5129cd1de164eb9cbd083e8a2503c4e",
        )

        // When
        val block = ChaCha20Core.block(key, counter, nonce)

        // Then
        Assert.assertArrayEquals(expected, block)
    }

    private fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have even length" }
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
