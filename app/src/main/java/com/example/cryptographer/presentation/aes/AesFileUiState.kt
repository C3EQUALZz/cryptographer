package com.example.cryptographer.presentation.aes

import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * UI state for AES file encryption/decryption screen.
 */
data class AesFileUiState(
    val selectedKeyLength: EncryptionAlgorithm = EncryptionAlgorithm.AES_256,
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
