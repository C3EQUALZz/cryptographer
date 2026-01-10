package com.example.cryptographer.application.queries.language.read

/**
 * Query to load language preference.
 *
 * Following CQRS pattern:
 * - Query represents a read operation
 * - Has no parameters as it loads the current saved language
 */
object LoadLanguageQuery
