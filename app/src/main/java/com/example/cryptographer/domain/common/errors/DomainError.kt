package com.example.cryptographer.domain.common.errors

/**
 * Base exception for all domain errors.
 *
 * All domain-specific exceptions should inherit from this class.
 * This provides a common base for error handling in the domain layer.
 *
 * Domain errors represent business logic violations and invariants.
 * Examples: validation errors, business rule violations, domain constraints.
 */
open class DomainError(
    message: String,
    cause: Throwable? = null,
) : AppError(message, cause)
