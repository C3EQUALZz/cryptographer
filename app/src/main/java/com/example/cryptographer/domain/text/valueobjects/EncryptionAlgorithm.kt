package com.example.cryptographer.domain.text.valueobjects

/**
 * Enumeration of supported encryption algorithms.
 */
enum class EncryptionAlgorithm {
    AES_128,
    AES_192,
    AES_256,
    CHACHA20_256,
    TDES_112, // Triple DES with 112-bit effective key (two keys)
    TDES_168, // Triple DES with 168-bit effective key (three keys)
}
