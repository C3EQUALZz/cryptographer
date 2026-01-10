package com.example.cryptographer.presentation.key

import com.example.cryptographer.application.commands.key.create.GenerateAndSaveKeyCommand
import com.example.cryptographer.application.commands.key.create.GenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommand
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommandHandler
import com.example.cryptographer.application.commands.key.delete_all.DeleteAllKeysCommand
import com.example.cryptographer.application.commands.key.delete_all.DeleteAllKeysCommandHandler
import com.example.cryptographer.application.common.views.KeyIdView
import com.example.cryptographer.application.common.views.KeyView
import com.example.cryptographer.application.queries.key.read_all.LoadAllKeysQuery
import com.example.cryptographer.application.queries.key.read_all.LoadAllKeysQueryHandler
import com.example.cryptographer.application.queries.key.read_by_id.LoadKeyQuery
import com.example.cryptographer.application.queries.key.read_by_id.LoadKeyQueryHandler
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Base64

/**
 * Unit tests for KeyGenerationPresenter.
 */
class KeyGenerationPresenterTest {

    private lateinit var generateAndSaveKeyHandler: GenerateAndSaveKeyCommandHandler
    private lateinit var loadKeyHandler: LoadKeyQueryHandler
    private lateinit var deleteKeyHandler: DeleteKeyCommandHandler
    private lateinit var deleteAllKeysHandler: DeleteAllKeysCommandHandler
    private lateinit var loadAllKeysHandler: LoadAllKeysQueryHandler
    private lateinit var presenter: KeyGenerationPresenter

    @Before
    fun setUp() {
        generateAndSaveKeyHandler = mockk()
        loadKeyHandler = mockk()
        deleteKeyHandler = mockk()
        deleteAllKeysHandler = mockk()
        loadAllKeysHandler = mockk()
        presenter = KeyGenerationPresenter(
            generateAndSaveKeyHandler = generateAndSaveKeyHandler,
            loadKeyHandler = loadKeyHandler,
            deleteKeyHandler = deleteKeyHandler,
            deleteAllKeysHandler = deleteAllKeysHandler,
            loadAllKeysHandler = loadAllKeysHandler
        )
    }

    @Test
    fun `generateAndSaveKey should return success when key is generated and saved`() = runTest {
        // Given
        val algorithm = EncryptionAlgorithm.AES_256
        val key = KeyFactory.createAes256(id = "test-key-id")
        val keyId = "test-key-id"
        val keyBase64 = Base64.getEncoder().encodeToString(key.value)
        
        val keyIdView = KeyIdView(keyId = keyId)
        val keyView = KeyView(
            id = keyId,
            algorithm = algorithm,
            keyBase64 = keyBase64
        )

        coEvery { generateAndSaveKeyHandler(any()) } returns Result.success(keyIdView)
        coEvery { loadKeyHandler(LoadKeyQuery(keyId)) } returns Result.success(keyView)

        // When
        val result = presenter.generateAndSaveKey(algorithm)

        // Then
        assertTrue(result.isSuccess)
        val keyInfo = result.getOrThrow()
        assertEquals(keyId, keyInfo.keyId)
        assertEquals(keyBase64, keyInfo.keyBase64)
        assertEquals(algorithm, keyInfo.key.algorithm)
        assertEquals(32, keyInfo.key.value.size) // AES-256 = 32 bytes

        coVerify(exactly = 1) { generateAndSaveKeyHandler(GenerateAndSaveKeyCommand(algorithm)) }
        coVerify(exactly = 1) { loadKeyHandler(LoadKeyQuery(keyId)) }
    }

    @Test
    fun `generateAndSaveKey should return failure when handler fails`() = runTest {
        // Given
        val algorithm = EncryptionAlgorithm.AES_128
        val error = Exception("Key generation failed")

        coEvery { generateAndSaveKeyHandler(any()) } returns Result.failure(error)

        // When
        val result = presenter.generateAndSaveKey(algorithm)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { generateAndSaveKeyHandler(GenerateAndSaveKeyCommand(algorithm)) }
        coVerify(exactly = 0) { loadKeyHandler(any()) }
    }

    @Test
    fun `generateAndSaveKey should return failure when load key fails after save`() = runTest {
        // Given
        val algorithm = EncryptionAlgorithm.AES_192
        val keyId = "test-key-id"
        val keyIdView = KeyIdView(keyId = keyId)
        val loadError = Exception("Key not found")

        coEvery { generateAndSaveKeyHandler(any()) } returns Result.success(keyIdView)
        coEvery { loadKeyHandler(LoadKeyQuery(keyId)) } returns Result.failure(loadError)

        // When
        val result = presenter.generateAndSaveKey(algorithm)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertTrue(result.exceptionOrNull()?.message?.contains("saved but could not be loaded") == true)

        coVerify(exactly = 1) { generateAndSaveKeyHandler(GenerateAndSaveKeyCommand(algorithm)) }
        coVerify(exactly = 1) { loadKeyHandler(LoadKeyQuery(keyId)) }
    }

