package com.example.cryptographer.presentation.key

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.R
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entities.EncryptionKey
import io.github.oshai.kotlinlogging.KotlinLogging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for key generation and management screen.
 *
 * Responsibilities:
 * - Manages UI state (loading, errors, displayed data)
 * - Delegates business logic to Presenter
 * - Handles coroutine scoping for async operations
 */
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    private val presenter: KeyGenerationPresenter,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val logger = KotlinLogging.logger {}

    private val _uiState = MutableStateFlow(KeyGenerationUiState())
    val uiState: StateFlow<KeyGenerationUiState> = _uiState.asStateFlow()

    private val _savedKeys = MutableStateFlow<List<SavedKeyItem>>(emptyList())
    val savedKeys: StateFlow<List<SavedKeyItem>> = _savedKeys.asStateFlow()

    init {
        loadSavedKeys()
    }

    /**
     * Generates a new encryption key for the selected algorithm.
     */
    fun generateKey(algorithm: EncryptionAlgorithm) {
        logger.debug { "Key generation requested: algorithm=$algorithm" }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            presenter.generateAndSaveKey(algorithm)
                .onSuccess { keyInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedKey = keyInfo.key,
                        keyId = keyInfo.keyId,
                        keyBase64 = keyInfo.keyBase64
                    )
                    loadSavedKeys()
                }
                .onFailure { error ->
                    logger.error(error) { "Key generation failed: algorithm=$algorithm, error=${error.message}" }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: context.getString(R.string.failed_to_generate_key)
                    )
                }
        }
    }

    /**
     * Loads a saved key by ID.
     */
    fun loadKey(keyId: String) {
        logger.debug { "Loading key: keyId=$keyId" }
        viewModelScope.launch {
            presenter.loadKey(keyId)
                .onSuccess { keyInfo ->
                    _uiState.value = _uiState.value.copy(
                        generatedKey = keyInfo.key,
                        keyId = keyInfo.keyId,
                        keyBase64 = keyInfo.keyBase64,
                        error = null
                    )
                }
                .onFailure { error ->
                    logger.warn { "Key not found: keyId=$keyId" }
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: context.getString(R.string.key_not_found)
                    )
                }
        }
    }

    /**
     * Deletes a saved key.
     */
    fun deleteKey(keyId: String) {
        logger.debug { "Deleting key: keyId=$keyId" }
        viewModelScope.launch {
            presenter.deleteKey(keyId)
                .onSuccess {
                    loadSavedKeys()
                    if (_uiState.value.keyId == keyId) {
                        _uiState.value = _uiState.value.copy(
                            generatedKey = null,
                            keyId = null,
                            keyBase64 = null
                        )
                    }
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to delete key: keyId=$keyId, error=${error.message}" }
                }
        }
    }

    /**
     * Deletes all saved keys.
     */
    fun deleteAllKeys() {
        logger.debug { "Deleting all keys requested" }
        viewModelScope.launch {
            presenter.deleteAllKeys()
                .onSuccess {
                    loadSavedKeys()
                    _uiState.value = _uiState.value.copy(
                        generatedKey = null,
                        keyId = null,
                        keyBase64 = null,
                        error = null
                    )
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to delete all keys: ${error.message}" }
                    _uiState.value = _uiState.value.copy(
                        error = context.getString(R.string.failed_to_delete_all_keys, error.message ?: "")
                    )
                }
        }
    }

    /**
     * Loads all saved keys.
     */
    private fun loadSavedKeys() {
        viewModelScope.launch {
            presenter.loadAllKeys()
                .onSuccess { keys ->
                    _savedKeys.value = keys.map { keyItem ->
                        SavedKeyItem(
                            id = keyItem.id,
                            algorithm = keyItem.algorithm,
                            keyBase64 = keyItem.keyBase64
                        )
                    }
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to load saved keys: ${error.message}" }
                }
        }
    }
}

/**
 * UI state for key generation screen.
 */
data class KeyGenerationUiState(
    val isLoading: Boolean = false,
    val generatedKey: EncryptionKey? = null,
    val keyId: String? = null,
    val keyBase64: String? = null,
    val error: String? = null
)

/**
 * Represents a saved key item for display.
 */
data class SavedKeyItem(
    val id: String,
    val algorithm: EncryptionAlgorithm,
    val keyBase64: String
)

