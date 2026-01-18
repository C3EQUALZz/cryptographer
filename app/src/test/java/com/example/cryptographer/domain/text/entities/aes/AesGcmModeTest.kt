package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import com.example.cryptographer.test.factories.KeyFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for AesGcmMode.
 */
class AesGcmModeTest {

    @Test
    fun `encrypt and decrypt should round-trip with fixed iv and aad`() {
        // Given
        val key = KeyFactory.createAes256()
        val numRounds = AesNumRounds.create(key.algorithm).getOrThrow()
        val roundKeys = AesKeyExpansion.expandKey(key.value, numRounds)
        val iv = ByteArray(12) { it.toByte() }
        val aad = "aad".toByteArray(Charsets.UTF_8)
        val plaintext = "AES-GCM test".toByteArray(Charsets.UTF_8)

        val encryptParams = AesGcmMode.EncryptParams(
            plaintext = plaintext,
            iv = iv,
            aad = aad,
            roundKeys = roundKeys,
            numRounds = numRounds,
        )

        // When
        val (ciphertext, tag) = AesGcmMode.encrypt(encryptParams)
        val decryptParams = AesGcmMode.DecryptParams(
            ciphertext = ciphertext,
            tag = tag,
            iv = iv,
            aad = aad,
            roundKeys = roundKeys,
            numRounds = numRounds,
        )
        val decrypted = AesGcmMode.decrypt(decryptParams)

        // Then
        assertNotNull(decrypted)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `decrypt should fail when tag is modified`() {
        // Given
        val key = KeyFactory.createAes256()
        val numRounds = AesNumRounds.create(key.algorithm).getOrThrow()
        val roundKeys = AesKeyExpansion.expandKey(key.value, numRounds)
        val iv = ByteArray(12) { (it + 1).toByte() }
        val aad = "aad".toByteArray(Charsets.UTF_8)
        val plaintext = "Tamper test".toByteArray(Charsets.UTF_8)

        val encryptParams = AesGcmMode.EncryptParams(
            plaintext = plaintext,
            iv = iv,
            aad = aad,
            roundKeys = roundKeys,
            numRounds = numRounds,
        )
        val (ciphertext, tag) = AesGcmMode.encrypt(encryptParams)
        val tamperedTag = tag.copyOf().also {
            it[0] = (it[0].toInt() xor 0x01).toByte()
        }

        // When
        val decryptParams = AesGcmMode.DecryptParams(
            ciphertext = ciphertext,
            tag = tamperedTag,
            iv = iv,
            aad = aad,
            roundKeys = roundKeys,
            numRounds = numRounds,
        )
        val decrypted = AesGcmMode.decrypt(decryptParams)

        // Then
        assertNull(decrypted)
    }
}
