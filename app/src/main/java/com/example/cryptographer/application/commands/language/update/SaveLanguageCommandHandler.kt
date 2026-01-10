package com.example.cryptographer.application.commands.language.update

import com.example.cryptographer.application.common.ports.settings.SettingsCommandGateway
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for saving language preference.
 *
 * This is a Command Handler in CQRS pattern - it handles write operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses gateway for persistence operations
 */
class SaveLanguageCommandHandler(
    private val settingsCommandGateway: SettingsCommandGateway
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the SaveLanguageCommand.
     *
     * @param command Command to execute
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(command: SaveLanguageCommand): Result<Unit> {
        return try {
            logger.debug { "Handling SaveLanguageCommand: languageCode=${command.languageCode}" }
            val success = settingsCommandGateway.saveLanguage(command.languageCode)
            if (success) {
                logger.info { "Language saved successfully: ${command.languageCode}" }
                Result.success(Unit)
            } else {
                logger.warn { "Failed to save language: ${command.languageCode}" }
                Result.failure(Exception("Failed to save language"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling SaveLanguageCommand: ${e.message}" }
            Result.failure(e)
        }
    }
}
