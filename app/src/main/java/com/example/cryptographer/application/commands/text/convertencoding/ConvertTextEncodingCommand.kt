package com.example.cryptographer.application.commands.text.convertencoding

import com.example.cryptographer.domain.text.valueobjects.TextEncoding

/**
 * Command for converting text encoding.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a transformation operation.
 */
data class ConvertTextEncodingCommand(
    val rawText: String,
    val targetEncoding: TextEncoding
)
