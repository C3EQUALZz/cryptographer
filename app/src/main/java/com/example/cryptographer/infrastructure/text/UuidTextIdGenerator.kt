package com.example.cryptographer.infrastructure.text

import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import java.util.UUID

/**
 * Infrastructure adapter for generating text IDs.
 *
 * Implements TextIdGeneratorPort using UUID.
 * This is an adapter in Hexagonal Architecture - it implements the ports
 * defined in the domain layer.
 */
class UuidTextIdGenerator : TextIdGeneratorPort {
    /**
     * Generates a unique text ID using UUID.
     *
     * @return A unique text identifier (UUID string)
     */
    override fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
