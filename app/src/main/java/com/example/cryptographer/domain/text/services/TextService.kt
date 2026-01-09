package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.services.DomainService
import com.example.cryptographer.domain.text.entities.Text
import com.example.cryptographer.domain.text.value_objects.TextEncoding
import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import com.example.cryptographer.domain.text.value_objects.ValidatedText
import com.example.cryptographer.setup.configs.getLogger

/**
 * Domain service for Text entity.
 *
 * Encapsulates complex logic for creating Text entities.
 * Uses ports for external dependencies (ID generation).
 *
 */
class TextService(
    private val textIdGenerator: TextIdGeneratorPort
) : DomainService() {
    private val logger = getLogger<TextService>()

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
            logger.d("Creating Text entity: length=${rawText.length}, encoding=$encoding")

            // Validate text using Value Object
            val validatedText = ValidatedText.create(rawText).getOrElse { error ->
                logger.e("Text validation failed: ${error.message}", error)
                return Result.failure(error)
            }

            // Generate unique ID using ports
            val textId = textIdGenerator.generate()
            logger.d("Generated text ID: $textId")

            // Create Text entity with ID
            val text = Text(
                id = textId,
                content = validatedText,
                encoding = encoding
            )

            logger.i("Text entity created successfully: id=$textId, length=${text.length}")
            Result.success(text)
        } catch (e: Exception) {
            logger.e("Error creating Text entity: ${e.message}", e)
            Result.failure(e)
        }
    }
}
