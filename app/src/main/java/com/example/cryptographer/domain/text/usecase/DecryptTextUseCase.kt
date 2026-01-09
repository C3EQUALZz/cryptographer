package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.EncryptedText
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.entity.TextEncoding
import com.example.cryptographer.domain.text.service.AesEncryptionService

/**
 * Use Case for decrypting text using AES algorithm.
 * Encapsulates the logic of decryption and conversion back to text.
 */
class DecryptTextUseCase(
    private val aesEncryptionService: AesEncryptionService
) {
    /**
     * Decrypts encrypted text using the provided AES key.
     *
     * @param encryptedText Encrypted text
     * @param key AES encryption key
     * @return Result with decrypted text or error
     */
    operator fun invoke(
        encryptedText: EncryptedText,
        key: EncryptionKey
    ): Result<Text> {
        return try {
            // Decrypt using AES service
            val decryptedBytes = aesEncryptionService.decrypt(encryptedText, key).getOrElse { error ->
                return Result.failure(error)
            }

            // Convert bytes back to text
            val decryptedContent = String(decryptedBytes, Charsets.UTF_8)

            Result.success(
                Text(
                    content = decryptedContent,
                    encoding = TextEncoding.UTF8
                )
            )
        } catch (e: Exception) {
            Result.failure(
                Exception("Text decryption error: ${e.message}", e)
            )
        }
    }
}

