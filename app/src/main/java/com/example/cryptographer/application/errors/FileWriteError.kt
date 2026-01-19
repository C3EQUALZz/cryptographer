package com.example.cryptographer.application.errors

/**
 * Error raised when file writing fails.
 */
class FileWriteError(
    message: String,
    cause: Throwable? = null,
) : ApplicationError(message, cause)
