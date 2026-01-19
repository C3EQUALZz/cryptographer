package com.example.cryptographer.presentation.chacha20

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * UI state for ChaCha20 file encryption/decryption screen.
 */
data class ChaCha20FileUiState(
    val selectedKeyId: String? = null,
    val selectedKey: EncryptionKey? = null,
    val encryptInputPath: String = "",
    val encryptOutputPath: String = "",
    val encryptResultPath: String = "",
    val nonceBase64: String = "",
    val decryptInputPath: String = "",
    val decryptOutputPath: String = "",
    val decryptResultPath: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
