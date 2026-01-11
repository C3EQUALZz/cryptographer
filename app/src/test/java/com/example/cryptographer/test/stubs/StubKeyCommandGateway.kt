package com.example.cryptographer.test.stubs

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Stub implementation of KeyCommandGateway for testing.
 * Stores keys in memory for testing purposes.
 *
 * Note: This stub also implements KeyQueryGateway methods internally
 * to support deleteAllKeys() which needs to query for all key IDs.
 */
class StubKeyCommandGateway(
    private val queryGateway: StubKeyQueryGateway? = null,
) : KeyCommandGateway {
    private val keys = mutableMapOf<String, EncryptionKey>()
    private var shouldFailSave = false
    private var shouldFailDelete = false
    private var shouldFailDeleteAll = false

    override fun saveKey(keyId: String, key: EncryptionKey): Boolean {
        if (shouldFailSave) {
            return false
        }
        keys[keyId] = key
        queryGateway?.addKey(keyId, key)
        return true
    }

    override fun deleteKey(keyId: String): Boolean {
        if (shouldFailDelete) {
            return false
        }
        val removed = keys.remove(keyId) != null
        queryGateway?.removeKey(keyId)
        return removed
    }

    override fun deleteAllKeys(): Boolean {
        if (shouldFailDeleteAll) {
            return false
        }
        // Get all key IDs from query gateway if available, otherwise use local keys
        val keyIds = queryGateway?.getAllKeyIds() ?: keys.keys.toList()
        keys.clear()
        keyIds.forEach { queryGateway?.removeKey(it) }
        return true
    }

    /**
     * Sets whether delete operations should fail.
     */
    fun setShouldFailDelete(shouldFail: Boolean) {
        shouldFailDelete = shouldFail
    }

    /**
     * Sets whether deleteAll operations should fail.
     */
    fun setShouldFailDeleteAll(shouldFail: Boolean) {
        shouldFailDeleteAll = shouldFail
    }

}
