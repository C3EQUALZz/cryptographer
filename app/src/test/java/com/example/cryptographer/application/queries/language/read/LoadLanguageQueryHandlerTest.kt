package com.example.cryptographer.application.queries.language.read

import com.example.cryptographer.test.stubs.StubSettingsQueryGateway
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoadLanguageQueryHandler.
 */
class LoadLanguageQueryHandlerTest {

    private lateinit var settingsGateway: StubSettingsQueryGateway
    private lateinit var handler: LoadLanguageQueryHandler

    @Before
    fun setUp() {
        settingsGateway = StubSettingsQueryGateway()
        handler = LoadLanguageQueryHandler(settingsGateway)
    }

    @Test
    fun `invoke should load language successfully`() = runTest {
        // Given
        val languageCode = "ru"
        settingsGateway.setLanguage(languageCode)
        val query = LoadLanguageQuery

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(languageCode, result.getOrThrow().languageCode)
    }
}
