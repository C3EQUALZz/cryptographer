package com.example.cryptographer.application.errors

import com.example.cryptographer.domain.common.errors.AppError

/**
 * Base exception for all application layer errors.
 *
 * All application-specific exceptions should inherit from this class.
 * Application errors represent use case and command/query handler errors.
 *
 * Examples:
 * - KeyNotFoundError - when a key is not found during a query
 * - InvalidKeyError - when a key is invalid for an operation
 * - CommandValidationError - when a command fails validation
 * - QueryValidationError - when a query fails validation
 */
open class ApplicationError(
    message: String,
    cause: Throwable? = null,
) : AppError(message, cause)
