package com.example.cryptographer.application.commands.text.convertencoding

import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Base64

/**
 * Unit tests for ConvertTextEncodingCommandHandler.
 */
class ConvertTextEncodingCommandHandlerTest {

    private lateinit var handler: ConvertTextEncodingCommandHandler
    private lateinit var service: TextService

    @Before
    fun setUp() {
        val textIdGenerator: TextIdGeneratorPort = mockk()
        service = TextService(textIdGenerator)
        handler = ConvertTextEncodingCommandHandler(service)
    }

    @Test
    fun `invoke should convert to BASE64 successfully`() {
        // Given
        val rawText = "Hello, World!"
        val command = ConvertTextEncodingCommand(rawText, TextEncoding.BASE64)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val view = result.getOrThrow()
        val expectedBase64 = Base64.getEncoder().encodeToString(rawText.toByteArray(Charsets.UTF_8))
        assertEquals(expectedBase64, view.convertedText)
    }

    @Test
    fun `invoke should convert from BASE64 to UTF8 successfully`() {
        // Given
        val originalText = "Hello, World!"
        val base64Text = Base64.getEncoder().encodeToString(originalText.toByteArray(Charsets.UTF_8))
        val command = ConvertTextEncodingCommand(base64Text, TextEncoding.UTF8)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val view = result.getOrThrow()
        assertEquals(originalText, view.convertedText)
    }

    @Test
    fun `invoke should convert to ASCII successfully`() {
        // Given
        val rawText = "Hello123"
        val command = ConvertTextEncodingCommand(rawText, TextEncoding.ASCII)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val view = result.getOrThrow()
        assertEquals(rawText, view.convertedText)
    }

    @Test
    fun `invoke should replace non-ASCII characters with question mark`() {
        // Given
        val rawText = "Привет"
        val command = ConvertTextEncodingCommand(rawText, TextEncoding.ASCII)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val view = result.getOrThrow()
        assertTrue(view.convertedText.all { it.code <= 127 })
        assertTrue(view.convertedText.contains('?'))
    }
}
