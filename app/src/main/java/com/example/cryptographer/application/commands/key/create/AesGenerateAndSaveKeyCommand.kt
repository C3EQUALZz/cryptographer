package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm

/**
 * Command for generating and saving an AES encryption key.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a write operation (key generation and saving) using AES.
 */
data class AesGenerateAndSaveKeyCommand(
    val algorithm: EncryptionAlgorithm // AES_128, AES_192, or AES_256
)


