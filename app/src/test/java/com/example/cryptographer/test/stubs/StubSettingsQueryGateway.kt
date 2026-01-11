package com.example.cryptographer.test.stubs

import com.example.cryptographer.application.common.ports.settings.SettingsQueryGateway
import kotlinx.coroutines.delay

/**
 * Stub implementation of SettingsQueryGateway for testing.
 * Provides settings from memory for testing purposes.
 */
class StubSettingsQueryGateway : SettingsQueryGateway {
    private var themeMode: String = "system"
    private var language: String = "en"
    private var shouldThrowOnLoadTheme = false
    private var shouldThrowOnLoadLanguage = false

    override suspend fun loadThemeMode(): String {
        delay(10) // Simulate async operation
        if (shouldThrowOnLoadTheme) {
            throw RuntimeException("Failed to load theme")
        }
        return themeMode
    }

    override suspend fun loadLanguage(): String {
        delay(10) // Simulate async operation
        if (shouldThrowOnLoadLanguage) {
            throw RuntimeException("Failed to load language")
        }
        return language
    }

    /**
     * Sets the theme mode to return.
     */
    fun setThemeMode(themeMode: String) {
        this.themeMode = themeMode
    }

    /**
     * Sets the language to return.
     */
    fun setLanguage(language: String) {
        this.language = language
    }
}
