package com.example.cryptographer.application.queries.key.read_by_id

/**
 * Query for loading a single encryption key.
 *
 * This is a Query DTO in CQRS pattern - it represents
 * an intent to read data without modifying state.
 */
data class LoadKeyQuery(
    val keyId: String
)
