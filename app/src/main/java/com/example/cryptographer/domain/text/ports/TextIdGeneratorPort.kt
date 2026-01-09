package com.example.cryptographer.domain.text.ports

/**
 * Port (interface) for generating unique text IDs.
 *
 * This is a ports in Hexagonal Architecture - it defines the interface
 * for generating IDs, but the implementation will be provided by the infrastructure layer.
 *
 * Similar to Python's Protocol, this interface defines the contract
 * that must be implemented by adapters.
 */
interface TextIdGeneratorPort {
    /**
     * Generates a unique text ID.
     *
     * @return A unique text identifier
     */
    fun generate(): String
}

