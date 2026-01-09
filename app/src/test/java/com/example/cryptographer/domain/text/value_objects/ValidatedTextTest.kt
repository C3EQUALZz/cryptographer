package com.example.cryptographer.domain.text.value_objects

import com.example.cryptographer.domain.common.errors.DomainFieldError
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ValidatedText value object.
 */
class ValidatedTextTest {

    @Test
    fun `create should succeed with valid text`() {
        // Given
        val validText = "Hello, World!"

        // When
        val result = ValidatedText.create(validText)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(validText.trim(), result.getOrThrow().content)
    }

    @Test
    fun `create should fail with blank text`() {
        // Given
        val blankText = "   "

        // When
        val result = ValidatedText.create(blankText)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `create should fail with empty text`() {
        // Given
        val emptyText = ""

        // When
        val result = ValidatedText.create(emptyText)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `create should fail with text exceeding max length`() {
        // Given
        val longText = "a".repeat(1_000_001) // Exceeds MAX_TEXT_LENGTH

        // When
        val result = ValidatedText.create(longText)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `create should normalize whitespace`() {
        // Given
        val textWithExtraSpaces = "Hello    World   Test"

        // When
        val result = ValidatedText.create(textWithExtraSpaces)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Hello World Test", result.getOrThrow().content)
    }

    @Test
    fun `create should normalize line breaks`() {
        // Given
        val textWithLineBreaks = "Line1\r\nLine2\rLine3"

        // When
        val result = ValidatedText.create(textWithLineBreaks)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().content.contains("\n"))
        assertFalse(result.getOrThrow().content.contains("\r"))
    }

    @Test
    fun `toBytes should convert text to UTF-8 bytes`() {
        // Given
        val text = "Test text"
        val validatedText = ValidatedText.create(text).getOrThrow()

        // When
        val bytes = validatedText.toBytes()

        // Then
        assertNotNull(bytes)
        assertEquals(text, String(bytes, Charsets.UTF_8))
    }

    @Test
    fun `equals should return true for same content`() {
        // Given
        val text1 = ValidatedText.create("Test").getOrThrow()
        val text2 = ValidatedText.create("Test").getOrThrow()

        // Then
        assertEquals(text1, text2)
    }

    @Test
    fun `equals should return false for different content`() {
        // Given
        val text1 = ValidatedText.create("Test1").getOrThrow()
        val text2 = ValidatedText.create("Test2").getOrThrow()

        // Then
        assertNotEquals(text1, text2)
    }

    @Test
    fun `hashCode should be same for same content`() {
        // Given
        val text1 = ValidatedText.create("Test").getOrThrow()
        val text2 = ValidatedText.create("Test").getOrThrow()

        // Then
        assertEquals(text1.hashCode(), text2.hashCode())
    }
}

