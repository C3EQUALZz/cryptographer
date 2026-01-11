package com.example.cryptographer.application.errors

/**
 * Error thrown when a command fails validation.
 *
 * This error is thrown by command handlers when a command doesn't meet
 * the required validation rules.
 *
 * @param commandName The name of the command that failed validation
 * @param reason The reason why validation failed
 */
class CommandValidationError(
    val commandName: String,
    val reason: String,
) : ApplicationError("Command validation failed: command=$commandName, reason=$reason")

