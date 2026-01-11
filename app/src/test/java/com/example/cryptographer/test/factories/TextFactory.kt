package com.example.cryptographer.test.factories

import com.example.cryptographer.domain.text.entities.Text
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import com.example.cryptographer.domain.text.valueobjects.ValidatedText
import java.time.Instant
import java.util.UUID

/**
 * Factory for creating test Text entities and Value Objects.
 * Provides convenient methods for creating text objects in tests.
 */
object TextFactory {
    /**
     * Creates a test Text entity with default values.
     */
    fun create(
        id: String = UUID.randomUUID().toString(),
        content: String = "Test text content",
        encoding: TextEncoding = TextEncoding.UTF8,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ): Text {
        // ValidatedText.create() will handle validation.
        // Note: In Kotlin, String is non-nullable, so null cannot be passed at compile time.
        // If you see a warning about null, it may be a false positive from static analysis.
        val validatedText = ValidatedText.create(content)
            .getOrElse { error ->
                throw IllegalArgumentException("Failed to create validated text: ${error.message}", error)
            }
        return Text(
            id = id,
            content = validatedText,
            encoding = encoding,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }


}

