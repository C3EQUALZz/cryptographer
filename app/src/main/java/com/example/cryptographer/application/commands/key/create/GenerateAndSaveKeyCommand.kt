package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm

/**
 * Command for generating and saving an encryption key.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a write operation.
 */
data class GenerateAndSaveKeyCommand(
    val algorithm: EncryptionAlgorithm
)
