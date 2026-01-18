package com.example.cryptographer.domain.text.entities.tdes

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.tdes.Pkcs5Padding
import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesIv
import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesKey
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * Unit tests for TripleDesCbcMode.
 */
class TripleDesCbcModeTest {

    @Test
    fun `encrypt and decrypt should round-trip for TDES_112`() {
        // Given
        val keyBytes = ByteArray(16) { it.toByte() }
        val key = TripleDesKey.create(EncryptionAlgorithm.TDES_112, keyBytes).getOrThrow()
        val iv = TripleDesIv.create(ByteArray(8) { it.toByte() }).getOrThrow()
        val data = "CBC test".toByteArray(Charsets.UTF_8)
        val padded = Pkcs5Padding.pad(data)

        // When
        val encrypted = TripleDesCbcMode.encrypt(padded, key, iv)
        val decrypted = TripleDesCbcMode.decrypt(encrypted, key, iv)
        val unpadded = Pkcs5Padding.unpad(decrypted)

        // Then
        assertArrayEquals(data, unpadded)
    }

    @Test
    fun `encrypt and decrypt should round-trip for TDES_168`() {
        // Given
        val keyBytes = ByteArray(24) { it.toByte() }
        val key = TripleDesKey.create(EncryptionAlgorithm.TDES_168, keyBytes).getOrThrow()
        val iv = TripleDesIv.create(ByteArray(8) { (it + 1).toByte() }).getOrThrow()
        val data = "CBC test 168".toByteArray(Charsets.UTF_8)
        val padded = Pkcs5Padding.pad(data)

        // When
        val encrypted = TripleDesCbcMode.encrypt(padded, key, iv)
        val decrypted = TripleDesCbcMode.decrypt(encrypted, key, iv)
        val unpadded = Pkcs5Padding.unpad(decrypted)

        // Then
        assertArrayEquals(data, unpadded)
    }
}
