package com.example.cryptographer.infrastructure.persistence.adapters.key

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.infrastructure.persistence.KeystoreHelper
import com.example.cryptographer.infrastructure.persistence.dao.KeyDao
import com.example.cryptographer.infrastructure.persistence.models.KeyEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.security.GeneralSecurityException
import java.util.Base64
import javax.inject.Inject

/**
 * Infrastructure adapter for KeyCommandGateway using Room and Android Keystore.
 *
 * This adapter implements the Application layer Gateway interface
 * and provides secure storage using:
 * - Room database for persistence
 * - Android Keystore for encrypting keys before storage
 *
 * Following Clean Architecture principles:
 * - Infrastructure implements Application Gateway
 * - Uses Room for persistence
 * - Uses Android Keystore for encryption
 * - Adapter pattern for layer translation
 */
class KeyCommandGatewayAdapter @Inject constructor(
    private val keyDao: KeyDao,
    private val keystoreHelper: KeystoreHelper,
) : KeyCommandGateway {
    private val logger = KotlinLogging.logger {}

    override fun saveKey(keyId: String, key: EncryptionKey): Boolean {
        return try {
            runBlocking {
                // Encrypt the key value using Android Keystore
                val encryptedKeyBytes = keystoreHelper.encrypt(key.value)
                val encryptedKeyBase64 = Base64.getEncoder().encodeToString(encryptedKeyBytes)

                // Create entity with encrypted key
                val keyEntity = KeyEntity(
                    id = keyId,
                    encryptedKeyValue = encryptedKeyBase64,
                    algorithm = key.algorithm.name,
                    createdAt = key.createdAt.toEpochMilli(),
                    updatedAt = key.updatedAt.toEpochMilli(),
                )

                // Save to Room database
                keyDao.insertKey(keyEntity)

                logger.debug {
                    "Key saved securely: keyId=$keyId, " +
                        "algorithm=${key.algorithm}, " +
                        "encryptedSize=${encryptedKeyBytes.size} bytes"
                }
                true
            }
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to save key: security error, keyId=$keyId, algorithm=${key.algorithm}" }
            false
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to save key: illegal state, keyId=$keyId, algorithm=${key.algorithm}" }
            false
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to save key: invalid argument, keyId=$keyId, algorithm=${key.algorithm}" }
            false
        }
    }

    override fun deleteKey(keyId: String): Boolean {
        return try {
            runBlocking {
                keyDao.deleteKeyById(keyId)
                logger.debug { "Key deleted successfully: keyId=$keyId" }
                true
            }
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to delete key: illegal state, keyId=$keyId" }
            false
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to delete key: invalid argument, keyId=$keyId" }
            false
        }
    }

    override fun deleteAllKeys(): Boolean {
        return try {
            runBlocking {
                val allKeyIds = keyDao.getAllKeyIds()
                logger.debug { "Deleting all keys: count=${allKeyIds.size}" }

                keyDao.deleteAllKeys()

                logger.info { "All keys deleted successfully: count=${allKeyIds.size}" }
                true
            }
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to delete all keys: illegal state" }
            false
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to delete all keys: invalid argument" }
            false
        }
    }
}
