package com.example.cryptographer.application.common.views

/**
 * View representing decrypted text result.
 *
 * This is a View in CQRS pattern - it represents
 * the result of a decryption command.
 */
data class DecryptedTextView(
    val decryptedText: String,
)
