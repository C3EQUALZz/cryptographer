package com.example.cryptographer.presentation.tdes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.R
import com.example.cryptographer.application.common.views.KeyView
import com.example.cryptographer.application.queries.key.readall.LoadAllKeysQuery
import com.example.cryptographer.application.queries.key.readall.LoadAllKeysQueryHandler
import com.example.cryptographer.application.queries.key.readbyid.LoadKeyQuery
import com.example.cryptographer.application.queries.key.readbyid.LoadKeyQueryHandler
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Base64
import javax.inject.Inject

/**
 * ViewModel for Triple DES encryption/decryption screen.
 */
@HiltViewModel
class TripleDesViewModel @Inject constructor(
    private val presenter: TripleDesPresenter,
    private val loadKeyHandler: LoadKeyQueryHandler,
    private val loadAllKeysHandler: LoadAllKeysQueryHandler,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {
    private val logger = KotlinLogging.logger {}

    private val _uiState = MutableStateFlow(TripleDesUiState())
    val uiState: StateFlow<TripleDesUiState> = _uiState.asStateFlow()

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
     * Updates the IV input.
     */
    fun updateIvText(text: String) {
        _uiState.value = _uiState.value.copy(
            ivInput = text,
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
                    // Verify key is 3DES
                    if (
                        keyView.algorithm != EncryptionAlgorithm.TDES_112 &&
                        keyView.algorithm != EncryptionAlgorithm.TDES_168
                    ) {
                        _uiState.value = _uiState.value.copy(
                            error = context.getString(R.string.error_invalid_algorithm_tdes, keyView.algorithm.name),
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
                        error = context.getString(R.string.error_failed_to_load_key, error.message ?: ""),
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
                error = context.getString(R.string.error_enter_text_to_encrypt),
            )
            return
        }

        if (selectedKey == null) {
            _uiState.value = _uiState.value.copy(
                error = context.getString(R.string.error_select_key_to_encrypt),
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
                        ivText = encryptedInfo.nonceBase64 ?: "",
                        error = null,
                    )
                }
                .onFailure { error ->
                    logger.error(error) { "Encryption failed: ${error.message}" }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = context.getString(R.string.error_encryption_failed, error.message ?: ""),
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
                error = context.getString(R.string.error_enter_encrypted_text),
            )
            return
        }

        if (selectedKey == null) {
            _uiState.value = _uiState.value.copy(
                error = context.getString(R.string.error_select_key_to_decrypt),
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
                nonceBase64 = ivText.takeIf { it.isNotBlank() },
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
                    val errorMessage = when {
                        error.message?.contains("Invalid Base64 format") == true -> {
                            context.getString(R.string.error_invalid_base64_format)
                        }

                        else -> {
                            context.getString(R.string.error_decryption_failed, error.message ?: "")
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage,
                    )
                }
        }
    }

    /**
     * Clears all input and output fields.
     */
    fun clearAll() {
        _uiState.value = TripleDesUiState(
            selectedKey = _uiState.value.selectedKey,
            selectedKeyId = _uiState.value.selectedKeyId,
        )
    }

    /**
     * Loads all available 3DES keys.
     * Can be called externally to refresh the key list.
     */
    fun loadAvailableKeys() {
        viewModelScope.launch {
            val query = LoadAllKeysQuery
            loadAllKeysHandler(query)
                .onSuccess { keyViews ->
                    // Filter keys by 3DES algorithms
                    _availableKeys.value = keyViews.filter {
                        it.algorithm == EncryptionAlgorithm.TDES_112 || it.algorithm == EncryptionAlgorithm.TDES_168
                    }
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to load available keys: ${error.message}" }
                }
        }
    }
}

/**
 * UI state for Triple DES encryption/decryption screen.
 */
data class TripleDesUiState(
    val inputText: String = "",
    val encryptedText: String = "",
    val encryptedTextInput: String = "",
    val decryptedText: String = "",
    val ivText: String = "",
    val ivInput: String = "",
    val selectedKey: EncryptionKey? = null,
    val selectedKeyId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
