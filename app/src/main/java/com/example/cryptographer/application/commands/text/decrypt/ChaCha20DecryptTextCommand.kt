package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for decrypting text using ChaCha20 algorithm.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a read operation (decryption) using ChaCha20.
 */
data class ChaCha20DecryptTextCommand(
    val encryptedText: EncryptedText,
    val key: EncryptionKey
)


