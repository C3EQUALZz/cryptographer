package com.example.cryptographer.presentation.encoding

import com.example.cryptographer.application.commands.text.convert_encoding.ConvertTextEncodingCommand
import com.example.cryptographer.application.commands.text.convert_encoding.ConvertTextEncodingCommandHandler
import com.example.cryptographer.domain.text.value_objects.TextEncoding
import com.example.cryptographer.setup.configs.getLogger

/**
 * Presenter for text encoding conversion screen.
 * Coordinates command handlers and transforms between presentation DTOs and Views.
 *
 * Following CQRS pattern - uses CommandHandler from Application layer.
 */
class EncodingPresenter(
    private val convertTextEncodingHandler: ConvertTextEncodingCommandHandler
) {
    private val logger = getLogger<EncodingPresenter>()

    /**
     * Converts text to the specified encoding.
     *
     * @param rawText Raw text string (DTO from presentation layer)
     * @param targetEncoding Target encoding
     * @return Result with converted text string (DTO for presentation layer) or error
     */
    suspend fun convertText(
        rawText: String,
        targetEncoding: TextEncoding
    ): Result<String> {
        return try {
            logger.d("Presenter: Converting text: length=${rawText.length}, targetEncoding=$targetEncoding")

            if (rawText.isBlank()) {
                return Result.success("")
            }

            // Execute command via CommandHandler
            val command = ConvertTextEncodingCommand(rawText, targetEncoding)
            val convertedEncodingViewResult = convertTextEncodingHandler(command)

            if (convertedEncodingViewResult.isFailure) {
                val error = convertedEncodingViewResult.exceptionOrNull() ?: Exception("Conversion failed")
                logger.e("Presenter: Conversion failed: ${error.message}", error)
                return Result.failure(error)
            }

            val convertedEncodingView = convertedEncodingViewResult.getOrThrow()
            val converted = convertedEncodingView.convertedText

            logger.i("Presenter: Text converted successfully: targetEncoding=$targetEncoding, convertedLength=${converted.length}")
            Result.success(converted)
        } catch (e: Exception) {
            logger.e("Presenter: Error converting text: ${e.message}", e)
            Result.failure(e)
        }
    }
}
