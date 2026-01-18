package com.example.cryptographer.domain.text.entities.tdes

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.tdes.TripleDesKey
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * Unit tests for TripleDesEdeCipher.
 */
class TripleDesEdeCipherTest {

    @Test
    fun `encryptBlock and decryptBlock should round-trip for TDES_112`() {
        // Given
        val keyBytes = ByteArray(16) { it.toByte() }
        val key = TripleDesKey.create(EncryptionAlgorithm.TDES_112, keyBytes).getOrThrow()
        val block = ByteArray(8) { (it + 1).toByte() }

        // When
        val encrypted = TripleDesEdeCipher.encryptBlock(block, key)
        val decrypted = TripleDesEdeCipher.decryptBlock(encrypted, key)

        // Then
        assertArrayEquals(block, decrypted)
    }

    @Test
    fun `encryptBlock and decryptBlock should round-trip for TDES_168`() {
        // Given
        val keyBytes = ByteArray(24) { it.toByte() }
        val key = TripleDesKey.create(EncryptionAlgorithm.TDES_168, keyBytes).getOrThrow()
        val block = ByteArray(8) { (it + 2).toByte() }

        // When
        val encrypted = TripleDesEdeCipher.encryptBlock(block, key)
        val decrypted = TripleDesEdeCipher.decryptBlock(encrypted, key)

        // Then
        assertArrayEquals(block, decrypted)
    }
}
