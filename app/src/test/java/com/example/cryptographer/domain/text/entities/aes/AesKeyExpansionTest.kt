package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * Unit tests for AesKeyExpansion.
 */
class AesKeyExpansionTest {

    @Test
    fun `expandKey should generate expected round keys for AES-128`() {
        // Given (FIPS 197, Appendix A.1)
        val keyBytes = hexToBytes("000102030405060708090a0b0c0d0e0f")
        val expectedRound1 = hexToBytes("d6aa74fdd2af72fadaa678f1d6ab76fe")
        val expectedRound10 = hexToBytes("13111d7fe3944a17f307a78b4d2b30c5")

        val numRounds = AesNumRounds.create(EncryptionAlgorithm.AES_128).getOrThrow()

        // When
        val roundKeys = AesKeyExpansion.expandKey(keyBytes, numRounds)

        // Then
        assertArrayEquals(keyBytes, roundKeys.getInitialRoundKey().bytes)
        assertArrayEquals(expectedRound1, roundKeys.getRoundKey(1).bytes)
        assertArrayEquals(expectedRound10, roundKeys.getFinalRoundKey().bytes)
    }

    private fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have even length" }
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
