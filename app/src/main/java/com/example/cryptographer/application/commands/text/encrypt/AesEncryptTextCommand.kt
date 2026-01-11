package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for encrypting text using AES algorithm.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a write operation (encryption) using AES.
 */
data class AesEncryptTextCommand(
    val rawText: String,
    val key: EncryptionKey,
)
