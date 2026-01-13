package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

/**
 * Command for generating and saving a Triple DES encryption key.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a write operation (key generation and saving) using 3DES.
 */
data class TripleDesGenerateAndSaveKeyCommand(
    val algorithm: EncryptionAlgorithm,
)
