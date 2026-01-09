package com.example.cryptographer.domain.common.errors

/**
 * Base exception for all domain errors.
 *
 * All domain-specific exceptions should inherit from this class.
 * This provides a common base for error handling in the domain layer.
 */
open class DomainError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
