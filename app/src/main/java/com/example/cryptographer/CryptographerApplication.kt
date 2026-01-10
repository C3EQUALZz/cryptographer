package com.example.cryptographer

import android.app.Application
import android.content.Context
import com.example.cryptographer.setup.i18n.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Cryptographer app.
 * Hilt uses this class to generate the dependency injection container.
 */
@HiltAndroidApp
class CryptographerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply saved locale on app start
        // Note: For Application.onCreate, we can use synchronous access
        val languageCode = getSavedLanguageSync(this)
        LocaleHelper.onAttach(this, languageCode)
    }

    override fun attachBaseContext(base: Context) {
        // For attachBaseContext, we need synchronous access to settings
        val languageCode = getSavedLanguageSync(base)
        val updatedContext = LocaleHelper.onAttach(base, languageCode)
        super.attachBaseContext(updatedContext)
    }

    /**
     * Synchronous helper to load language for attachBaseContext/onCreate.
     * This is a necessary compromise for Android lifecycle.
     * Actual async operations should use SettingsGateway through ViewModel.
     */
    private fun getSavedLanguageSync(context: Context): String {
        val prefs = context.getSharedPreferences("locale_prefs", MODE_PRIVATE)
        return prefs.getString("selected_language", "en") ?: "en"
    }
}

