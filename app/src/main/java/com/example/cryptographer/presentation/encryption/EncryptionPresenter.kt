package com.example.cryptographer.presentation.encryption

import com.example.cryptographer.domain.text.entity.EncryptedText
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.entity.TextEncoding
import com.example.cryptographer.domain.text.usecase.DecryptTextUseCase
import com.example.cryptographer.domain.text.usecase.EncryptTextUseCase
import com.example.cryptographer.setup.configs.getLogger
import java.util.Base64

/**
 * Presenter for encryption/decryption screen.
 * Coordinates use cases and transforms domain models to presentation models.
 */
class EncryptionPresenter(
    private val encryptTextUseCase: EncryptTextUseCase,
    private val decryptTextUseCase: DecryptTextUseCase
) {
    private val logger = getLogger<EncryptionPresenter>()

    /**
     * Encrypts text using the provided key.
     *
     * @param text Text to encrypt
     * @param key Encryption key
     * @return Result with encrypted text (Base64 encoded) or error
     */
    suspend fun encryptText(text: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        return try {
            logger.d("Encrypting text: length=${text.length}")
            
            val textEntity = Text(
                content = text,
                encoding = TextEncoding.UTF8
            )
            
            val result = encryptTextUseCase(textEntity, key)
            
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Encryption failed"))
            }
            
            val encryptedText = result.getOrThrow()
            val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedText.encryptedData)
            val ivBase64 = encryptedText.initializationVector?.let {
                Base64.getEncoder().encodeToString(it)
            }
            
            logger.i("Text encrypted successfully: algorithm=${key.algorithm}, size=${encryptedText.encryptedData.size} bytes")
            Result.success(
                EncryptedTextInfo(
                    encryptedBase64 = encryptedBase64,
                    ivBase64 = ivBase64,
                    algorithm = key.algorithm
                )
            )
        } catch (e: Exception) {
            logger.e("Error encrypting text", e)
            Result.failure(e)
        }
    }

    /**
     * Decrypts encrypted text using the provided key.
     *
     * @param encryptedBase64 Base64 encoded encrypted data
     * @param ivBase64 Base64 encoded initialization vector (optional)
     * @param key Encryption key
     * @return Result with decrypted text or error
     */
    suspend fun decryptText(
        encryptedBase64: String,
        ivBase64: String?,
        key: EncryptionKey
    ): Result<String> {
        return try {
            logger.d("Decrypting text: algorithm=${key.algorithm}")
            
            val encryptedData = Base64.getDecoder().decode(encryptedBase64)
            val iv = ivBase64?.let { Base64.getDecoder().decode(it) }
            
            val encryptedText = EncryptedText(
                encryptedData = encryptedData,
                algorithm = key.algorithm,
                initializationVector = iv
            )
            
            val result = decryptTextUseCase(encryptedText, key)
            
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Decryption failed"))
            }
            
            val decryptedText = result.getOrThrow()
            logger.i("Text decrypted successfully: length=${decryptedText.content.length}")
            Result.success(decryptedText.content)
        } catch (e: Exception) {
            logger.e("Error decrypting text", e)
            Result.failure(e)
        }
    }
}

/**
 * Presentation model for encrypted text information.
 */
data class EncryptedTextInfo(
    val encryptedBase64: String,
    val ivBase64: String?,
    val algorithm: com.example.cryptographer.domain.text.entity.EncryptionAlgorithm
)

