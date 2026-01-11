package com.example.cryptographer.domain.text.valueobjects

/**
 * Represents different text encodings.
 *
 * This is a Value Object in Domain-Driven Design - it represents
 * a concept from the domain (text encoding) without identity.
 */
enum class TextEncoding {
    UTF8,
    ASCII,
    BASE64,
}
