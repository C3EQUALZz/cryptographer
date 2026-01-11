package com.example.cryptographer.application.queries.key.readall

import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.application.common.views.KeyView
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64

/**
 * Query Handler for loading all encryption keys.
 *
 * This is a Query Handler in CQRS pattern - it handles read operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses Gateway for infrastructure operations
 * - Returns View (DTO) for presentation layer
 */
class LoadAllKeysQueryHandler(
    private val queryGateway: KeyQueryGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the LoadAllKeysQuery.
     *
     * @param query Query to execute
     * @return Result with list of KeyView or error
     */
    operator fun invoke(query: LoadAllKeysQuery): Result<List<KeyView>> {
        return try {
            logger.debug { "Handling LoadAllKeysQuery" }
            val keyIds = queryGateway.getAllKeyIds()
            logger.debug { "Found ${keyIds.size} saved key(s)" }

            val keys = keyIds.mapNotNull { keyId ->
                queryGateway.getKey(keyId)?.let { key ->
                    KeyView(
                        id = keyId,
                        algorithm = key.algorithm,
                        keyBase64 = Base64.getEncoder().encodeToString(key.value),
                    )
                }
            }

            logger.debug { "Loaded ${keys.size} key(s) successfully" }
            Result.success(keys)
        } catch (e: Exception) {
            logger.error(e) { "Error handling LoadAllKeysQuery" }
            Result.failure(e)
        }
    }
}
