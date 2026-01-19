package com.example.cryptographer.application.commands.file.decrypt

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for decrypting a file using ChaCha20-Poly1305 algorithm.
 */
data class ChaCha20DecryptFileCommand(
    val inputPath: String,
    val outputPath: String,
    val key: EncryptionKey,
)
