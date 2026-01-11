package com.example.cryptographer.domain.common.errors

/**
 * Base exception for all application errors.
 *
 * This is the root of the error hierarchy for the entire application.
 * All application-specific errors should inherit from this class:
 * - DomainError - for domain layer errors
 * - ApplicationError - for application layer errors
 * - InfrastructureError - for infrastructure layer errors
 *
 * This provides a common base for error handling across all layers
 * and allows for unified error processing in the presentation layer.
 */
open class AppError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

