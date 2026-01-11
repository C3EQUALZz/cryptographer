package com.example.cryptographer.infrastructure.errors

/**
 * Error thrown when infrastructure configuration is invalid.
 *
 * This error is thrown when there are issues with infrastructure configuration,
 * such as missing required settings or invalid configuration values.
 *
 * @param component The component with invalid configuration
 * @param reason The reason why configuration is invalid
 */
class ConfigurationError(
    val component: String,
    val reason: String,
    cause: Throwable? = null,
) : InfrastructureError(
    message = "Configuration error: component=$component, reason=$reason",
    cause = cause,
)
