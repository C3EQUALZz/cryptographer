package com.example.cryptographer.presentation.chacha20

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.application.common.views.KeyView
import com.example.cryptographer.application.queries.key.readall.LoadAllKeysQuery
import com.example.cryptographer.application.queries.key.readall.LoadAllKeysQueryHandler
import com.example.cryptographer.application.queries.key.readbyid.LoadKeyQuery
import com.example.cryptographer.application.queries.key.readbyid.LoadKeyQueryHandler
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Base64
import javax.inject.Inject

/**
 * ViewModel for ChaCha20 encryption/decryption screen.
 */
@HiltViewModel
class ChaCha20ViewModel @Inject constructor(
    private val presenter: ChaCha20Presenter,
    private val loadKeyHandler: LoadKeyQueryHandler,
    private val loadAllKeysHandler: LoadAllKeysQueryHandler,
) : ViewModel() {
    private val logger = KotlinLogging.logger {}

    private val _uiState = MutableStateFlow(ChaCha20UiState())
    val uiState: StateFlow<ChaCha20UiState> = _uiState.asStateFlow()

    private val _availableKeys = MutableStateFlow<List<KeyView>>(emptyList())
    val availableKeys: StateFlow<List<KeyView>> = _availableKeys.asStateFlow()

    init {
        loadAvailableKeys()
    }

    /**
     * Updates the input text.
     */
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(
            inputText = text,
            error = null,
        )
    }

    /**
     * Updates the encrypted text input.
     */
    fun updateEncryptedText(text: String) {
        _uiState.value = _uiState.value.copy(
            encryptedTextInput = text,
            error = null,
        )
    }

    /**
     * Updates the nonce input.
     */
    fun updateNonceText(text: String) {
        _uiState.value = _uiState.value.copy(
            nonceInput = text,
            error = null,
        )
    }

    /**
     * Selects a key by ID.
     */
    fun selectKey(keyId: String) {
        logger.debug { "Selecting key: keyId=$keyId" }
        viewModelScope.launch {
            val query = LoadKeyQuery(keyId)
            loadKeyHandler(query)
                .onSuccess { keyView ->
                    // Verify key is ChaCha20
                    if (keyView.algorithm != EncryptionAlgorithm.CHACHA20_256) {
                        _uiState.value = _uiState.value.copy(
                            error = "Выбранный ключ не является ChaCha20 ключом",
                        )
                        return@launch
                    }

                    // Convert KeyView to EncryptionKey domain entity
                    val key = EncryptionKey(
                        value = Base64.getDecoder().decode(keyView.keyBase64),
                        algorithm = keyView.algorithm,
                    )
                    _uiState.value = _uiState.value.copy(
                        selectedKey = key,
                        selectedKeyId = keyId,
                        error = null,
                    )
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to load key: keyId=$keyId" }
                    _uiState.value = _uiState.value.copy(
                        error = "Не удалось загрузить ключ: ${error.message}",
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
                error = "Введите текст для шифрования",
            )
            return
        }

        if (selectedKey == null) {
            _uiState.value = _uiState.value.copy(
                error = "Выберите ключ для шифрования",
            )
            return
        }

        logger.debug { "Encrypting text: length=${inputText.length}, algorithm=${selectedKey.algorithm}" }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
            )

            presenter.encryptText(inputText, selectedKey)
                .onSuccess { encryptedInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        encryptedText = encryptedInfo.encryptedBase64,
                        nonceText = encryptedInfo.nonceBase64 ?: "",
                        error = null,
                    )
                }
                .onFailure { error ->
                    logger.error(error) { "Encryption failed: ${error.message}" }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка шифрования: ${error.message}",
                    )
                }
        }
    }

    /**
     * Decrypts the encrypted text.
     */
    fun decryptText() {
        val encryptedText = _uiState.value.encryptedTextInput
        val nonceText = _uiState.value.nonceInput
        val selectedKey = _uiState.value.selectedKey

        if (encryptedText.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Введите зашифрованный текст",
            )
            return
        }

        if (selectedKey == null) {
            _uiState.value = _uiState.value.copy(
                error = "Выберите ключ для дешифрования",
            )
            return
        }

        logger.debug { "Decrypting text: algorithm=${selectedKey.algorithm}" }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
            )

            presenter.decryptText(
                encryptedBase64 = encryptedText,
                nonceBase64 = nonceText.takeIf { it.isNotBlank() },
                key = selectedKey,
            )
                .onSuccess { decryptedText ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        decryptedText = decryptedText,
                        error = null,
                    )
                }
                .onFailure { error ->
                    logger.error(error) { "Decryption failed: ${error.message}" }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка дешифрования: ${error.message}",
                    )
                }
        }
    }

    /**
     * Clears all input and output fields.
     */
    fun clearAll() {
        _uiState.value = ChaCha20UiState(
            selectedKey = _uiState.value.selectedKey,
            selectedKeyId = _uiState.value.selectedKeyId,
        )
    }

    /**
     * Loads all available ChaCha20 keys.
     */
    private fun loadAvailableKeys() {
        viewModelScope.launch {
            val query = LoadAllKeysQuery
            loadAllKeysHandler(query)
                .onSuccess { keyViews ->
                    // Filter keys by ChaCha20 algorithm
                    _availableKeys.value = keyViews.filter { it.algorithm == EncryptionAlgorithm.CHACHA20_256 }
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to load available keys: ${error.message}" }
                }
        }
    }
}

