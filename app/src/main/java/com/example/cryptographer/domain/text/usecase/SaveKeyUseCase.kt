package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
import com.example.cryptographer.setup.configs.getLogger
import java.util.UUID

/**
 * Use Case for saving an encryption key.
 * Generates a unique ID and saves the key to storage.
 */
class SaveKeyUseCase(
    private val keyStorageAdapter: KeyStorageAdapter
) {
    private val logger = getLogger<SaveKeyUseCase>()

    /**
     * Saves an encryption key with a generated unique ID.
     *
     * @param key Encryption key to save
     * @return Result with generated key ID or error
     */
    suspend operator fun invoke(key: EncryptionKey): Result<String> {
        return try {
            val keyId = UUID.randomUUID().toString()
            logger.d("Saving key: keyId=$keyId, algorithm=${key.algorithm}")
            
            val saved = keyStorageAdapter.saveKey(keyId, key)
            
            if (saved) {
                logger.i("Key saved successfully: keyId=$keyId")
                Result.success(keyId)
            } else {
                logger.e("Failed to save key: keyId=$keyId")
                Result.failure(Exception("Failed to save key"))
            }
        } catch (e: Exception) {
            logger.e("Error saving key", e)
            Result.failure(e)
        }
    }
}

