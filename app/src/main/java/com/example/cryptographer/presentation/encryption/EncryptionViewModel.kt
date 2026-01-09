package com.example.cryptographer.presentation.encryption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.usecase.KeyItem
import com.example.cryptographer.domain.text.usecase.LoadAllKeysUseCase
import com.example.cryptographer.domain.text.usecase.LoadKeyUseCase
import com.example.cryptographer.setup.configs.getLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for encryption/decryption screen.
 * 
 * Responsibilities:
 * - Manages UI state (input text, encrypted text, selected key, etc.)
 * - Delegates business logic to Presenter
 * - Handles coroutine scoping for async operations
 */
@HiltViewModel
class EncryptionViewModel @Inject constructor(
    private val presenter: EncryptionPresenter,
    private val loadKeyUseCase: LoadKeyUseCase,
    private val loadAllKeysUseCase: LoadAllKeysUseCase
) : ViewModel() {
    private val logger = getLogger<EncryptionViewModel>()

    private val _uiState = MutableStateFlow(EncryptionUiState())
    val uiState: StateFlow<EncryptionUiState> = _uiState.asStateFlow()

    private val _availableKeys = MutableStateFlow<List<KeyItem>>(emptyList())
    val availableKeys: StateFlow<List<KeyItem>> = _availableKeys.asStateFlow()

    init {
        loadAvailableKeys()
    }

    /**
     * Updates the input text.
     */
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(
            inputText = text,
            error = null
        )
    }

    /**
     * Updates the encrypted text input.
     */
    fun updateEncryptedText(text: String) {
        _uiState.value = _uiState.value.copy(
            encryptedTextInput = text,
            error = null
        )
    }

    /**
     * Updates the IV input.
     */
    fun updateIvText(text: String) {
        _uiState.value = _uiState.value.copy(
            ivInput = text,
            error = null
        )
    }

    /**
     * Selects a key by ID.
     */
    fun selectKey(keyId: String) {
        logger.d("Selecting key: keyId=$keyId")
        viewModelScope.launch {
            loadKeyUseCase(keyId)
                .onSuccess { key ->
                    _uiState.value = _uiState.value.copy(
                        selectedKey = key,
                        selectedKeyId = keyId,
                        error = null
                    )
                }
                .onFailure { error ->
                    logger.e("Failed to load key: keyId=$keyId", error)
                    _uiState.value = _uiState.value.copy(
                        error = "Не удалось загрузить ключ: ${error.message}"
                    )
                }
        }
    }

    /**
     * Encrypts the input text.
     */
    fun encryptText() {
        val inputText = _uiState.value.inputText
        val selectedKey = _uiState.value.selectedKey

        if (inputText.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Введите текст для шифрования"
            )
            return
        }

        if (selectedKey == null) {
            _uiState.value = _uiState.value.copy(
                error = "Выберите ключ для шифрования"
            )
            return
        }

        logger.d("Encrypting text: length=${inputText.length}, algorithm=${selectedKey.algorithm}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            presenter.encryptText(inputText, selectedKey)
                .onSuccess { encryptedInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        encryptedText = encryptedInfo.encryptedBase64,
                        ivText = encryptedInfo.ivBase64 ?: "",
                        error = null
                    )
                }
                .onFailure { error ->
                    logger.e("Encryption failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка шифрования: ${error.message}"
                    )
                }
        }
    }

    /**
     * Decrypts the encrypted text.
     */
    fun decryptText() {
        val encryptedText = _uiState.value.encryptedTextInput
        val ivText = _uiState.value.ivInput
        val selectedKey = _uiState.value.selectedKey

        if (encryptedText.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Введите зашифрованный текст"
            )
            return
        }

        if (selectedKey == null) {
            _uiState.value = _uiState.value.copy(
                error = "Выберите ключ для дешифрования"
            )
            return
        }

        logger.d("Decrypting text: algorithm=${selectedKey.algorithm}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            presenter.decryptText(
                encryptedBase64 = encryptedText,
                ivBase64 = ivText.takeIf { it.isNotBlank() },
                key = selectedKey
            )
                .onSuccess { decryptedText ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        decryptedText = decryptedText,
                        error = null
                    )
                }
                .onFailure { error ->
                    logger.e("Decryption failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка дешифрования: ${error.message}"
                    )
                }
        }
    }

    /**
     * Clears all input and output fields.
     */
    fun clearAll() {
        _uiState.value = EncryptionUiState(
            selectedKey = _uiState.value.selectedKey,
            selectedKeyId = _uiState.value.selectedKeyId
        )
    }

    /**
     * Loads all available keys.
     */
    private fun loadAvailableKeys() {
        viewModelScope.launch {
            loadAllKeysUseCase()
                .onSuccess { keys ->
                    _availableKeys.value = keys
                }
                .onFailure { error ->
                    logger.e("Failed to load available keys: ${error.message}", error)
                }
        }
    }
}

/**
 * UI state for encryption/decryption screen.
 */
data class EncryptionUiState(
    val inputText: String = "",
    val encryptedText: String = "",
    val encryptedTextInput: String = "",
    val ivText: String = "",
    val ivInput: String = "",
    val decryptedText: String = "",
    val selectedKey: EncryptionKey? = null,
    val selectedKeyId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

