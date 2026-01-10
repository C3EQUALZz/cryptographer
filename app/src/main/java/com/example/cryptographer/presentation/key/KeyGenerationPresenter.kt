package com.example.cryptographer.presentation.key

import com.example.cryptographer.application.commands.key.create.GenerateAndSaveKeyCommand
import com.example.cryptographer.application.commands.key.create.GenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommand
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommandHandler
import com.example.cryptographer.application.commands.key.delete_all.DeleteAllKeysCommand
import com.example.cryptographer.application.commands.key.delete_all.DeleteAllKeysCommandHandler
import com.example.cryptographer.application.queries.key.read_all.LoadAllKeysQuery
import com.example.cryptographer.application.queries.key.read_all.LoadAllKeysQueryHandler
import com.example.cryptographer.application.queries.key.read_by_id.LoadKeyQuery
import com.example.cryptographer.application.queries.key.read_by_id.LoadKeyQueryHandler
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entities.EncryptionKey
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64

/**
 * Presenter for key generation screen.
 * Coordinates command/query handlers and transforms Views to presentation models.
 *
 * This layer separates business logic from ViewModel, making it easier to test.
 * Following CQRS pattern - uses CommandHandlers and QueryHandlers from Application layer.
 */
class KeyGenerationPresenter(
    private val generateAndSaveKeyHandler: GenerateAndSaveKeyCommandHandler,
    private val loadKeyHandler: LoadKeyQueryHandler,
    private val deleteKeyHandler: DeleteKeyCommandHandler,
    private val deleteAllKeysHandler: DeleteAllKeysCommandHandler,
    private val loadAllKeysHandler: LoadAllKeysQueryHandler
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Generates and saves a new encryption key.
     *
     * @param algorithm Encryption algorithm to use
     * @return Result with generated key info (key, keyId, base64) or error
     */
    suspend fun generateAndSaveKey(algorithm: EncryptionAlgorithm): Result<GeneratedKeyInfo> {
        return try {
            logger.debug { "Presenter: Generating and saving key: algorithm=$algorithm" }

            // Execute command via CommandHandler
            val command = GenerateAndSaveKeyCommand(algorithm)
            val keyIdViewResult = generateAndSaveKeyHandler(command)

            if (keyIdViewResult.isFailure) {
                return Result.failure(keyIdViewResult.exceptionOrNull() ?: Exception("Key generation and saving failed"))
            }

            val keyIdView = keyIdViewResult.getOrThrow()
            val keyId = keyIdView.keyId

            // Load the saved key to get full key info via QueryHandler
            val query = LoadKeyQuery(keyId)
            val keyViewResult = loadKeyHandler(query)

            if (keyViewResult.isFailure) {
                val loadError = keyViewResult.exceptionOrNull() ?: Exception("Key was saved but could not be loaded")
                logger.warn(loadError) { "Key was saved but could not be loaded: keyId=$keyId" }
                return Result.failure(Exception("Key was saved but could not be loaded: ${loadError.message}"))
            }

            val keyView = keyViewResult.getOrThrow()

            // Convert View to domain entity for presentation
            val key = EncryptionKey(
                value = Base64.getDecoder().decode(keyView.keyBase64),
                algorithm = keyView.algorithm
            )

            logger.info { "Presenter: Key generated and saved successfully: keyId=$keyId, algorithm=$algorithm" }
            Result.success(
                GeneratedKeyInfo(
                    key = key,
                    keyId = keyId,
                    keyBase64 = keyView.keyBase64
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Presenter: Error generating and saving key: algorithm=$algorithm" }
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
            logger.debug { "Loading key: keyId=$keyId" }
            val query = LoadKeyQuery(keyId)
            val keyViewResult = loadKeyHandler(query)

            if (keyViewResult.isFailure) {
                return Result.failure(keyViewResult.exceptionOrNull() ?: Exception("Key not found"))
            }

            val keyView = keyViewResult.getOrThrow()

            // Convert View to domain entity for presentation
            val key = EncryptionKey(
                value = Base64.getDecoder().decode(keyView.keyBase64),
                algorithm = keyView.algorithm
            )

            Result.success(
                GeneratedKeyInfo(
                    key = key,
                    keyId = keyId,
                    keyBase64 = keyView.keyBase64
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Error loading key: keyId=$keyId" }
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
        val command = DeleteKeyCommand(keyId)
        return deleteKeyHandler(command)
    }

    /**
     * Deletes all keys.
     *
     * @return Result indicating success or failure
     */
    suspend fun deleteAllKeys(): Result<Unit> {
        return deleteAllKeysHandler(DeleteAllKeysCommand)
    }

    /**
     * Loads all saved keys.
     *
     * @return Result with list of key items or error
     */
    suspend fun loadAllKeys(): Result<List<KeyItem>> {
        return try {
            val query = LoadAllKeysQuery
            val keyViewsResult = loadAllKeysHandler(query)

            if (keyViewsResult.isFailure) {
                return Result.failure(keyViewsResult.exceptionOrNull() ?: Exception("Failed to load keys"))
            }

            val keyViews = keyViewsResult.getOrThrow()

            // Convert Views to presentation models
            val keyItems = keyViews.map { keyView ->
                KeyItem(
                    id = keyView.id,
                    algorithm = keyView.algorithm,
                    keyBase64 = keyView.keyBase64
                )
            }

            Result.success(keyItems)
        } catch (e: Exception) {
            logger.error(e) { "Error loading all keys" }
            Result.failure(e)
        }
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

/**
 * Presentation model for a saved key item.
 */
data class KeyItem(
    val id: String,
    val algorithm: EncryptionAlgorithm,
    val keyBase64: String
)
