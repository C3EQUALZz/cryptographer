package com.example.cryptographer.application.common.ports.settings

/**
 * Gateway for settings-related query operations (read operations).
 *
 * This is a Gateway in CQRS pattern - it defines the interface
 * for read operations (queries) that retrieve application settings.
 *
 * Following Clean Architecture principles:
 * - Interface is in Application layer (application boundary)
 * - Implementation will be in Infrastructure layer
 */
interface SettingsQueryGateway {
    /**
     * Loads the saved theme mode.
     *
     * @return Theme mode value ("system", "light", or "dark")
     */
    suspend fun loadThemeMode(): String

    /**
     * Loads the saved language preference.
     *
     * @return Language code (e.g., "en", "ru")
     */
    suspend fun loadLanguage(): String
}

