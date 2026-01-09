package com.example.cryptographer.domain.text.repository

import com.example.cryptographer.domain.text.entity.Text

/**
 * Repository interface for text operations.
 * Defines the contract for text data operations.
 * Implementation will be in the data layer.
 */
interface TextRepository {
    /**
     * Saves text.
     */
    suspend fun saveText(text: Text): Result<Unit>

    /**
     * Gets text by identifier.
     */
    suspend fun getText(id: String): Result<Text>

    /**
     * Deletes text.
     */
    suspend fun deleteText(id: String): Result<Unit>

    /**
     * Gets all saved texts.
     */
    suspend fun getAllTexts(): Result<List<Text>>

    /**
     * Validates text against requirements.
     */
    suspend fun validateText(text: Text): Result<Boolean>
}

