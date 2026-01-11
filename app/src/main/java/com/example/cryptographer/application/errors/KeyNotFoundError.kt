package com.example.cryptographer.application.errors

/**
 * Error thrown when a requested key is not found.
 *
 * This error is thrown by query handlers when attempting to load a key
 * that doesn't exist in the system.
 *
 * @param keyId The identifier of the key that was not found
 */
class KeyNotFoundError(
    val keyId: String,
) : ApplicationError("Key not found: keyId=$keyId")

