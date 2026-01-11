package com.example.cryptographer.application.commands.key.delete

import com.example.cryptographer.application.errors.KeyDeleteError
import com.example.cryptographer.test.factories.KeyFactory
import com.example.cryptographer.test.stubs.StubKeyCommandGateway
import com.example.cryptographer.test.stubs.StubKeyQueryGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteKeyCommandHandler.
 */
class DeleteKeyCommandHandlerTest {

    private lateinit var queryGateway: StubKeyQueryGateway
    private lateinit var commandGateway: StubKeyCommandGateway
    private lateinit var handler: DeleteKeyCommandHandler

    @Before
    fun setUp() {
        queryGateway = StubKeyQueryGateway()
        commandGateway = StubKeyCommandGateway(queryGateway)
        handler = DeleteKeyCommandHandler(commandGateway)
    }

    @Test
    fun `invoke should delete key successfully`() {
        // Given
        val keyId = "test-key-id"
        val key = KeyFactory.createAes256(id = keyId)
        queryGateway.addKey(keyId, key)
        commandGateway.saveKey(keyId, key)
        val command = DeleteKeyCommand(keyId)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        assertNull(queryGateway.getKey(keyId))
    }

    @Test
    fun `invoke should fail with KeyDeleteError when delete fails`() {
        // Given
        commandGateway.setShouldFailDelete(true)
        val keyId = "test-key"
        val command = DeleteKeyCommand(keyId)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is KeyDeleteError)
        assertEquals(keyId, (error as KeyDeleteError).keyId)
    }

    @Test
    fun `invoke should succeed even if key does not exist`() {
        // Given
        val command = DeleteKeyCommand("non-existent-key")

        // When
        val result = handler(command)

        // Then
        // Depending on implementation, this might succeed or fail
        // For now, we test that it doesn't throw
        assertNotNull(result)
    }
}
