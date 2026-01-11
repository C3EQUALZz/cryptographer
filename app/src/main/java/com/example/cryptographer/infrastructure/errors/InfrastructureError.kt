package com.example.cryptographer.infrastructure.errors

import com.example.cryptographer.domain.common.errors.AppError

/**
 * Base exception for all infrastructure layer errors.
 *
 * All infrastructure-specific exceptions should inherit from this class.
 * Infrastructure errors represent technical failures in external systems,
 * persistence, network, and other infrastructure concerns.
 *
 * Examples:
 * - StorageError - when storage operations fail
 * - PersistenceError - when data persistence fails
 * - ConfigurationError - when infrastructure configuration is invalid
 * - SerializationError - when data serialization/deserialization fails
 */
open class InfrastructureError(
    message: String,
    cause: Throwable? = null,
) : AppError(message, cause)
