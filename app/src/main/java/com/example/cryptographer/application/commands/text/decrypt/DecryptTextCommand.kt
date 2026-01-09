package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for decrypting text.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a read operation (decryption).
 * Note: Decryption is technically a read operation, but we treat it as a command
 * because it requires input and produces output.
 */
data class DecryptTextCommand(
    val encryptedText: EncryptedText,
    val key: EncryptionKey
)
