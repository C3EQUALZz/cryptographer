package com.example.cryptographer.domain.text.entity

/**
 * Entity representing text data in the domain.
 * This is a pure business object without framework dependencies.
 */
data class Text(
    val content: String,
    val encoding: TextEncoding = TextEncoding.UTF8
) {
    val length: Int
        get() = content.length

    val isEmpty: Boolean
        get() = content.isEmpty()

    val isNotEmpty: Boolean
        get() = content.isNotEmpty()
}

/**
 * Represents different text encodings.
 */
enum class TextEncoding {
    UTF8,
    ASCII,
    BASE64
}

