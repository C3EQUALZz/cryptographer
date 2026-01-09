package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
import com.example.cryptographer.setup.configs.getLogger

/**
 * Use Case for loading an encryption key by ID.
 */
class LoadKeyUseCase(
    private val keyStorageAdapter: KeyStorageAdapter
) {
    private val logger = getLogger<LoadKeyUseCase>()

    /**
     * Loads an encryption key by ID.
     *
     * @param keyId Key identifier
     * @return Result with encryption key or error
     */
    operator fun invoke(keyId: String): Result<EncryptionKey> {
        return try {
            logger.d("Loading key: keyId=$keyId")
            val key = keyStorageAdapter.getKey(keyId)
            
            if (key != null) {
                logger.d("Key loaded successfully: keyId=$keyId, algorithm=${key.algorithm}")
                Result.success(key)
            } else {
                logger.w("Key not found: keyId=$keyId")
                Result.failure(Exception("Key not found"))
            }
        } catch (e: Exception) {
            logger.e("Error loading key: keyId=$keyId", e)
            Result.failure(e)
        }
    }
}

