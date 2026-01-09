package com.example.cryptographer.application.common.views

/**
 * View representing a key ID result.
 * 
 * This is a View in CQRS pattern - it represents
 * the result of a command operation (key generation).
 */
data class KeyIdView(
    val keyId: String
)

