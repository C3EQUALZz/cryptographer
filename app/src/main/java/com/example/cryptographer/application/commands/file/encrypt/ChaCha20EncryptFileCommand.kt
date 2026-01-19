package com.example.cryptographer.application.commands.file.encrypt

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for encrypting a file using ChaCha20-Poly1305 algorithm.
 */
data class ChaCha20EncryptFileCommand(
    val inputPath: String,
    val outputPath: String,
    val key: EncryptionKey,
)
