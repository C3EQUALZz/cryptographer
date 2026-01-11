package com.example.cryptographer.test.stubs

import com.example.cryptographer.application.common.ports.settings.SettingsCommandGateway
import kotlinx.coroutines.delay

/**
 * Stub implementation of SettingsCommandGateway for testing.
 * Stores settings in memory for testing purposes.
 */
class StubSettingsCommandGateway : SettingsCommandGateway {
    private var savedThemeMode: String? = null
    private var savedLanguage: String? = null
    private var shouldFailSaveTheme = false
    private var shouldFailSaveLanguage = false

    override suspend fun saveThemeMode(themeMode: String): Boolean {
        delay(10) // Simulate async operation
        if (shouldFailSaveTheme) {
            return false
        }
        savedThemeMode = themeMode
        return true
    }

    override suspend fun saveLanguage(languageCode: String): Boolean {
        delay(10) // Simulate async operation
        if (shouldFailSaveLanguage) {
            return false
        }
        savedLanguage = languageCode
        return true
    }

    /**
     * Gets the saved theme mode.
     */
    fun getSavedThemeMode(): String? = savedThemeMode

    /**
     * Gets the saved language.
     */
    fun getSavedLanguage(): String? = savedLanguage

    /**
     * Sets whether saveThemeMode should fail.
     */
    fun setShouldFailSaveTheme(shouldFail: Boolean) {
        shouldFailSaveTheme = shouldFail
    }

    /**
     * Sets whether saveLanguage should fail.
     */
    fun setShouldFailSaveLanguage(shouldFail: Boolean) {
        shouldFailSaveLanguage = shouldFail
    }

    /**
     * Clears all saved settings.
     */
    fun clear() {
        savedThemeMode = null
        savedLanguage = null
    }
}

