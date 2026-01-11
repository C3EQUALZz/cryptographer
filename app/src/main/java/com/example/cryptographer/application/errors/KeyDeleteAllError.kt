package com.example.cryptographer.application.errors

/**
 * Error thrown when delete all keys operation fails.
 *
 * This error is thrown by command handlers when attempting to delete all keys
 * but the delete operation fails.
 */
class KeyDeleteAllError(
    cause: Throwable? = null,
) : ApplicationError("Failed to delete all keys", cause)

