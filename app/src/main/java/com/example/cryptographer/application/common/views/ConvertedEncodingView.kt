package com.example.cryptographer.application.common.views

/**
 * View representing converted text encoding result.
 *
 * This is a View in CQRS pattern - it represents
 * the result of an encoding conversion command.
 */
data class ConvertedEncodingView(
    val convertedText: String,
)
