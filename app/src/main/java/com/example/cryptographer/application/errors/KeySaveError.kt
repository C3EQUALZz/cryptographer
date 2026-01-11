package com.example.cryptographer.application.errors

/**
 * Error thrown when a key save operation fails.
 *
 * This error is thrown by command handlers when attempting to save a key
 * but the save operation fails (e.g., due to storage issues).
 *
 * @param keyId The identifier of the key that failed to save
 */
class KeySaveError(
    val keyId: String,
    cause: Throwable? = null,
) : ApplicationError("Failed to save key: keyId=$keyId", cause)
