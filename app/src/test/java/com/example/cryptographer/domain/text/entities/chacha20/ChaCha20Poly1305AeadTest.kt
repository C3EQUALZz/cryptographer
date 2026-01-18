package com.example.cryptographer.domain.text.entities.chacha20

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.chacha20.ChaCha20Key
import com.example.cryptographer.domain.text.valueobjects.chacha20.ChaCha20Nonce
import com.example.cryptographer.domain.text.valueobjects.chacha20.Poly1305Tag
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for ChaCha20Poly1305Aead.
 */
class ChaCha20Poly1305AeadTest {

    private val aead = ChaCha20Poly1305Aead()

    @Test
    fun `encrypt and decrypt should round-trip`() {
        // Given
        val key = ChaCha20Key.Companion.create(
            EncryptionAlgorithm.CHACHA20_256,
            ByteArray(32) { it.toByte() },
        ).getOrThrow()
        val nonce = ChaCha20Nonce.Companion.create(ByteArray(12) { it.toByte() }).getOrThrow()
        val aad = "aad".toByteArray(Charsets.UTF_8)
        val plaintext = "ChaCha20-Poly1305 test".toByteArray(Charsets.UTF_8)

        // When
        val (ciphertext, tag) = aead.encrypt(plaintext, key, nonce, aad)
        val decrypted = aead.decrypt(ciphertext, tag, key, nonce, aad)

        // Then
        Assert.assertNotNull(decrypted)
        Assert.assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `decrypt should fail when tag is modified`() {
        // Given
        val key = ChaCha20Key.Companion.create(
            EncryptionAlgorithm.CHACHA20_256,
            ByteArray(32) { it.toByte() },
        ).getOrThrow()
        val nonce = ChaCha20Nonce.Companion.create(ByteArray(12) { (it + 1).toByte() }).getOrThrow()
        val aad = "aad".toByteArray(Charsets.UTF_8)
        val plaintext = "Tamper test".toByteArray(Charsets.UTF_8)

        val (ciphertext, tag) = aead.encrypt(plaintext, key, nonce, aad)
        val tamperedTagBytes = tag.toByteArray()
        tamperedTagBytes[0] = (tamperedTagBytes[0].toInt() xor 0x01).toByte()
        val tamperedTag = Poly1305Tag.Companion.create(tamperedTagBytes).getOrThrow()

        // When
        val decrypted = aead.decrypt(ciphertext, tamperedTag, key, nonce, aad)

        // Then
        Assert.assertNull(decrypted)
    }
}
