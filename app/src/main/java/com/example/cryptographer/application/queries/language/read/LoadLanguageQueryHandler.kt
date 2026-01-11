package com.example.cryptographer.application.queries.language.read

import com.example.cryptographer.application.common.ports.settings.SettingsQueryGateway
import com.example.cryptographer.application.common.views.LanguageView
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Query Handler for loading language preference.
 *
 * This is a Query Handler in CQRS pattern - it handles read operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses gateway for persistence operations
 * - Returns View (DTO) for presentation layer
 */
class LoadLanguageQueryHandler(
    private val settingsQueryGateway: SettingsQueryGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the LoadLanguageQuery.
     *
     * @param query Query to execute
     * @return Result with LanguageView or error
     */
    suspend operator fun invoke(query: LoadLanguageQuery): Result<LanguageView> {
        return try {
            logger.debug { "Handling LoadLanguageQuery" }
            val languageCode = settingsQueryGateway.loadLanguage()
            logger.debug { "Language loaded: $languageCode" }
            Result.success(LanguageView(languageCode = languageCode))
        } catch (e: Exception) {
            logger.error(e) { "Error handling LoadLanguageQuery: ${e.message}" }
            Result.failure(e)
        }
    }
}
