package com.example.cryptographer.application.commands.theme.update

import com.example.cryptographer.application.errors.SettingsSaveError
import com.example.cryptographer.test.stubs.StubSettingsCommandGateway
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SaveThemeCommandHandler.
 */
class SaveThemeCommandHandlerTest {

    private lateinit var settingsGateway: StubSettingsCommandGateway
    private lateinit var handler: SaveThemeCommandHandler

    @Before
    fun setUp() {
        settingsGateway = StubSettingsCommandGateway()
        handler = SaveThemeCommandHandler(settingsGateway)
    }

    @Test
    fun `invoke should save theme mode successfully`() = runTest {
        // Given
        val themeMode = "dark"
        val command = SaveThemeCommand(themeMode)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(themeMode, settingsGateway.getSavedThemeMode())
    }

    @Test
    fun `invoke should fail with SettingsSaveError when save fails`() = runTest {
        // Given
        settingsGateway.setShouldFailSaveTheme(true)
        val themeMode = "light"
        val command = SaveThemeCommand(themeMode)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SettingsSaveError)
        assertEquals("theme", (error as SettingsSaveError).settingType)
    }
}
