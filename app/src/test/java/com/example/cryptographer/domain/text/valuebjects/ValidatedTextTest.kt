package com.example.cryptographer.domain.text.valuebjects

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.text.valueobjects.ValidatedText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        // Note: Text is normalized (trimmed and whitespace normalized)
        assertEquals("Hello, World!", result.getOrThrow().content)
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
        // Note: After normalization, multiple spaces are replaced with single space

        // When
        val result = ValidatedText.create(textWithExtraSpaces)

        // Then
        assertTrue(result.isSuccess)
        val validatedText = result.getOrThrow()
        // Text is normalized: trimmed and multiple spaces replaced with single space
        assertEquals("Hello World Test", validatedText.content)
    }

    @Test
    fun `create should normalize line breaks`() {
        // Given
        val textWithLineBreaks = "Line1\r\nLine2\rLine3"
        // Expected after normalization: "Line1\nLine2\nLine3"

        // When
        val result = ValidatedText.create(textWithLineBreaks)

        // Then
        assertTrue(result.isSuccess)
        val validatedText = result.getOrThrow()
        // Line breaks are normalized to \n (Windows \r\n and Mac \r become \n)
        assertEquals("Line1\nLine2\nLine3", validatedText.content)
        assertTrue(validatedText.content.contains("\n"))
        assertFalse(validatedText.content.contains("\r"))
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
        // Note: Text is normalized, so we check against normalized content
        assertEquals(validatedText.content, String(bytes, Charsets.UTF_8))
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
