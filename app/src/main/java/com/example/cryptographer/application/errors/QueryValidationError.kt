package com.example.cryptographer.application.errors

/**
 * Error thrown when a query fails validation.
 *
 * This error is thrown by query handlers when a query doesn't meet
 * the required validation rules.
 *
 * @param queryName The name of the query that failed validation
 * @param reason The reason why validation failed
 */
class QueryValidationError(
    val queryName: String,
    val reason: String,
) : ApplicationError("Query validation failed: query=$queryName, reason=$reason")

