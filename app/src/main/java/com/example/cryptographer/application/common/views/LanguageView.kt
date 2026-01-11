package com.example.cryptographer.application.common.views

/**
 * View representing language preference setting.
 *
 * This is a View in CQRS pattern - it represents
 * the result of a language query.
 */
data class LanguageView(
    val languageCode: String,
)

