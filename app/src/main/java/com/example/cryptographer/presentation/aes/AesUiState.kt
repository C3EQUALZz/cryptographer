package com.example.cryptographer.presentation.aes

import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * UI state for AES encryption/decryption screen.
 */
data class AesUiState(
    val selectedKeyLength: EncryptionAlgorithm = EncryptionAlgorithm.AES_256,
    val inputText: String = "",
    val encryptedText: String = "",
    val encryptedTextInput: String = "",
    val ivText: String = "",
    val ivInput: String = "",
    val decryptedText: String = "",
    val selectedKey: EncryptionKey? = null,
    val selectedKeyId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

