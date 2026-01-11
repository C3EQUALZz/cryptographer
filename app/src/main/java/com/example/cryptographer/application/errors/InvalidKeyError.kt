package com.example.cryptographer.application.errors

/**
 * Error thrown when a key is invalid for a specific operation.
 *
 * This error is thrown when a key exists but cannot be used for the requested operation.
 * Examples:
 * - Key algorithm doesn't match the required algorithm
 * - Key is corrupted or invalid format
 *
 * @param keyId The identifier of the invalid key
 * @param reason The reason why the key is invalid
 */
class InvalidKeyError(
    val keyId: String,
    val reason: String,
) : ApplicationError("Invalid key: keyId=$keyId, reason=$reason")

