package com.example.cryptographer.application.commands.key.delete

/**
 * Command for deleting a single encryption key.
 *
 * This is a Command DTO in CQRS pattern - it represents
 * an intent to perform a write operation.
 */
data class DeleteKeyCommand(
    val keyId: String
)
