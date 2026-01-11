package com.example.cryptographer.infrastructure.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptographer.application.common.ports.settings.SettingsCommandGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Adapter implementing SettingsCommandGateway using SharedPreferences.
 *
 * This adapter is in Infrastructure layer and implements the gateway interface
 * from Application layer. Following Clean Architecture principles:
 * - Implementation details (SharedPreferences) are hidden from Application layer
 * - Uses coroutines for async operations
 */
class SettingsCommandGatewayAdapter @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettingsCommandGateway {
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
    }

    override suspend fun saveThemeMode(themeMode: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                logger.debug { "Saving theme mode: $themeMode" }
                themePrefs.edit().putString(KEY_THEME_MODE, themeMode).commit()
            } catch (e: Exception) {
                logger.error(e) { "Error saving theme mode: ${e.message}" }
                false
            }
        }
    }

    override suspend fun saveLanguage(languageCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                logger.debug { "Saving language: $languageCode" }
                localePrefs.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).commit()
            } catch (e: Exception) {
                logger.error(e) { "Error saving language: ${e.message}" }
                false
            }
        }
    }
}
