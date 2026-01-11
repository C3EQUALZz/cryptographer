package com.example.cryptographer.presentation.chacha20

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * UI state for ChaCha20 encryption/decryption screen.
 */
data class ChaCha20UiState(
    val inputText: String = "",
    val encryptedText: String = "",
    val encryptedTextInput: String = "",
    val nonceText: String = "",
    val nonceInput: String = "",
    val decryptedText: String = "",
    val selectedKey: EncryptionKey? = null,
    val selectedKeyId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

