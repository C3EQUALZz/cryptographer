package com.example.cryptographer.infrastructure.persistence.adapters.key

import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.infrastructure.errors.InfrastructureError
import com.example.cryptographer.infrastructure.persistence.KeystoreHelper
import com.example.cryptographer.infrastructure.persistence.dao.KeyDao
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.security.GeneralSecurityException
import java.time.Instant
import java.util.Base64
import javax.inject.Inject

/**
 * Infrastructure adapter for KeyQueryGateway using Room and Android Keystore.
 *
 * This adapter implements the Application layer Gateway interface
 * and provides secure retrieval using:
 * - Room database for persistence
 * - Android Keystore for decrypting keys after retrieval
 *
 * Following Clean Architecture principles:
 * - Infrastructure implements Application Gateway
 * - Uses Room for persistence
 * - Uses Android Keystore for decryption
 * - Adapter pattern for layer translation
 */
class KeyQueryGatewayAdapter @Inject constructor(
    private val keyDao: KeyDao,
    private val keystoreHelper: KeystoreHelper,
) : KeyQueryGateway {
    private val logger = KotlinLogging.logger {}

    override fun getKey(keyId: String): EncryptionKey? {
        return try {
            runBlocking {
                val keyEntity = keyDao.getKeyById(keyId)

                if (keyEntity == null) {
                    logger.debug { "Key not found: keyId=$keyId" }
                    return@runBlocking null
                }

                // Decrypt the key value using Android Keystore
                val encryptedKeyBytes = Base64.getDecoder().decode(keyEntity.encryptedKeyValue)
                val decryptedKeyBytes = keystoreHelper.decrypt(encryptedKeyBytes)

                // Parse algorithm
                val algorithm = try {
                    EncryptionAlgorithm.valueOf(keyEntity.algorithm)
                } catch (e: IllegalArgumentException) {
                    logger.error(e) { "Invalid algorithm in database: ${keyEntity.algorithm}" }
                    return@runBlocking null
                }

                logger.debug {
                    "Key retrieved successfully: keyId=$keyId, " +
                        "algorithm=${keyEntity.algorithm}"
                }

                EncryptionKey(
                    id = keyEntity.id,
                    value = decryptedKeyBytes,
                    algorithm = algorithm,
                    createdAt = Instant.ofEpochMilli(keyEntity.createdAt),
                    updatedAt = Instant.ofEpochMilli(keyEntity.updatedAt),
                )
            }
        } catch (e: InfrastructureError) {
            logger.error(e) { "Failed to retrieve key: keyId=$keyId" }
            null
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to retrieve key: security error, keyId=$keyId" }
            null
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to retrieve key: illegal state, keyId=$keyId" }
            null
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to retrieve key: invalid argument, keyId=$keyId" }
            null
        }
    }

    override fun getAllKeyIds(): List<String> {
        return try {
            runBlocking {
                keyDao.getAllKeyIds()
            }
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to retrieve all key IDs: illegal state" }
            emptyList()
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to retrieve all key IDs: invalid argument" }
            emptyList()
        }
    }
}
