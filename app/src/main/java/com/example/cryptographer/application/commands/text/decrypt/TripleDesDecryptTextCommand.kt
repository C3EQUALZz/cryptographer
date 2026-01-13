package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey

/**
 * Command for decrypting text using Triple DES algorithm.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a write operation (decryption) using 3DES.
 */
data class TripleDesDecryptTextCommand(
    val encryptedText: EncryptedText,
    val key: EncryptionKey,
)
