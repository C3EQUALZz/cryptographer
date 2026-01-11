package com.example.cryptographer.application.common.views

/**
 * View representing theme mode setting.
 *
 * This is a View in CQRS pattern - it represents
 * the result of a theme query.
 */
data class ThemeView(
    val themeMode: String // "system", "light", or "dark"
)

