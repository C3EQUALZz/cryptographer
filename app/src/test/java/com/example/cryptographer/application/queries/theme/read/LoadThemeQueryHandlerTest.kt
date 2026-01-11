package com.example.cryptographer.application.queries.theme.read

import com.example.cryptographer.test.stubs.StubSettingsQueryGateway
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoadThemeQueryHandler.
 */
class LoadThemeQueryHandlerTest {

    private lateinit var settingsGateway: StubSettingsQueryGateway
    private lateinit var handler: LoadThemeQueryHandler

    @Before
    fun setUp() {
        settingsGateway = StubSettingsQueryGateway()
        handler = LoadThemeQueryHandler(settingsGateway)
    }

    @Test
    fun `invoke should load theme mode successfully`() = runTest {
        // Given
        val themeMode = "dark"
        settingsGateway.setThemeMode(themeMode)
        val query = LoadThemeQuery

        // When
        val result = handler(query)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(themeMode, result.getOrThrow().themeMode)
    }
}
