package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.repository.TextRepository

/**
 * Use Case for getting all saved texts.
 */
class GetAllTextsUseCase(
    private val textRepository: TextRepository
) {
    suspend operator fun invoke(): Result<List<Text>> {
        return textRepository.getAllTexts()
    }
}

