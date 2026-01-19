package com.example.cryptographer.presentation.tdes

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * UI state for Triple DES file encryption/decryption screen.
 */
data class TripleDesFileUiState(
    val selectedKeyId: String? = null,
    val selectedKey: EncryptionKey? = null,
    val encryptInputPath: String = "",
    val encryptOutputPath: String = "",
    val encryptResultPath: String = "",
    val ivBase64: String = "",
    val decryptInputPath: String = "",
    val decryptOutputPath: String = "",
    val decryptResultPath: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
