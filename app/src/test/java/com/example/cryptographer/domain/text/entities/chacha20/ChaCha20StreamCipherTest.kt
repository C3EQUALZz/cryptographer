package com.example.cryptographer.domain.text.entities.chacha20

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for ChaCha20StreamCipher.
 */
class ChaCha20StreamCipherTest {

    @Test
    fun `process should round-trip with same key and nonce`() {
        // Given
        val key = ByteArray(32) { it.toByte() }
        val nonce = ByteArray(12) { (it + 1).toByte() }
        val plaintext = "ChaCha20 stream".toByteArray(Charsets.UTF_8)
        val cipher = ChaCha20StreamCipher()

        // When
        val ciphertext = cipher.process(plaintext, key, nonce, initialCounter = 1)
        val decrypted = cipher.process(ciphertext, key, nonce, initialCounter = 1)

        // Then
        Assert.assertArrayEquals(plaintext, decrypted)
    }
}
