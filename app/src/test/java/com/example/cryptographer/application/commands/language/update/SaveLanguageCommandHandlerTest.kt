package com.example.cryptographer.application.commands.language.update

import com.example.cryptographer.application.errors.SettingsSaveError
import com.example.cryptographer.test.stubs.StubSettingsCommandGateway
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SaveLanguageCommandHandler.
 */
class SaveLanguageCommandHandlerTest {

    private lateinit var settingsGateway: StubSettingsCommandGateway
    private lateinit var handler: SaveLanguageCommandHandler

    @Before
    fun setUp() {
        settingsGateway = StubSettingsCommandGateway()
        handler = SaveLanguageCommandHandler(settingsGateway)
    }

    @Test
    fun `invoke should save language successfully`() = runTest {
        // Given
        val languageCode = "ru"
        val command = SaveLanguageCommand(languageCode)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(languageCode, settingsGateway.getSavedLanguage())
    }

    @Test
    fun `invoke should fail with SettingsSaveError when save fails`() = runTest {
        // Given
        settingsGateway.setShouldFailSaveLanguage(true)
        val languageCode = "en"
        val command = SaveLanguageCommand(languageCode)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SettingsSaveError)
        assertEquals("language", (error as SettingsSaveError).settingType)
    }
}

