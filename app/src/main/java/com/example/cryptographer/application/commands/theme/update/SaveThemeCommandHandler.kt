package com.example.cryptographer.application.commands.theme.update

import com.example.cryptographer.application.common.ports.settings.SettingsCommandGateway
import com.example.cryptographer.application.errors.SettingsSaveError
import com.example.cryptographer.domain.common.errors.AppError
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for saving theme mode.
 *
 * This is a Command Handler in CQRS pattern - it handles write operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses gateway for persistence operations
 */
class SaveThemeCommandHandler(
    private val settingsCommandGateway: SettingsCommandGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the SaveThemeCommand.
     *
     * @param command Command to execute
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(command: SaveThemeCommand): Result<Unit> {
        return try {
            logger.debug { "Handling SaveThemeCommand: themeMode=${command.themeMode}" }
            val success = settingsCommandGateway.saveThemeMode(command.themeMode)
            if (success) {
                logger.info { "Theme mode saved successfully: ${command.themeMode}" }
                Result.success(Unit)
            } else {
                logger.warn { "Failed to save theme mode: ${command.themeMode}" }
                Result.failure(SettingsSaveError("theme"))
            }
        } catch (e: AppError) {
            logger.error(e) { "Error handling SaveThemeCommand: ${e.message}" }
            Result.failure(e)
        }
    }
}
