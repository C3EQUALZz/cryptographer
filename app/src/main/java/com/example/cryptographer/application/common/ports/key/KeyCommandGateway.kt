package com.example.cryptographer.application.common.ports.key

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Gateway for key-related command operations (write operations).
 *
 * This is a Gateway in CQRS pattern - it defines the interface
 * for write operations (commands) that modify state.
 *
 * Following Clean Architecture principles:
 * - Interface is in Application layer (application boundary)
 * - Implementation will be in Infrastructure layer
 * - Domain layer doesn't know about this gateway
 */
interface KeyCommandGateway {
    /**
     * Saves an encryption key.
     *
     * @param keyId Unique identifier for the key
     * @param key Encryption key to save
     * @return true if key was saved successfully, false otherwise
     */
    fun saveKey(keyId: String, key: EncryptionKey): Boolean

    /**
     * Deletes an encryption key by ID.
     *
     * @param keyId Key identifier
     * @return true if key was deleted successfully, false otherwise
     */
    fun deleteKey(keyId: String): Boolean

    /**
     * Deletes all encryption keys.
     *
     * @return true if all keys were deleted successfully, false otherwise
     */
    fun deleteAllKeys(): Boolean
}

