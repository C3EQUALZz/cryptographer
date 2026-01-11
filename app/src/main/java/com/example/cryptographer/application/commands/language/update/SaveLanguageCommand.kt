package com.example.cryptographer.application.commands.language.update

/**
 * Command to save language preference.
 *
 * Following CQRS pattern:
 * - Command represents an intent to change state
 * - Contains only the data needed to execute the command
 */
data class SaveLanguageCommand(
    val languageCode: String,
)
