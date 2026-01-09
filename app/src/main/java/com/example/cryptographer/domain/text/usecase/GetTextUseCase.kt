package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.repository.TextRepository

/**
 * Use Case for getting text by ID.
 */
class GetTextUseCase(
    private val textRepository: TextRepository
) {
    suspend operator fun invoke(id: String): Result<Text> {
        return textRepository.getText(id)
    }
}

