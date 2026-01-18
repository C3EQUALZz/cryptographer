package com.example.cryptographer.domain.text.valueobjects.aes

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for AesState.
 */
class AesStateTest {

    @Test
    fun `fromBlock and toBlock should preserve bytes`() {
        // Given
        val block = ByteArray(16) { it.toByte() }

        // When
        val state = AesState.fromBlock(block)
        val restored = state.toBlock()

        // Then
        Assert.assertArrayEquals(block, restored)
    }

    @Test
    fun `fromBlock should use column-major order`() {
        // Given
        val block = ByteArray(16) { it.toByte() }

        // When
        val state = AesState.fromBlock(block)

        // Then
        Assert.assertEquals(0, state.getByte(0, 0).toInt() and 0xFF)
        Assert.assertEquals(6, state.getByte(2, 1).toInt() and 0xFF)
        Assert.assertEquals(15, state.getByte(3, 3).toInt() and 0xFF)
    }

    @Test
    fun `setByte should return new state without mutating original`() {
        // Given
        val block = ByteArray(16) { it.toByte() }
        val state = AesState.fromBlock(block)

        // When
        val updated = state.setByte(0, 0, 0x7F.toByte())

        // Then
        Assert.assertNotEquals(state, updated)
        Assert.assertEquals(0, state.getByte(0, 0).toInt() and 0xFF)
        Assert.assertEquals(0x7F, updated.getByte(0, 0).toInt() and 0xFF)
    }
}
