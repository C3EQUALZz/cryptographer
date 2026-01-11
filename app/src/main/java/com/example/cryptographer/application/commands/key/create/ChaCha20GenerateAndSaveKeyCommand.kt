package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm

/**
 * Command for generating and saving a ChaCha20 encryption key.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a write operation (key generation and saving) using ChaCha20.
 */
data class ChaCha20GenerateAndSaveKeyCommand(
    val algorithm: EncryptionAlgorithm // CHACHA20_256
)


