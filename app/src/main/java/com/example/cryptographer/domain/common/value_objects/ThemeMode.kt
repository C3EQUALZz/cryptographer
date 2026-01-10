package com.example.cryptographer.domain.common.value_objects

/**
 * Theme mode enumeration for application theming.
 *
 * This is a Value Object following DDD principles.
 * Represents available theme modes in the domain.
 */
enum class ThemeMode(val value: String) {
    SYSTEM("system"), // Use system default
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromValue(value: String): ThemeMode {
            return ThemeMode.entries.find { it.value == value } ?: SYSTEM
        }
    }
}
