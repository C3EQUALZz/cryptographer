package com.example.cryptographer.test.stubs

import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Stub implementation of KeyQueryGateway for testing.
 * Provides keys from memory for testing purposes.
 */
class StubKeyQueryGateway : KeyQueryGateway {
    private val keys = mutableMapOf<String, EncryptionKey>()

    override fun getKey(keyId: String): EncryptionKey? {
        return keys[keyId]
    }

    override fun getAllKeyIds(): List<String> {
        return keys.keys.toList()
    }

    /**
     * Adds a key to the stub.
     */
    fun addKey(keyId: String, key: EncryptionKey) {
        keys[keyId] = key
    }

    /**
     * Removes a key from the stub.
     */
    fun removeKey(keyId: String) {
        keys.remove(keyId)
    }

    /**
     * Clears all keys.
     */
    fun clear() {
        keys.clear()
    }

    /**
     * Gets all stored keys.
     */
    fun getAllKeys(): Map<String, EncryptionKey> {
        return keys.toMap()
    }
}
