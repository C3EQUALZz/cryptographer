package com.example.cryptographer.application.errors

/**
 * Error thrown when a key delete operation fails.
 *
 * This error is thrown by command handlers when attempting to delete a key
 * but the delete operation fails.
 *
 * @param keyId The identifier of the key that failed to delete
 */
class KeyDeleteError(
    val keyId: String,
    cause: Throwable? = null,
) : ApplicationError("Failed to delete key: keyId=$keyId", cause)
