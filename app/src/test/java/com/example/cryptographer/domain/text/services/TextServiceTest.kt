package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import com.example.cryptographer.test.stubs.StubTextIdGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TextService.
 */
class TextServiceTest {

    private lateinit var textIdGenerator: StubTextIdGenerator
    private lateinit var textService: TextService

    @Before
    fun setUp() {
        textIdGenerator = StubTextIdGenerator()
        textService = TextService(textIdGenerator)
    }

    @Test
    fun `create should succeed with valid text`() {
        // Given
        val rawText = "Hello, World!"
        textIdGenerator.setNextId("test-id-123")

        // When
        val result = textService.create(rawText)

        // Then
        assertTrue(result.isSuccess)
        val text = result.getOrThrow()
        assertEquals("test-id-123", text.id)
        // Note: Text is normalized (trimmed and whitespace normalized), so we check normalized version
        assertEquals("Hello, World!", text.rawContent)
        assertEquals(TextEncoding.UTF8, text.encoding)
    }

    @Test
    fun `create should use specified encoding`() {
        // Given
        val rawText = "Test text"
        textIdGenerator.setNextId("test-id-456")

        // When
        val result = textService.create(rawText, TextEncoding.BASE64)

        // Then
        assertTrue(result.isSuccess)
        val text = result.getOrThrow()
        assertEquals(TextEncoding.BASE64, text.encoding)
    }

    @Test
    fun `create should fail with invalid text`() {
        // Given
        val invalidText = "" // Empty text

        // When
        val result = textService.create(invalidText)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `create should generate unique IDs`() {
        // Given
        val rawText = "Test text"
        textIdGenerator.setNextId("id-1")

        // When
        val result1 = textService.create(rawText)
        textIdGenerator.setNextId("id-2")
        val result2 = textService.create(rawText)

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertNotEquals(result1.getOrThrow().id, result2.getOrThrow().id)
    }

    @Test
    fun `create should normalize text`() {
        // Given
        val rawText = "  Hello    World  "
        textIdGenerator.setNextId("test-id")

        // When
        val result = textService.create(rawText)

        // Then
        assertTrue(result.isSuccess)
        val text = result.getOrThrow()
        // Text is normalized: trimmed and multiple spaces replaced with single space
        assertEquals("Hello World", text.rawContent)
    }
}
