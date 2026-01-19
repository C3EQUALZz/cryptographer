package com.example.cryptographer.application.commands.file.encrypt

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for encrypting a file using AES algorithm.
 */
data class AesEncryptFileCommand(
    val inputPath: String,
    val outputPath: String,
    val key: EncryptionKey,
)
