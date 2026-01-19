package com.example.cryptographer.presentation.main

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.setup.i18n.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for main screen (navigation and settings).
 *
 * Responsibilities:
 * - Manages UI state (selected screen, algorithm, language, theme)
 * - Delegates business logic to Presenter
 * - Handles coroutine scoping for async operations
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val presenter: MainPresenter,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {
    private val logger = KotlinLogging.logger {}

    private companion object {
        const val THEME_PREFS_NAME = "theme_prefs"
        const val KEY_THEME_MODE = "theme_mode"
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Loads saved settings (theme and language) on initialization.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // Load theme
            presenter.loadThemeMode()
                .onSuccess { themeMode ->
                    _uiState.value = _uiState.value.copy(
                        themeMode = themeMode,
                    )
                    saveThemeModePref(themeMode)
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to load theme mode: ${error.message}" }
                }

            // Load language
            presenter.loadLanguage()
                .onSuccess { languageCode ->
                    _uiState.value = _uiState.value.copy(
                        languageCode = languageCode,
                    )
                    LocaleHelper.saveLanguage(context, languageCode)
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to load language: ${error.message}" }
                }
        }
    }

    /**
     * Updates the selected screen.
     */
    fun selectScreen(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(selectedScreen = screen)
    }

    /**
     * Updates the selected encryption algorithm.
     */
    fun selectAlgorithm(algorithm: EncryptionAlgorithm) {
        _uiState.value = _uiState.value.copy(selectedAlgorithm = algorithm)
    }

    /**
     * Updates the theme mode and saves it.
     */
    fun updateThemeMode(themeMode: String) {
        _uiState.value = _uiState.value.copy(themeMode = themeMode)
        saveThemeModePref(themeMode)
        viewModelScope.launch {
            presenter.saveThemeMode(themeMode)
                .onFailure { error ->
                    logger.error(error) { "Failed to save theme mode: ${error.message}" }
                    // Revert on failure - reload settings
                    presenter.loadThemeMode()
                        .onSuccess { themeMode ->
                            _uiState.value = _uiState.value.copy(themeMode = themeMode)
                            saveThemeModePref(themeMode)
                        }
                }
        }
    }

    /**
     * Updates the language preference and saves it.
     */
    fun updateLanguage(languageCode: String) {
        _uiState.value = _uiState.value.copy(languageCode = languageCode)
        LocaleHelper.saveLanguage(context, languageCode)
        viewModelScope.launch {
            presenter.saveLanguage(languageCode)
                .onFailure { error ->
                    logger.error(error) { "Failed to save language: ${error.message}" }
                    // Revert on failure - reload settings
                    presenter.loadLanguage()
                        .onSuccess { languageCode ->
                            _uiState.value = _uiState.value.copy(languageCode = languageCode)
                            LocaleHelper.saveLanguage(context, languageCode)
                        }
                }
        }
    }

    private fun saveThemeModePref(themeMode: String) {
        val prefs = context.getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME_MODE, themeMode) }
    }
}

/**
 * UI state for main screen.
 */
data class MainUiState(
    val selectedScreen: AppScreen = AppScreen.KeyGeneration,
    val selectedAlgorithm: EncryptionAlgorithm = EncryptionAlgorithm.AES_256,
    val themeMode: String = "system",
    val languageCode: String = "en",
)

/**
 * App screens enumeration.
 */
enum class AppScreen {
    KeyGeneration,
    AesEncryption,
    AesFile,
    ChaCha20Encryption,
    ChaCha20File,
    TripleDesEncryption,
    TripleDesFile,
    Encoding,
}
