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

    init {
        // Validation must be performed after field initialization
        // (BaseValueObject.init() is called before fields are initialized)
        if (content.isBlank()) {
            throw DomainFieldError("ValidatedText content cannot be blank")
        }
    }

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
                        // Check if normalized content is not blank (it might become blank after normalization)
                        if (normalizedContent.isBlank()) {
                            logger.w("Text validation failed: text becomes blank after normalization")
                            Result.failure(
                                DomainFieldError("Text cannot be empty")
                            )
                        } else {
                            logger.d("Text validation successful: length=${normalizedContent.length}")
                            Result.success(ValidatedText(normalizedContent))
                        }
                    }
                }
            } catch (e: Exception) {
                logger.e("Text validation error: ${e.message}", e)
                Result.failure(e)
            }
        }

        /**
         * Normalizes text for encryption:
         * - Normalizes line breaks to \n (must be done first)
         * - Normalizes multiple spaces/tabs to single space (but preserves line breaks)
         * - Trims whitespace from start and end (but preserves line breaks in the middle)
         */
        private fun normalizeText(content: String): String {
            return content
                .replace("\r\n", "\n") // Normalize Windows line breaks first
                .replace("\r", "\n") // Normalize Mac line breaks
                .replace(Regex("[ \t]+"), " ") // Replace multiple spaces/tabs with one space (but not \n)
                .trim() // Remove whitespace from start and end (but preserve \n in the middle)
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
            } catch (_: Exception) {
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
        // and also in init{} block after field initialization.
        // This method is called by BaseValueObject.init(), but at that point
        // fields are not yet initialized, so we do nothing here.
        // Actual validation happens in init{} block and in create() method.
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