    @Test
    fun `loadKey should return success when key is found`() = runTest {
        // Given
        val keyId = "test-key-id"
        val algorithm = EncryptionAlgorithm.AES_256
        val key = KeyFactory.createAes256(id = keyId)
        val keyBase64 = Base64.getEncoder().encodeToString(key.value)
        
        val keyView = KeyView(
            id = keyId,
            algorithm = algorithm,
            keyBase64 = keyBase64
        )

        coEvery { loadKeyHandler(LoadKeyQuery(keyId)) } returns Result.success(keyView)

        // When
        val result = presenter.loadKey(keyId)

        // Then
        assertTrue(result.isSuccess)
        val keyInfo = result.getOrThrow()
        assertEquals(keyId, keyInfo.keyId)
        assertEquals(keyBase64, keyInfo.keyBase64)
        assertEquals(algorithm, keyInfo.key.algorithm)

        coVerify(exactly = 1) { loadKeyHandler(LoadKeyQuery(keyId)) }
    }

    @Test
    fun `loadKey should return failure when key is not found`() = runTest {
        // Given
        val keyId = "non-existent-key"
        val error = Exception("Key not found")

        coEvery { loadKeyHandler(LoadKeyQuery(keyId)) } returns Result.failure(error)

        // When
        val result = presenter.loadKey(keyId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { loadKeyHandler(LoadKeyQuery(keyId)) }
    }

    @Test
    fun `deleteKey should return success when deletion succeeds`() = runTest {
        // Given
        val keyId = "test-key-id"
        coEvery { deleteKeyHandler(DeleteKeyCommand(keyId)) } returns Result.success(Unit)

        // When
        val result = presenter.deleteKey(keyId)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { deleteKeyHandler(DeleteKeyCommand(keyId)) }
    }

    @Test
    fun `deleteKey should return failure when deletion fails`() = runTest {
        // Given
        val keyId = "test-key-id"
        val error = Exception("Delete failed")
        coEvery { deleteKeyHandler(DeleteKeyCommand(keyId)) } returns Result.failure(error)

        // When
        val result = presenter.deleteKey(keyId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { deleteKeyHandler(DeleteKeyCommand(keyId)) }
    }

    @Test
    fun `deleteAllKeys should return success when deletion succeeds`() = runTest {
        // Given
        coEvery { deleteAllKeysHandler(DeleteAllKeysCommand) } returns Result.success(Unit)

        // When
        val result = presenter.deleteAllKeys()

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { deleteAllKeysHandler(DeleteAllKeysCommand) }
    }

    @Test
    fun `deleteAllKeys should return failure when deletion fails`() = runTest {
        // Given
        val error = Exception("Delete all failed")
        coEvery { deleteAllKeysHandler(DeleteAllKeysCommand) } returns Result.failure(error)

        // When
        val result = presenter.deleteAllKeys()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { deleteAllKeysHandler(DeleteAllKeysCommand) }
    }

    @Test
    fun `loadAllKeys should return success with list of keys`() = runTest {
        // Given
        val key1 = KeyView(
            id = "key-1",
            algorithm = EncryptionAlgorithm.AES_128,
            keyBase64 = Base64.getEncoder().encodeToString(ByteArray(16))
        )
        val key2 = KeyView(
            id = "key-2",
            algorithm = EncryptionAlgorithm.AES_256,
            keyBase64 = Base64.getEncoder().encodeToString(ByteArray(32))
        )
        val keyViews = listOf(key1, key2)

        coEvery { loadAllKeysHandler(LoadAllKeysQuery) } returns Result.success(keyViews)

        // When
        val result = presenter.loadAllKeys()

        // Then
        assertTrue(result.isSuccess)
        val keyItems = result.getOrThrow()
        assertEquals(2, keyItems.size)
        assertEquals("key-1", keyItems[0].id)
        assertEquals(EncryptionAlgorithm.AES_128, keyItems[0].algorithm)
        assertEquals("key-2", keyItems[1].id)
        assertEquals(EncryptionAlgorithm.AES_256, keyItems[1].algorithm)

        coVerify(exactly = 1) { loadAllKeysHandler(LoadAllKeysQuery) }
    }

    @Test
    fun `loadAllKeys should return empty list when no keys exist`() = runTest {
        // Given
        coEvery { loadAllKeysHandler(LoadAllKeysQuery) } returns Result.success(emptyList())

        // When
        val result = presenter.loadAllKeys()

        // Then
        assertTrue(result.isSuccess)
        val keyItems = result.getOrThrow()
        assertTrue(keyItems.isEmpty())

        coVerify(exactly = 1) { loadAllKeysHandler(LoadAllKeysQuery) }
    }

    @Test
    fun `loadAllKeys should return failure when handler fails`() = runTest {
        // Given
        val error = Exception("Failed to load keys")
        coEvery { loadAllKeysHandler(LoadAllKeysQuery) } returns Result.failure(error)

        // When
        val result = presenter.loadAllKeys()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { loadAllKeysHandler(LoadAllKeysQuery) }
    }
}
