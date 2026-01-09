package com.example.cryptographer.presentation.key

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.domain.text.entity.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.usecase.GenerateEncryptionKeyUseCase
import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
import com.example.cryptographer.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Base64
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for key generation and management screen.
 */
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    private val generateEncryptionKeyUseCase: GenerateEncryptionKeyUseCase,
    private val keyStorageAdapter: KeyStorageAdapter
) : ViewModel() {

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
        Logger.d("Key generation requested: algorithm=$algorithm")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            generateEncryptionKeyUseCase(algorithm)
                .onSuccess { key ->
                    val keyId = UUID.randomUUID().toString()
                    Logger.d("Key generated successfully, saving: keyId=$keyId, algorithm=$algorithm")
                    val saved = keyStorageAdapter.saveKey(keyId, key)
                    
                    if (saved) {
                        Logger.i("Key saved successfully: keyId=$keyId, algorithm=$algorithm")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generatedKey = key,
                            keyId = keyId,
                            keyBase64 = Base64.getEncoder().encodeToString(key.value)
                        )
                        loadSavedKeys()
                    } else {
                        Logger.e("Failed to save generated key: keyId=$keyId, algorithm=$algorithm")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to save key"
                        )
                    }
                }
                .onFailure { error ->
                    Logger.e("Key generation failed: algorithm=$algorithm, error=${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to generate key"
                    )
                }
        }
    }

    /**
     * Loads a saved key by ID.
     */
    fun loadKey(keyId: String) {
        Logger.d("Loading key: keyId=$keyId")
        viewModelScope.launch {
            val key = keyStorageAdapter.getKey(keyId)
            if (key != null) {
                Logger.d("Key loaded successfully: keyId=$keyId, algorithm=${key.algorithm}")
                _uiState.value = _uiState.value.copy(
                    generatedKey = key,
                    keyId = keyId,
                    keyBase64 = Base64.getEncoder().encodeToString(key.value),
                    error = null
                )
            } else {
                Logger.w("Key not found: keyId=$keyId")
                _uiState.value = _uiState.value.copy(
                    error = "Key not found"
                )
            }
        }
    }

    /**
     * Deletes a saved key.
     */
    fun deleteKey(keyId: String) {
        Logger.d("Deleting key: keyId=$keyId")
        viewModelScope.launch {
            if (keyStorageAdapter.deleteKey(keyId)) {
                Logger.i("Key deleted successfully: keyId=$keyId")
                loadSavedKeys()
                if (_uiState.value.keyId == keyId) {
                    _uiState.value = _uiState.value.copy(
                        generatedKey = null,
                        keyId = null,
                        keyBase64 = null
                    )
                }
            } else {
                Logger.e("Failed to delete key: keyId=$keyId")
            }
        }
    }

    /**
     * Clears the current generated key from UI.
     */
    fun clearGeneratedKey() {
        _uiState.value = _uiState.value.copy(
            generatedKey = null,
            keyId = null,
            keyBase64 = null,
            error = null
        )
    }

    /**
     * Loads all saved keys.
     */
    private fun loadSavedKeys() {
        viewModelScope.launch {
            Logger.d("Loading all saved keys")
            val keyIds = keyStorageAdapter.getAllKeyIds()
            Logger.d("Found ${keyIds.size} saved key(s)")
            val keys = keyIds.mapNotNull { keyId ->
                keyStorageAdapter.getKey(keyId)?.let { key ->
                    SavedKeyItem(
                        id = keyId,
                        algorithm = key.algorithm,
                        keyBase64 = Base64.getEncoder().encodeToString(key.value)
                    )
                }
            }
            _savedKeys.value = keys
            Logger.d("Loaded ${keys.size} key(s) successfully")
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

