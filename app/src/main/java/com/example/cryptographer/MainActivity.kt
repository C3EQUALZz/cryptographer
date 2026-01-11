package com.example.cryptographer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cryptographer.domain.common.valueobjects.ThemeMode
import com.example.cryptographer.presentation.common.CryptographerTheme
import com.example.cryptographer.presentation.main.MainScreen
import com.example.cryptographer.setup.i18n.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        // For attachBaseContext, we need synchronous access to settings
        // This is a necessary compromise for Android's attachBaseContext lifecycle
        val languageCode = getSavedLanguageSync(newBase)
        val updatedContext = LocaleHelper.onAttach(newBase, languageCode)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load theme synchronously for initial setup (necessary for onCreate)
        val savedThemeMode = getSavedThemeModeSync()

        setContent {
            val isSystemDark = isSystemInDarkTheme()
            var currentTheme by remember { mutableStateOf(savedThemeMode) }

            val darkTheme = remember(currentTheme, isSystemDark) {
                when (currentTheme) {
                    ThemeMode.SYSTEM -> isSystemDark
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
            }

            CryptographerTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // MainScreen now uses ViewModel for state management
                    MainScreen()
                }
            }
        }
    }

    /**
     * Synchronous helper to load theme mode for onCreate.
     * This is a necessary compromise for Activity lifecycle.
     * Actual async operations should use Gateway through ViewModel.
     */
    private fun getSavedThemeModeSync(): ThemeMode {
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val themeValue = prefs.getString("theme_mode", "system") ?: "system"
        return ThemeMode.fromValue(themeValue)
    }

    /**
     * Synchronous helper to load language for attachBaseContext.
     * This is a necessary compromise for Android's attachBaseContext lifecycle.
     * Actual async operations should use Gateway through ViewModel.
     */
    private fun getSavedLanguageSync(context: Context): String {
        val prefs = context.getSharedPreferences("locale_prefs", MODE_PRIVATE)
        return prefs.getString("selected_language", "en") ?: "en"
    }
}
