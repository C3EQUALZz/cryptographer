package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.repository.TextRepository

/**
 * Use Case for deleting text.
 */
class DeleteTextUseCase(
    private val textRepository: TextRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return textRepository.deleteText(id)
    }
}

