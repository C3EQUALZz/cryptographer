package com.example.cryptographer.presentation.encoding

import com.example.cryptographer.application.commands.text.convert_encoding.ConvertTextEncodingCommand
import com.example.cryptographer.application.commands.text.convert_encoding.ConvertTextEncodingCommandHandler
import com.example.cryptographer.application.common.views.ConvertedEncodingView
import com.example.cryptographer.domain.text.value_objects.TextEncoding
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for EncodingPresenter.
 */
class EncodingPresenterTest {

    private lateinit var convertTextEncodingHandler: ConvertTextEncodingCommandHandler
    private lateinit var presenter: EncodingPresenter

    @Before
    fun setUp() {
        convertTextEncodingHandler = mockk()
        presenter = EncodingPresenter(convertTextEncodingHandler)
    }

    @Test
    fun `convertText should return success when conversion succeeds`() = runTest {
        // Given
        val rawText = "Hello, World!"
        val targetEncoding = TextEncoding.BASE64
        val convertedText = "SGVsbG8sIFdvcmxkIQ=="

        val convertedEncodingView = ConvertedEncodingView(convertedText = convertedText)

        coEvery { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) } returns Result.success(convertedEncodingView)

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(convertedText, result.getOrThrow())

        coVerify(exactly = 1) { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) }
    }

    @Test
    fun `convertText should return empty string when input is blank`() = runTest {
        // Given
        val rawText = "   "
        val targetEncoding = TextEncoding.UTF8

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("", result.getOrThrow())

        coVerify(exactly = 0) { convertTextEncodingHandler(any()) }
    }

    @Test
    fun `convertText should return empty string when input is empty`() = runTest {
        // Given
        val rawText = ""
        val targetEncoding = TextEncoding.ASCII

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("", result.getOrThrow())

        coVerify(exactly = 0) { convertTextEncodingHandler(any()) }
    }

    @Test
    fun `convertText should return failure when conversion fails`() = runTest {
        // Given
        val rawText = "Test text"
        val targetEncoding = TextEncoding.BASE64
        val error = Exception("Conversion failed")

        coEvery { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) } returns Result.failure(error)

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) }
    }

    @Test
    fun `convertText should handle UTF8 encoding`() = runTest {
        // Given
        val rawText = "Привет, мир!"
        val targetEncoding = TextEncoding.UTF8
        val convertedText = rawText // UTF8 conversion returns same text

        val convertedEncodingView = ConvertedEncodingView(convertedText = convertedText)

        coEvery { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) } returns Result.success(convertedEncodingView)

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(convertedText, result.getOrThrow())

        coVerify(exactly = 1) { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) }
    }

    @Test
    fun `convertText should handle ASCII encoding`() = runTest {
        // Given
        val rawText = "Hello"
        val targetEncoding = TextEncoding.ASCII
        val convertedText = "Hello"

        val convertedEncodingView = ConvertedEncodingView(convertedText = convertedText)

        coEvery { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) } returns Result.success(convertedEncodingView)

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(convertedText, result.getOrThrow())

        coVerify(exactly = 1) { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) }
    }

    @Test
    fun `convertText should handle BASE64 encoding`() = runTest {
        // Given
        val rawText = "Test"
        val targetEncoding = TextEncoding.BASE64
        val convertedText = "VGVzdA=="

        val convertedEncodingView = ConvertedEncodingView(convertedText = convertedText)

        coEvery { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) } returns Result.success(convertedEncodingView)

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(convertedText, result.getOrThrow())

        coVerify(exactly = 1) { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) }
    }

    @Test
    fun `convertText should handle exception during conversion`() = runTest {
        // Given
        val rawText = "Test text"
        val targetEncoding = TextEncoding.BASE64
        val exception = RuntimeException("Unexpected error")

        coEvery { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) } throws exception

        // When
        val result = presenter.convertText(rawText, targetEncoding)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify(exactly = 1) { convertTextEncodingHandler(ConvertTextEncodingCommand(rawText, targetEncoding)) }
    }
}
