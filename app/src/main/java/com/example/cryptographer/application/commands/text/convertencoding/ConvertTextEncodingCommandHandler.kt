package com.example.cryptographer.application.commands.text.convertencoding

import com.example.cryptographer.application.common.views.ConvertedEncodingView
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.services.TextService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for converting text encoding.
 *
 * This is a Command Handler in CQRS pattern - it handles transformation operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class ConvertTextEncodingCommandHandler(
    private val textService: TextService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the ConvertTextEncodingCommand.
     *
     * @param command Command to execute
     * @return Result with ConvertedEncodingView or error
     */
    operator fun invoke(command: ConvertTextEncodingCommand): Result<ConvertedEncodingView> {
        return try {
            logger.debug {
                "Handling ConvertTextEncodingCommand:" +
                    " length=${command.rawText.length}, " +
                    "targetEncoding=${command.targetEncoding}"
            }

            val converted = textService.convertEncoding(
                rawText = command.rawText,
                targetEncoding = command.targetEncoding,
            ).getOrElse { error ->
                logger.error(error) { "Text encoding conversion failed: ${error.message}" }
                return Result.failure(error)
            }

            Result.success(ConvertedEncodingView(converted))
        } catch (e: AppError) {
            logger.error(e) { "Error handling ConvertTextEncodingCommand: ${e.message}" }
            Result.failure(e)
        }
    }
}
