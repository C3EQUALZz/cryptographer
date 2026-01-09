package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
import com.example.cryptographer.setup.configs.getLogger
import java.util.Base64

/**
 * Use Case for loading all saved encryption keys.
 * Converts domain entities to presentation models.
 */
class LoadAllKeysUseCase(
    private val keyStorageAdapter: KeyStorageAdapter
) {
    private val logger = getLogger<LoadAllKeysUseCase>()

    /**
     * Loads all saved encryption keys.
     *
     * @return Result with list of key items (id, algorithm, base64) or error
     */
    operator fun invoke(): Result<List<KeyItem>> {
        return try {
            logger.d("Loading all saved keys")
            val keyIds = keyStorageAdapter.getAllKeyIds()
            logger.d("Found ${keyIds.size} saved key(s)")
            
            val keys = keyIds.mapNotNull { keyId ->
                keyStorageAdapter.getKey(keyId)?.let { key ->
                    KeyItem(
                        id = keyId,
                        algorithm = key.algorithm,
                        keyBase64 = Base64.getEncoder().encodeToString(key.value)
                    )
                }
            }
            
            logger.d("Loaded ${keys.size} key(s) successfully")
            Result.success(keys)
        } catch (e: Exception) {
            logger.e("Error loading all keys", e)
            Result.failure(e)
        }
    }
}

/**
 * Presentation model for a saved key item.
 */
data class KeyItem(
    val id: String,
    val algorithm: com.example.cryptographer.domain.text.entity.EncryptionAlgorithm,
    val keyBase64: String
)

