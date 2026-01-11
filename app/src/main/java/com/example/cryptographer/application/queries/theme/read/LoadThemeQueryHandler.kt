package com.example.cryptographer.application.queries.theme.read

import com.example.cryptographer.application.common.ports.settings.SettingsQueryGateway
import com.example.cryptographer.application.common.views.ThemeView
import com.example.cryptographer.domain.common.errors.AppError
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Query Handler for loading theme mode.
 *
 * This is a Query Handler in CQRS pattern - it handles read operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses gateway for persistence operations
 * - Returns View (DTO) for presentation layer
 */
class LoadThemeQueryHandler(
    private val settingsQueryGateway: SettingsQueryGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the LoadThemeQuery.
     *
     * @param query Query to execute
     * @return Result with ThemeView or error
     */
    suspend operator fun invoke(query: LoadThemeQuery): Result<ThemeView> {
        return try {
            logger.debug { "Handling LoadThemeQuery" }
            val themeMode = settingsQueryGateway.loadThemeMode()
            logger.debug { "Theme mode loaded: $themeMode" }
            Result.success(ThemeView(themeMode = themeMode))
        } catch (e: AppError) {
            logger.error(e) { "Error handling LoadThemeQuery: ${e.message}" }
            Result.failure(e)
        }
    }
}
