package com.example.cryptographer.application.errors

/**
 * Error thrown when a settings load operation fails.
 *
 * This error is thrown by query handlers when attempting to load settings
 * but the load operation fails (e.g., due to storage issues).
 *
 * @param settingType The type of setting that failed to load (e.g., "theme", "language")
 */
class SettingsLoadError(
    val settingType: String,
    cause: Throwable? = null,
) : ApplicationError("Failed to load setting: type=$settingType", cause)
