package com.example.cryptographer.application.queries.key.read_by_id

import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.application.common.views.KeyView
import com.example.cryptographer.setup.configs.getLogger
import java.util.Base64

/**
 * Query Handler for loading a single encryption key.
 *
 * This is a Query Handler in CQRS pattern - it handles read operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses Gateway for infrastructure operations
 * - Returns View (DTO) for presentation layer
 */
class LoadKeyQueryHandler(
    private val queryGateway: KeyQueryGateway
) {
    private val logger = getLogger<LoadKeyQueryHandler>()

    /**
     * Handles the LoadKeyQuery.
     *
     * @param query Query to execute
     * @return Result with KeyView or error
     */
    operator fun invoke(query: LoadKeyQuery): Result<KeyView> {
        return try {
            logger.d("Handling LoadKeyQuery: keyId=${query.keyId}")
            val key = queryGateway.getKey(query.keyId)

            if (key != null) {
                logger.d("Key loaded successfully: keyId=${query.keyId}, algorithm=${key.algorithm}")
                Result.success(
                    KeyView(
                        id = query.keyId,
                        algorithm = key.algorithm,
                        keyBase64 = Base64.getEncoder().encodeToString(key.value)
                    )
                )
            } else {
                logger.w("Key not found: keyId=${query.keyId}")
                Result.failure(Exception("Key not found"))
            }
        } catch (e: Exception) {
            logger.e("Error handling LoadKeyQuery: keyId=${query.keyId}", e)
            Result.failure(e)
        }
    }
}
