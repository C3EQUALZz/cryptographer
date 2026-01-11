package com.example.cryptographer.application.commands.text.convertencoding

import com.example.cryptographer.application.common.views.ConvertedEncodingView
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64

/**
 * Command Handler for converting text encoding.
 *
 * This is a Command Handler in CQRS pattern - it handles transformation operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Returns View (DTO) for presentation layer
 */
class ConvertTextEncodingCommandHandler {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the ConvertTextEncodingCommand.
     *
     * @param command Command to execute
     * @return Result with ConvertedEncodingView or error
     */
    operator fun invoke(command: ConvertTextEncodingCommand): Result<ConvertedEncodingView> {
        return try {
            logger.debug { "Handling ConvertTextEncodingCommand: length=${command.rawText.length}, targetEncoding=${command.targetEncoding}" }

            val converted = when (command.targetEncoding) {
                TextEncoding.UTF8 -> {
                    // If input is BASE64, decode it first
                    if (isBase64(command.rawText)) {
                        try {
                            val decoded = Base64.getDecoder().decode(command.rawText)
                            String(decoded, Charsets.UTF_8)
                        } catch (_: Exception) {
                            // If Base64 decode fails, treat as UTF-8
                            command.rawText
                        }
                    } else {
                        command.rawText
                    }
                }
                TextEncoding.ASCII -> {
                    // Convert to ASCII (only characters 0-127)
                    command.rawText.map { char ->
                        if (char.code > 127) {
                            '?' // Replace non-ASCII characters
                        } else {
                            char
                        }
                    }.joinToString("")
                }
                TextEncoding.BASE64 -> {
                    // Encode to BASE64
                    val bytes = command.rawText.toByteArray(Charsets.UTF_8)
                    Base64.getEncoder().encodeToString(bytes)
                }
            }

            logger.info { "Text conversion successful: targetEncoding=${command.targetEncoding}, convertedLength=${converted.length}" }
            Result.success(ConvertedEncodingView(converted))
        } catch (e: Exception) {
            logger.error(e) { "Error handling ConvertTextEncodingCommand: ${e.message}" }
            Result.failure(
                Exception("Ошибка конвертации текста: ${e.message}", e)
            )
        }
    }

    /**
     * Checks if a string is valid BASE64.
     */
    private fun isBase64(text: String): Boolean {
        if (text.isBlank()) return false
        return try {
            Base64.getDecoder().decode(text)
            text.matches(Regex("^[A-Za-z0-9+/=]*$"))
        } catch (_: Exception) {
            false
        }
    }
}
