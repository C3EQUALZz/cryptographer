package com.example.cryptographer.domain.text.value_objects

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.common.values.BaseValueObject
import com.example.cryptographer.setup.configs.getLogger

/**
 * Value Object for validated text.
 * Encapsulates validation logic and ensures text meets requirements for cryptographic operations.
 *
 * This is a Value Object following DDD principles - it validates on creation and is immutable.
 */
class ValidatedText private constructor(
    val content: String
) : BaseValueObject() {
    companion object {
        private val logger = getLogger<ValidatedText>()
        private const val MAX_TEXT_LENGTH = 1_000_000 // approximately 1MB

        /**
         * Creates a validated text instance.
         * Validates the text and returns Result with ValidatedText or validation error.
         *
         * @param rawText Raw text string to validate
         * @return Result with ValidatedText if valid, or error if validation fails
         */
        fun create(rawText: String): Result<ValidatedText> {
            return try {
                when {
                    rawText.isBlank() -> {
                        logger.w("Text validation failed: text is blank")
                        Result.failure(
                            DomainFieldError("Text cannot be empty")
                        )
                    }
                    rawText.length > MAX_TEXT_LENGTH -> {
                        logger.w("Text validation failed: text exceeds maximum length (${rawText.length} > $MAX_TEXT_LENGTH)")
                        Result.failure(
                            DomainFieldError("Text exceeds maximum length: $MAX_TEXT_LENGTH characters")
                        )
                    }
                    !isValidUtf8(rawText) -> {
                        logger.w("Text validation failed: invalid UTF-8 encoding")
                        Result.failure(
                            DomainFieldError("Text contains invalid UTF-8 characters")
                        )
                    }
                    else -> {
                        // Normalize text (trim, normalize whitespace and line breaks)
                        val normalizedContent = normalizeText(rawText)
                        logger.d("Text validation successful: length=${normalizedContent.length}")
                        Result.success(ValidatedText(normalizedContent))
                    }
                }
            } catch (e: Exception) {
                logger.e("Text validation error: ${e.message}", e)
                Result.failure(e)
            }
        }

        /**
         * Normalizes text for encryption:
         * - Trims whitespace
         * - Normalizes multiple spaces to single space
         * - Normalizes line breaks to \n
         */
        private fun normalizeText(content: String): String {
            return content
                .trim()
                .replace(Regex("\\s+"), " ") // Replace multiple spaces with one
                .replace("\r\n", "\n") // Normalize line breaks
                .replace("\r", "\n")
        }

        /**
         * Validates that text contains valid UTF-8 characters.
         */
        private fun isValidUtf8(text: String): Boolean {
            return try {
                // Try to encode and decode to check UTF-8 validity
                val bytes = text.toByteArray(Charsets.UTF_8)
                val decoded = String(bytes, Charsets.UTF_8)
                decoded == text
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Converts validated text to bytes for encryption.
     */
    fun toBytes(): ByteArray {
        return content.toByteArray(Charsets.UTF_8)
    }

    override fun validate() {
        // Validation is performed in create() method before construction
        // This method is called by BaseValueObject.init(), but validation
        // has already been done in create(), so we just ensure content is not blank
        if (content.isBlank()) {
            throw DomainFieldError("ValidatedText content cannot be blank")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValidatedText) return false
        return content == other.content
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    override fun toString(): String {
        return "ValidatedText(length=${content.length})"
    }
}
