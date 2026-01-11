package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.DomainError
import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.Text
import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import com.example.cryptographer.domain.text.valueobjects.ValidatedText
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64

/**
 * Domain service for Text entity.
 *
 * Encapsulates complex logic for creating Text entities.
 * Uses ports for external dependencies (ID generation).
 *
 */
class TextService(
    private val textIdGenerator: TextIdGeneratorPort,
) : DomainService() {
    private val logger = KotlinLogging.logger {}

    /**
     * Creates a new Text entity from raw string.
     *
     * This method:
     * 1. Validates the text using ValidatedText Value Object
     * 2. Generates a unique ID using TextIdGeneratorPort
     * 3. Creates and returns the Text entity
     *
     * @param rawText Raw text string to validate and create Text from
     * @param encoding Text encoding (defaults to UTF8)
     * @return Result with Text if validation succeeds, or error if validation fails
     */
    fun create(rawText: String, encoding: TextEncoding = TextEncoding.UTF8): Result<Text> {
        return try {
            logger.debug { "Creating Text entity: length=${rawText.length}, encoding=$encoding" }

            // Validate text using Value Object
            val validatedText = ValidatedText.create(rawText).getOrElse { error ->
                logger.error(error) { "Text validation failed: ${error.message}" }
                return Result.failure(error)
            }

            // Generate unique ID using ports
            val textId = textIdGenerator.generate()
            logger.debug { "Generated text ID: $textId" }

            // Create Text entity with ID
            val text = Text(
                id = textId,
                content = validatedText,
                encoding = encoding,
            )

            logger.info { "Text entity created successfully: id=$textId, length=${text.length}" }
            Result.success(text)
        } catch (e: DomainError) {
            logger.error(e) { "Error creating Text entity: ${e.message}" }
            Result.failure(e)
        }
    }

    companion object {
        private const val MAX_ASCII_CODE = 127
    }

    /**
     * Converts text to the specified encoding.
     *
     * @param rawText Text to convert
     * @param targetEncoding Target encoding
     * @return Result with converted text string or error
     */
    fun convertEncoding(rawText: String, targetEncoding: TextEncoding): Result<String> {
        return try {
            logger.debug {
                "Converting text encoding: " +
                    "length=${rawText.length}, " +
                    "targetEncoding=$targetEncoding"
            }

            val converted = when (targetEncoding) {
                TextEncoding.UTF8 -> convertToUtf8(rawText)
                TextEncoding.ASCII -> convertToAscii(rawText)
                TextEncoding.BASE64 -> convertToBase64(rawText)
            }

            logger.info {
                "Text encoding conversion successful: " +
                    "targetEncoding=$targetEncoding, " +
                    "convertedLength=${converted.length}"
            }
            Result.success(converted)
        } catch (e: DomainError) {
            logger.error(e) { "Error converting text encoding: ${e.message}" }
            Result.failure(e)
        }
    }

    private fun convertToUtf8(rawText: String): String {
        // If input is BASE64, decode it first
        if (isBase64(rawText)) {
            return try {
                val decoded = Base64.getDecoder().decode(rawText)
                String(decoded, Charsets.UTF_8)
            } catch (_: Exception) {
                // If Base64 decode fails, treat as UTF-8
                rawText
            }
        }
        return rawText
    }

    private fun convertToAscii(rawText: String): String {
        // Convert to ASCII (only characters 0-127)
        return rawText.map { char ->
            if (char.code > MAX_ASCII_CODE) {
                '?' // Replace non-ASCII characters
            } else {
                char
            }
        }.joinToString("")
    }

    private fun convertToBase64(rawText: String): String {
        // Encode to BASE64
        val bytes = rawText.toByteArray(Charsets.UTF_8)
        return Base64.getEncoder().encodeToString(bytes)
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
