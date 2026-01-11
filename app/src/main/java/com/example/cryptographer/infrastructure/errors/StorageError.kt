package com.example.cryptographer.infrastructure.errors

/**
 * Error thrown when storage operations fail.
 *
 * This error is thrown when there are issues with storing or retrieving data
 * from the storage system (e.g., SharedPreferences, database, file system).
 *
 * @param operation The storage operation that failed (e.g., "save", "load", "delete")
 * @param details Additional details about the failure
 */
class StorageError(
    val operation: String,
    details: String? = null,
    cause: Throwable? = null,
) : InfrastructureError(
    message = "Storage operation failed: operation=$operation${if (details != null) ", details=$details" else ""}",
    cause = cause,
)
