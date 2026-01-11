package com.example.cryptographer.application.errors

/**
 * Error thrown when a settings save operation fails.
 *
 * This error is thrown by command handlers when attempting to save settings
 * but the save operation fails (e.g., due to storage issues).
 *
 * @param settingType The type of setting that failed to save (e.g., "theme", "language")
 */
class SettingsSaveError(
    val settingType: String,
    cause: Throwable? = null,
) : ApplicationError("Failed to save setting: type=$settingType", cause)

