package com.example.cryptographer.domain.text.valuebjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.text.valueobjects.aes.AesBlock
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for AesBlock.
 */
class AesBlockTest {

    @Test
    fun `create should succeed with 16 bytes`() {
        // Given
        val bytes = ByteArray(16) { it.toByte() }

        // When
        val result = AesBlock.create(bytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertArrayEquals(bytes, result.getOrThrow().bytes)
    }

    @Test
    fun `create should fail with invalid size`() {
        // Given
        val bytes = ByteArray(15)

        // When
        val result = AesBlock.create(bytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `createZero should return zeroed block`() {
        // When
        val block = AesBlock.createZero()

        // Then
        Assert.assertArrayEquals(ByteArray(16), block.bytes)
    }

    @Test
    fun `toByteArray should return a copy`() {
        // Given
        val bytes = ByteArray(16) { it.toByte() }
        val block = AesBlock.create(bytes).getOrThrow()

        // When
        val copy = block.toByteArray()
        copy[0] = 0x7F

        // Then
        Assert.assertNotEquals(copy[0], block.bytes[0])
    }
}
