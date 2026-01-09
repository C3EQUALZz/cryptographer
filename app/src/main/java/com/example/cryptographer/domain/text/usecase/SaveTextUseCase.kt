package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.repository.TextRepository

/**
 * Use Case for saving text.
 * Encapsulates business logic for saving text with validation.
 */
class SaveTextUseCase(
    private val textRepository: TextRepository,
    private val validateTextUseCase: ValidateTextUseCase
) {
    suspend operator fun invoke(text: Text): Result<Unit> {
        return try {
            // Validate before saving
            validateTextUseCase(text).getOrThrow()

            // Save through repository
            textRepository.saveText(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

