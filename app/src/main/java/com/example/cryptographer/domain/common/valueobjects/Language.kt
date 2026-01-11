package com.example.cryptographer.domain.common.valueobjects

/**
 * Language enumeration for application localization.
 *
 * This is a Value Object following DDD principles.
 * Represents supported languages in the domain.
 */
enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    ;

    companion object {
        fun fromCode(code: String): Language {
            return Language.entries.find { it.code == code } ?: ENGLISH
        }
    }
}
