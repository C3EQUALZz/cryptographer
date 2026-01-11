package com.example.cryptographer.application.queries.key.readbyid

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import com.example.cryptographer.test.stubs.StubKeyQueryGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoadKeyQueryHandler.
 */
class LoadKeyQueryHandlerTest {

    private lateinit var queryGateway: StubKeyQueryGateway
    private lateinit var handler: LoadKeyQueryHandler

    @Before
    fun setUp() {
        queryGateway = StubKeyQueryGateway()
        handler = LoadKeyQueryHandler(queryGateway)
    }

    @Test
    fun `invoke should return key view when key exists`() {
        // Given
        val keyId = "test-key-id"
        val key = KeyFactory.createAes256(id = keyId)
        queryGateway.addKey(keyId, key)
        val query = LoadKeyQuery(keyId)

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isSuccess)
        val keyView = result.getOrThrow()
        assertEquals(keyId, keyView.id)
        assertEquals(EncryptionAlgorithm.AES_256, keyView.algorithm)
        assertNotNull(keyView.keyBase64)
    }

    @Test
    fun `invoke should fail when key does not exist`() {
        // Given
        val query = LoadKeyQuery("non-existent-key")

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should return correct algorithm`() {
        // Given
        val keyId = "test-key"
        val key = KeyFactory.createAes128(id = keyId)
        queryGateway.addKey(keyId, key)
        val query = LoadKeyQuery(keyId)

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(EncryptionAlgorithm.AES_128, result.getOrThrow().algorithm)
    }
}

