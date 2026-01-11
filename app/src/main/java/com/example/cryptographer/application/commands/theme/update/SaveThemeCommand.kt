package com.example.cryptographer.application.commands.theme.update

/**
 * Command to save theme mode.
 *
 * Following CQRS pattern:
 * - Command represents an intent to change state
 * - Contains only the data needed to execute the command
 */
data class SaveThemeCommand(
    val themeMode: String,
)
