package com.example.cryptographer.application.common.ports.key

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Gateway for key-related query operations (read operations).
 *
 * This is a Gateway in CQRS pattern - it defines the interface
 * for read operations (queries) that don't modify state.
 *
 * Following Clean Architecture principles:
 * - Interface is in Application layer (application boundary)
 * - Implementation will be in Infrastructure layer
 * - Domain layer doesn't know about this gateway
 */
interface KeyQueryGateway {
    /**
     * Retrieves an encryption key by identifier.
     *
     * @param keyId Key identifier
     * @return Encryption key if found, null otherwise
     */
    fun getKey(keyId: String): EncryptionKey?

    /**
     * Gets all saved key identifiers.
     *
     * @return List of all saved key IDs
     */
    fun getAllKeyIds(): List<String>
}

