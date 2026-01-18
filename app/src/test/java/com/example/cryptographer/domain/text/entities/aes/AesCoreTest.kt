package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.aes.AesBlock
import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * Unit tests for AesCore.
 */
class AesCoreTest {

    @Test
    fun `encryptBlock should match AES-128 test vector`() {
        // Given (FIPS 197, Appendix C.1)
        val keyBytes = hexToBytes("000102030405060708090a0b0c0d0e0f")
        val plaintext = hexToBytes("00112233445566778899aabbccddeeff")
        val expectedCiphertext = hexToBytes("69c4e0d86a7b0430d8cdb78070b4c55a")

        val numRounds = AesNumRounds.create(EncryptionAlgorithm.AES_128).getOrThrow()
        val roundKeys = AesKeyExpansion.expandKey(keyBytes, numRounds)
        val block = AesBlock.create(plaintext).getOrThrow()

        // When
        val encryptedBlock = AesCore.encryptBlock(block, roundKeys, numRounds)

        // Then
        assertArrayEquals(expectedCiphertext, encryptedBlock.bytes)
    }

    private fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have even length" }
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
