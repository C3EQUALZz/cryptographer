package com.example.cryptographer.presentation.key

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.domain.text.entity.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.usecase.GenerateEncryptionKeyUseCase
import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            generateEncryptionKeyUseCase(algorithm)
                .onSuccess { key ->
                    val keyId = UUID.randomUUID().toString()
                    val saved = keyStorageAdapter.saveKey(keyId, key)
                    
                    if (saved) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generatedKey = key,
                            keyId = keyId,
                            keyBase64 = Base64.getEncoder().encodeToString(key.value)
                        )
                        loadSavedKeys()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to save key"
                        )
                    }
                }
                .onFailure { error ->
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
        viewModelScope.launch {
            val key = keyStorageAdapter.getKey(keyId)
            if (key != null) {
                _uiState.value = _uiState.value.copy(
                    generatedKey = key,
                    keyId = keyId,
                    keyBase64 = Base64.getEncoder().encodeToString(key.value),
                    error = null
                )
            } else {
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
        viewModelScope.launch {
            if (keyStorageAdapter.deleteKey(keyId)) {
                loadSavedKeys()
                if (_uiState.value.keyId == keyId) {
                    _uiState.value = _uiState.value.copy(
                        generatedKey = null,
                        keyId = null,
                        keyBase64 = null
                    )
                }
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
            val keyIds = keyStorageAdapter.getAllKeyIds()
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

