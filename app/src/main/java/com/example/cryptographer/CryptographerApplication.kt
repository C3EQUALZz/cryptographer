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
        LocaleHelper.onAttach(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}

