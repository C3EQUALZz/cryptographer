package com.example.cryptographer.infrastructure.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptographer.application.common.ports.settings.SettingsQueryGateway
import io.github.oshai.kotlinlogging.KotlinLogging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Adapter implementing SettingsQueryGateway using SharedPreferences.
 *
 * This adapter is in Infrastructure layer and implements the gateway interface
 * from Application layer. Following Clean Architecture principles:
 * - Implementation details (SharedPreferences) are hidden from Application layer
 * - Uses coroutines for async operations
 */
class SettingsQueryGatewayAdapter @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsQueryGateway {
    private val logger = KotlinLogging.logger {}

    private val themePrefs: SharedPreferences by lazy {
        context.getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val localePrefs: SharedPreferences by lazy {
        context.getSharedPreferences(LOCALE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val THEME_PREFS_NAME = "theme_prefs"
        private const val LOCALE_PREFS_NAME = "locale_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_LANGUAGE = "en"
    }

    override suspend fun loadThemeMode(): String {
        return withContext(Dispatchers.IO) {
            try {
                val themeMode = themePrefs.getString(KEY_THEME_MODE, DEFAULT_THEME) ?: DEFAULT_THEME
                logger.debug { "Loaded theme mode: $themeMode" }
                themeMode
            } catch (e: Exception) {
                logger.error(e) { "Error loading theme mode: ${e.message}" }
                DEFAULT_THEME
            }
        }
    }

    override suspend fun loadLanguage(): String {
        return withContext(Dispatchers.IO) {
            try {
                val languageCode = localePrefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
                logger.debug { "Loaded language: $languageCode" }
                languageCode
            } catch (e: Exception) {
                logger.error(e) { "Error loading language: ${e.message}" }
                DEFAULT_LANGUAGE
            }
        }
    }
}
