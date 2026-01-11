package com.example.cryptographer.application.queries.key.readall

import com.example.cryptographer.test.factories.KeyFactory
import com.example.cryptographer.test.stubs.StubKeyQueryGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoadAllKeysQueryHandler.
 */
class LoadAllKeysQueryHandlerTest {

    private lateinit var queryGateway: StubKeyQueryGateway
    private lateinit var handler: LoadAllKeysQueryHandler

    @Before
    fun setUp() {
        queryGateway = StubKeyQueryGateway()
        handler = LoadAllKeysQueryHandler(queryGateway)
    }

    @Test
    fun `invoke should return empty list when no keys exist`() {
        // Given
        val query = LoadAllKeysQuery

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `invoke should return all keys`() {
        // Given
        val key1 = KeyFactory.createAes128(id = "key-1")
        val key2 = KeyFactory.createAes192(id = "key-2")
        val key3 = KeyFactory.createAes256(id = "key-3")

        queryGateway.addKey("key-1", key1)
        queryGateway.addKey("key-2", key2)
        queryGateway.addKey("key-3", key3)

        val query = LoadAllKeysQuery

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isSuccess)
        val keyViews = result.getOrThrow()
        assertEquals(3, keyViews.size)
        assertEquals(setOf("key-1", "key-2", "key-3"), keyViews.map { it.id }.toSet())
    }

    @Test
    fun `invoke should return correct key views`() {
        // Given
        val key = KeyFactory.createAes256(id = "test-key")
        queryGateway.addKey("test-key", key)
        val query = LoadAllKeysQuery

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isSuccess)
        val keyViews = result.getOrThrow()
        assertEquals(1, keyViews.size)
        assertEquals("test-key", keyViews[0].id)
        assertNotNull(keyViews[0].keyBase64)
    }
}
