package com.example.cryptographer.application.errors

/**
 * Error raised when file reading fails.
 */
class FileReadError(
    message: String,
    cause: Throwable? = null,
) : ApplicationError(message, cause)
