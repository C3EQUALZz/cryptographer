package com.example.cryptographer.application.commands.key.delete_all

import com.example.cryptographer.test.factories.KeyFactory
import com.example.cryptographer.test.stubs.StubKeyCommandGateway
import com.example.cryptographer.test.stubs.StubKeyQueryGateway
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteAllKeysCommandHandler.
 */
class DeleteAllKeysCommandHandlerTest {

    private lateinit var queryGateway: StubKeyQueryGateway
    private lateinit var commandGateway: StubKeyCommandGateway
    private lateinit var handler: DeleteAllKeysCommandHandler

    @Before
    fun setUp() {
        queryGateway = StubKeyQueryGateway()
        commandGateway = StubKeyCommandGateway(queryGateway)
        handler = DeleteAllKeysCommandHandler(commandGateway)
    }

    @Test
    fun `invoke should delete all keys successfully`() {
        // Given
        val key1 = KeyFactory.createAes128(id = "key-1")
        val key2 = KeyFactory.createAes192(id = "key-2")
        queryGateway.addKey("key-1", key1)
        queryGateway.addKey("key-2", key2)
        commandGateway.saveKey("key-1", key1)
        commandGateway.saveKey("key-2", key2)
        val command = DeleteAllKeysCommand

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(queryGateway.getAllKeyIds().isEmpty())
    }

    @Test
    fun `invoke should succeed when no keys exist`() {
        // Given
        val command = DeleteAllKeysCommand

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should fail when deleteAll fails`() {
        // Given
        commandGateway.setShouldFailDeleteAll(true)
        val command = DeleteAllKeysCommand

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
    }
}

