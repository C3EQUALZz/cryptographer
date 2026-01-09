package com.example.cryptographer.setup.i18n

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.core.content.edit
import java.util.Locale

/**
 * Helper class for managing application locale.
 * Follows Android best practices for i18n.
 */
object LocaleHelper {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"
    private const val DEFAULT_LANGUAGE = "en"

    /**
     * Supported languages in the application.
     */
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        RUSSIAN("ru", "Русский");

        companion object {
            fun fromCode(code: String): Language {
                return Language.entries.find { it.code == code } ?: ENGLISH
            }
        }
    }

    /**
     * Gets the saved language from SharedPreferences.
     */
    fun getSavedLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        return Language.fromCode(languageCode)
    }

    /**
     * Saves the selected language to SharedPreferences.
     */
    fun saveLanguage(context: Context, language: Language) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_SELECTED_LANGUAGE, language.code)
        }
    }

    /**
     * Sets the locale for the given context.
     * Returns a new context with the updated locale.
     */
    fun setLocale(context: Context, language: Language): Context {
        saveLanguage(context, language)
        return updateContextLocale(context, language)
    }

    /**
     * Updates the context with the saved locale.
     * Should be called in Application.onCreate() and Activity.attachBaseContext().
     */
    fun onAttach(context: Context): Context {
        val language = getSavedLanguage(context)
        return updateContextLocale(context, language)
    }

    /**
     * Updates the context's locale configuration.
     */
    private fun updateContextLocale(context: Context, language: Language): Context {
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}

