package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.Text

/**
 * Use Case for text validation.
 * Checks that text meets requirements for cryptographic operations.
 */
class ValidateTextUseCase {
    operator fun invoke(text: Text): Result<Boolean> {
        return try {
            when {
                text.content.isEmpty() -> Result.failure(
                    IllegalArgumentException("Text cannot be empty")
                )
                text.content.length > MAX_TEXT_LENGTH -> Result.failure(
                    IllegalArgumentException("Text exceeds maximum length: $MAX_TEXT_LENGTH characters")
                )
                !isValidEncoding(text) -> Result.failure(
                    IllegalArgumentException("Text contains invalid characters for the selected encoding")
                )
                else -> Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isValidEncoding(text: Text): Boolean {
        return when (text.encoding) {
            com.example.cryptographer.domain.text.entity.TextEncoding.UTF8 -> true
            com.example.cryptographer.domain.text.entity.TextEncoding.ASCII -> 
                text.content.all { it.code < 128 }
            com.example.cryptographer.domain.text.entity.TextEncoding.BASE64 -> 
                text.content.matches(Regex("^[A-Za-z0-9+/=]*$"))
        }
    }

    companion object {
        private const val MAX_TEXT_LENGTH = 1_000_000 // approximately 1MB
    }
}

