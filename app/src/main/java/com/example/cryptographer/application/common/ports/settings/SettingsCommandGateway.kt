package com.example.cryptographer.application.common.ports.settings

/**
 * Gateway for settings-related command operations (write operations).
 *
 * This is a Gateway in CQRS pattern - it defines the interface
 * for write operations (commands) that modify application settings.
 *
 * Following Clean Architecture principles:
 * - Interface is in Application layer (application boundary)
 * - Implementation will be in Infrastructure layer
 */
interface SettingsCommandGateway {
    /**
     * Saves the theme mode.
     *
     * @param themeMode Theme mode to save (SYSTEM, LIGHT, DARK)
     * @return true if theme was saved successfully, false otherwise
     */
    suspend fun saveThemeMode(themeMode: String): Boolean

    /**
     * Saves the language preference.
     *
     * @param languageCode Language code to save (e.g., "en", "ru")
     * @return true if language was saved successfully, false otherwise
     */
    suspend fun saveLanguage(languageCode: String): Boolean
}
