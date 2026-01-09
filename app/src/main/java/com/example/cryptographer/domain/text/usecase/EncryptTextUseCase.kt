package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.EncryptedText
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.service.AesEncryptionService
import com.example.cryptographer.setup.configs.getLogger

/**
 * Use Case for encrypting text using AES algorithm.
 * Encapsulates the logic of preparing text for encryption and performing encryption.
 */
class EncryptTextUseCase(
    private val aesEncryptionService: AesEncryptionService,
    private val prepareTextUseCase: PrepareTextForEncryptionUseCase
) {
    private val logger = getLogger<EncryptTextUseCase>()
    /**
     * Encrypts text using the provided AES key.
     *
     * @param text Text to encrypt
     * @param key AES encryption key
     * @return Result with encrypted text or error
     */
    operator fun invoke(
        text: Text,
        key: EncryptionKey
    ): Result<EncryptedText> {
        return try {
            logger.d("Encrypting text: length=${text.content.length}, encoding=${text.encoding}, algorithm=${key.algorithm}")
            // Prepare text for encryption (normalization, validation)
            val preparedText = prepareTextUseCase(text).getOrElse { error ->
                logger.e("Text preparation failed: ${error.message}", error)
                return Result.failure(error)
            }

            // Convert text to bytes
            val textBytes = preparedText.content.toByteArray(Charsets.UTF_8)

            // Encrypt using AES service
            val result = aesEncryptionService.encrypt(textBytes, key)
            if (result.isSuccess) {
                logger.i("Text encryption successful: algorithm=${key.algorithm}")
            }
            result
        } catch (e: Exception) {
            logger.e("Text encryption error: ${e.message}", e)
            Result.failure(
                Exception("Text encryption error: ${e.message}", e)
            )
        }
    }
}

