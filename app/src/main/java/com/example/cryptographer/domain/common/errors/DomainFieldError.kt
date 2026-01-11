package com.example.cryptographer.domain.common.errors

/**
 * Error thrown when a domain field validation fails.
 *
 * Used for value object and entity field validation errors.
 */
class DomainFieldError(
    message: String,
    cause: Throwable? = null,
) : DomainError(message, cause)
