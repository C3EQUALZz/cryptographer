package com.example.cryptographer.setup.i18n

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.example.cryptographer.domain.common.valueobjects.Language
import java.util.Locale

/**
 * Helper class for managing application locale context.
 * This is a utility class for Android-specific locale operations.
 *
 * Note: This class handles only Context operations (attachBaseContext).
 * Actual saving/loading of language preference is handled by SettingsGateway
 * through MainPresenter/MainViewModel (following Clean Architecture).
 */
object LocaleHelper {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"
    private const val DEFAULT_LANGUAGE = "en"

    /**
     * Gets the saved language from SharedPreferences (synchronous).
     * This is used only for attachBaseContext where async operations are not possible.
     * For all other cases, use SettingsGateway through MainViewModel.
     */
    fun getSavedLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        return Language.fromCode(languageCode)
    }

    /**
     * Updates the context with the saved locale.
     * Should be called in Application.onCreate() and Activity.attachBaseContext().
     *
     * Note: For attachBaseContext, languageCode should be passed directly
     * to avoid async operations during Activity lifecycle.
     */
    fun onAttach(context: Context, languageCode: String? = null): Context {
        val code = languageCode ?: getSavedLanguage(context).code
        return updateContextLocale(context, code)
    }

    /**
     * Updates the context's locale configuration by language code.
     */
    private fun updateContextLocale(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}

