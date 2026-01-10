package com.example.cryptographer.application.queries.theme.read

/**
 * Query to load theme mode.
 *
 * Following CQRS pattern:
 * - Query represents a read operation
 * - Has no parameters as it loads the current saved theme
 */
object LoadThemeQuery
