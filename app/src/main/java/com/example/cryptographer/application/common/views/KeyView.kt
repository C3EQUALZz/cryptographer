package com.example.cryptographer.application.common.views

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * View representing a key for presentation layer.
 *
 * This is a View in CQRS pattern - it represents
 * the result of a query operation, optimized for presentation.
 */
data class KeyView(
    val id: String,
    val algorithm: EncryptionAlgorithm,
    val keyBase64: String
)

