package com.example.cryptographer.presentation.key

import com.example.cryptographer.domain.text.entity.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.usecase.DeleteAllKeysUseCase
import com.example.cryptographer.domain.text.usecase.DeleteKeyUseCase
import com.example.cryptographer.domain.text.usecase.GenerateEncryptionKeyUseCase
import com.example.cryptographer.domain.text.usecase.KeyItem
import com.example.cryptographer.domain.text.usecase.LoadAllKeysUseCase
import com.example.cryptographer.domain.text.usecase.LoadKeyUseCase
import com.example.cryptographer.domain.text.usecase.SaveKeyUseCase
import com.example.cryptographer.setup.configs.getLogger
import java.util.Base64

/**
 * Presenter for key generation screen.
 * Coordinates use cases and transforms domain models to presentation models.
 * This layer separates business logic from ViewModel, making it easier to test.
 */
class KeyGenerationPresenter(
    private val generateEncryptionKeyUseCase: GenerateEncryptionKeyUseCase,
    private val saveKeyUseCase: SaveKeyUseCase,
    private val loadKeyUseCase: LoadKeyUseCase,
    private val deleteKeyUseCase: DeleteKeyUseCase,
    private val deleteAllKeysUseCase: DeleteAllKeysUseCase,
    private val loadAllKeysUseCase: LoadAllKeysUseCase
) {
    private val logger = getLogger<KeyGenerationPresenter>()

    /**
     * Generates and saves a new encryption key.
     *
     * @param algorithm Encryption algorithm to use
     * @return Result with generated key info (key, keyId, base64) or error
     */
    suspend fun generateAndSaveKey(algorithm: EncryptionAlgorithm): Result<GeneratedKeyInfo> {
        return try {
            logger.d("Generating and saving key: algorithm=$algorithm")
            
            // Generate key
            val keyResult = generateEncryptionKeyUseCase(algorithm)
            if (keyResult.isFailure) {
                return Result.failure(keyResult.exceptionOrNull() ?: Exception("Key generation failed"))
            }
            
            val key = keyResult.getOrThrow()
            
            // Save key
            val saveResult = saveKeyUseCase(key)
            if (saveResult.isFailure) {
                return Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save key"))
            }
            
            val keyId = saveResult.getOrThrow()
            val keyBase64 = Base64.getEncoder().encodeToString(key.value)
            
            logger.i("Key generated and saved successfully: keyId=$keyId, algorithm=$algorithm")
            Result.success(
                GeneratedKeyInfo(
                    key = key,
                    keyId = keyId,
                    keyBase64 = keyBase64
                )
            )
        } catch (e: Exception) {
            logger.e("Error generating and saving key: algorithm=$algorithm", e)
            Result.failure(e)
        }
    }

    /**
     * Loads a key by ID.
     *
     * @param keyId Key identifier
     * @return Result with key info or error
     */
    suspend fun loadKey(keyId: String): Result<GeneratedKeyInfo> {
        return try {
            logger.d("Loading key: keyId=$keyId")
            val keyResult = loadKeyUseCase(keyId)
            
            if (keyResult.isFailure) {
                return Result.failure(keyResult.exceptionOrNull() ?: Exception("Key not found"))
            }
            
            val key = keyResult.getOrThrow()
            val keyBase64 = Base64.getEncoder().encodeToString(key.value)
            
            Result.success(
                GeneratedKeyInfo(
                    key = key,
                    keyId = keyId,
                    keyBase64 = keyBase64
                )
            )
        } catch (e: Exception) {
            logger.e("Error loading key: keyId=$keyId", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a key by ID.
     *
     * @param keyId Key identifier
     * @return Result indicating success or failure
     */
    suspend fun deleteKey(keyId: String): Result<Unit> {
        return deleteKeyUseCase(keyId)
    }

    /**
     * Deletes all keys.
     *
     * @return Result indicating success or failure
     */
    suspend fun deleteAllKeys(): Result<Unit> {
        return deleteAllKeysUseCase()
    }

    /**
     * Loads all saved keys.
     *
     * @return Result with list of key items or error
     */
    suspend fun loadAllKeys(): Result<List<KeyItem>> {
        return loadAllKeysUseCase()
    }
}

/**
 * Presentation model for generated key information.
 */
data class GeneratedKeyInfo(
    val key: EncryptionKey,
    val keyId: String,
    val keyBase64: String
)

