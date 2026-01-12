package com.example.cryptographer.integration.key

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.fixtures.TestFixtures
import com.example.cryptographer.infrastructure.key.KeyCommandGatewayAdapter
import com.example.cryptographer.infrastructure.key.KeyQueryGatewayAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for key management functionality.
 *
 * These tests verify that key storage and retrieval work correctly
 * in a real Android environment using SharedPreferences.
 *
 * Category: Integration Tests
 * Scope: Key persistence and retrieval
 */
@RunWith(AndroidJUnit4::class)
class KeyManagementIntegrationTest {

    private lateinit var keyCommandGateway: KeyCommandGateway
    private lateinit var keyQueryGateway: KeyQueryGateway
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        // Create query gateway first (needed by command gateway)
        keyQueryGateway = KeyQueryGatewayAdapter(appContext)
        // Create command gateway with query gateway dependency
        keyCommandGateway = KeyCommandGatewayAdapter(appContext, keyQueryGateway)

        // Clear all keys before each test
        val allKeyIds = keyQueryGateway.getAllKeyIds()
        allKeyIds.forEach { keyId ->
            keyCommandGateway.deleteKey(keyId)
        }
    }

    @Test
    fun saveAndLoadKey_shouldWorkCorrectly() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)

        // When
        val saveResult = keyCommandGateway.saveKey(key.id, key)
        assertTrue("Key save should succeed", saveResult)

        val loadedKey = keyQueryGateway.getKey(key.id)

        // Then
        assertNotNull("Loaded key should not be null", loadedKey)
        assertEquals("Key ID should match", key.id, loadedKey!!.id)
        assertEquals("Key algorithm should match", key.algorithm, loadedKey.algorithm)
        assertTrue("Key bytes should match", key.value.contentEquals(loadedKey.value))
    }

    @Test
    fun getAllKeyIds_shouldReturnAllSavedKeyIds() {
        // Given
        val key1 = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val key2 = TestFixtures.createEncryptionKey(EncryptionAlgorithm.AES_256)
        val key3 = TestFixtures.createEncryptionKey(EncryptionAlgorithm.AES_128)

        // When
        keyCommandGateway.saveKey(key1.id, key1)
        keyCommandGateway.saveKey(key2.id, key2)
        keyCommandGateway.saveKey(key3.id, key3)

        val allKeyIds = keyQueryGateway.getAllKeyIds()

        // Then
        assertTrue("Should have at least 3 keys", allKeyIds.size >= 3)

        val keyIdsSet = allKeyIds.toSet()
        assertTrue("Should contain key1", keyIdsSet.contains(key1.id))
        assertTrue("Should contain key2", keyIdsSet.contains(key2.id))
        assertTrue("Should contain key3", keyIdsSet.contains(key3.id))
    }

    @Test
    fun deleteKey_shouldRemoveKey() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        keyCommandGateway.saveKey(key.id, key)

        // When
        val deleteResult = keyCommandGateway.deleteKey(key.id)
        assertTrue("Key delete should succeed", deleteResult)

        val loadedKey = keyQueryGateway.getKey(key.id)

        // Then
        assertNull("Key should be null after deletion", loadedKey)
    }

    @Test
    fun deleteAllKeys_shouldRemoveAllKeys() {
        // Given
        val key1 = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val key2 = TestFixtures.createEncryptionKey(EncryptionAlgorithm.AES_256)

        keyCommandGateway.saveKey(key1.id, key1)
        keyCommandGateway.saveKey(key2.id, key2)

        // When
        val deleteResult = keyCommandGateway.deleteAllKeys()
        assertTrue("Delete all keys should succeed", deleteResult)

        val allKeyIds = keyQueryGateway.getAllKeyIds()

        // Then
        assertTrue("Should have no keys after deletion", allKeyIds.isEmpty())
    }
}
