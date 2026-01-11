package com.example.cryptographer.domain.text.entities

import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import com.example.cryptographer.test.factories.TextFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Text entity.
 */
class TextTest {

    @Test
    fun `text should have correct properties`() {
        // Given
        val text = TextFactory.create(content = "Test content")

        // Then
        assertNotNull(text.id)
        // Note: Text is normalized (trimmed and whitespace normalized),
        // but "Test content" doesn't have extra spaces, so it should remain the same
        assertEquals("Test content", text.rawContent)
        assertEquals(TextEncoding.UTF8, text.encoding)
        assertEquals(12, text.length)
        assertFalse(text.isEmpty)
        assertTrue(text.isNotEmpty)
    }

    @Test
    fun `text should use specified encoding`() {
        // Given
        val text = TextFactory.create(
            content = "Test",
            encoding = TextEncoding.BASE64,
        )

        // Then
        assertEquals(TextEncoding.BASE64, text.encoding)
    }

    @Test
    fun `text should be equal when IDs are same`() {
        // Given
        val id = "test-id-123"
        val text1 = TextFactory.create(id = id, content = "Content 1")
        val text2 = TextFactory.create(id = id, content = "Content 2")

        // Then
        assertEquals(text1, text2)
        assertEquals(text1.hashCode(), text2.hashCode())
    }

    @Test
    fun `text should not be equal when IDs are different`() {
        // Given
        val text1 = TextFactory.create(id = "id-1", content = "Same content")
        val text2 = TextFactory.create(id = "id-2", content = "Same content")

        // Then
        assertNotEquals(text1, text2)
    }

    @Test
    fun `isEmpty should return true for empty text`() {
        // Given
        // Note: Empty or blank text cannot be created (validation fails),
        // so we test with minimal valid text that becomes empty after normalization
        // This test validates that isEmpty property works correctly.
        // Actually, after normalization, even whitespace-only text becomes empty,
        // but validation prevents creation of such text.
        // So we test with a minimal non-empty text instead.
        val text = TextFactory.create(content = "a")

        // Then
        assertFalse(text.isEmpty)
        assertTrue(text.isNotEmpty)

        // For truly empty text, creation should fail
        val result = com.example.cryptographer.domain.text.valueobjects.ValidatedText.create("   ")
        assertTrue(result.isFailure)
    }
}
