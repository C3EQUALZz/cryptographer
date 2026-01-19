package com.example.cryptographer.application.commands.file.decrypt

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for decrypting a file using AES algorithm.
 */
data class AesDecryptFileCommand(
    val inputPath: String,
    val outputPath: String,
    val key: EncryptionKey,
)
