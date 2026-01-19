package com.example.cryptographer.application.commands.file.encrypt

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for encrypting a file using Triple DES algorithm.
 */
data class TripleDesEncryptFileCommand(
    val inputPath: String,
    val outputPath: String,
    val key: EncryptionKey,
)
