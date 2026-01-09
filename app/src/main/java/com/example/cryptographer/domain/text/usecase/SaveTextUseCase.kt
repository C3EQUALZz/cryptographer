package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.repository.TextRepository
import com.example.cryptographer.setup.configs.getLogger

/**
 * Use Case for saving text.
 * Encapsulates business logic for saving text with validation.
 */
class SaveTextUseCase(
    private val textRepository: TextRepository,
    private val validateTextUseCase: ValidateTextUseCase
) {
    private val logger = getLogger<SaveTextUseCase>()
    suspend operator fun invoke(text: Text): Result<Unit> {
        return try {
            logger.d("Saving text: length=${text.content.length}, encoding=${text.encoding}")
            // Validate before saving
            validateTextUseCase(text).getOrThrow()

            // Save through repository
            val result = textRepository.saveText(text)
            if (result.isSuccess) {
                logger.i("Text saved successfully: length=${text.content.length}, encoding=${text.encoding}")
            }
            result
        } catch (e: Exception) {
            logger.e("Failed to save text: length=${text.content.length}, encoding=${text.encoding}, error=${e.message}", e)
            Result.failure(e)
        }
    }
}

