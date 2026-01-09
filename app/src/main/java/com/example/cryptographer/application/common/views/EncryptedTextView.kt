package com.example.cryptographer.application.common.views

import com.example.cryptographer.domain.text.entities.EncryptedText

/**
 * View representing encrypted text result.
 *
 * This is a View in CQRS pattern - it represents
 * the result of an encryption command.
 */
data class EncryptedTextView(
    val encryptedText: EncryptedText
)

