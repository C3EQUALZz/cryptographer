package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.entity.TextEncoding

/**
 * Use Case for preparing text for encryption.
 * Performs normalization and transformation of text before cryptographic operations.
 */
class PrepareTextForEncryptionUseCase(
    private val validateTextUseCase: ValidateTextUseCase
) {
    operator fun invoke(text: Text): Result<Text> {
        return try {
            // Validate text
            validateTextUseCase(text).getOrThrow()

            // Normalize text (remove extra spaces, normalize line breaks)
            val normalizedContent = normalizeText(text.content)

            // Create prepared text
            val preparedText = text.copy(
                content = normalizedContent,
                encoding = TextEncoding.UTF8 // Convert to UTF8 for encryption
            )

            Result.success(preparedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeText(content: String): String {
        return content
            .trim()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with one
            .replace("\r\n", "\n") // Normalize line breaks
            .replace("\r", "\n")
    }
}

