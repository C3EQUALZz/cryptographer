package com.example.cryptographer.infrastructure.errors

/**
 * Error thrown when data persistence operations fail.
 *
 * This error is thrown when there are issues with persisting or loading data.
 * It's a more specific form of StorageError for persistence-related failures.
 *
 * @param operation The persistence operation that failed
 * @param resource The resource that failed to persist (e.g., "key", "settings")
 * @param details Additional details about the failure
 */
class PersistenceError(
    val operation: String,
    val resource: String,
    details: String? = null,
    cause: Throwable? = null,
) : InfrastructureError(
    message = "Persistence operation failed: operation=$operation, resource=$resource${if (details != null) ", details=$details" else ""}",
    cause = cause,
)

