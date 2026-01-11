package com.example.cryptographer.presentation.encoding

import com.example.cryptographer.application.commands.text.convertencoding.ConvertTextEncodingCommand
import com.example.cryptographer.application.commands.text.convertencoding.ConvertTextEncodingCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Presenter for text encoding conversion screen.
 * Coordinates command handlers and transforms between presentation DTOs and Views.
 *
 * Following CQRS pattern - uses CommandHandler from Application layer.
 */
class EncodingPresenter(
    private val convertTextEncodingHandler: ConvertTextEncodingCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Converts text to the specified encoding.
     *
     * @param rawText Raw text string (DTO from presentation layer)
     * @param targetEncoding Target encoding
     * @return Result with converted text string (DTO for presentation layer) or error
     */
    suspend fun convertText(rawText: String, targetEncoding: TextEncoding): Result<String> {
        return try {
            logger.debug {
                "Presenter: Converting text: " +
                    "length=${rawText.length}, " +
                    "targetEncoding=$targetEncoding"
            }

            if (rawText.isBlank()) {
                Result.success("")
            } else {
                convertNonEmptyText(rawText, targetEncoding)
            }
        } catch (e: AppError) {
            logger.error(e) { "Presenter: Error converting text: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun convertNonEmptyText(rawText: String, targetEncoding: TextEncoding): Result<String> {
        // Execute command via CommandHandler
        val command = ConvertTextEncodingCommand(rawText, targetEncoding)
        val convertedEncodingViewResult = convertTextEncodingHandler(command)

        return convertedEncodingViewResult.fold(
            onSuccess = { convertedEncodingView ->
                val converted = convertedEncodingView.convertedText
                logger.info {
                    "Presenter: Text converted successfully: " +
                        "targetEncoding=$targetEncoding, " +
                        "convertedLength=${converted.length}"
                }
                Result.success(converted)
            },
            onFailure = { error ->
                logger.error(error) { "Presenter: Conversion failed: ${error.message}" }
                Result.failure(error)
            },
        )
    }
}
