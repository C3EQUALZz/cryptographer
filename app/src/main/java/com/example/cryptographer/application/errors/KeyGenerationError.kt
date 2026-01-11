package com.example.cryptographer.application.errors

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * Error thrown when a key generation operation fails.
 *
 * This error is thrown by command handlers when attempting to generate a key
 * but the generation operation fails (e.g., due to algorithm issues, insufficient resources).
 *
 * @param algorithm The encryption algorithm for which key generation failed
 * @param cause The underlying cause of the failure
 */
class KeyGenerationError(
    val algorithm: EncryptionAlgorithm,
    cause: Throwable? = null,
) : ApplicationError("Failed to generate key: algorithm=$algorithm", cause)

