package com.example.cryptographer.infrastructure.key

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.domain.text.entities.EncryptionKey
import io.github.oshai.kotlinlogging.KotlinLogging
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Base64
import javax.inject.Inject

/**
 * Infrastructure adapter for KeyCommandGateway.
 *
 * This adapter implements the Application layer Gateway interface
 * and provides the actual implementation using SharedPreferences.
 *
 * Following Clean Architecture principles:
 * - Infrastructure implements Application Gateway
 * - Uses SharedPreferences for persistence
 * - Adapter pattern for layer translation
 *
 * Note: In production, consider using Android Keystore for better security.
 */
class KeyCommandGatewayAdapter @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val queryGateway: KeyQueryGateway
) : KeyCommandGateway {
    private val logger = KotlinLogging.logger {}

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun saveKey(keyId: String, key: EncryptionKey): Boolean {
        return try {
            val keyBase64 = Base64.getEncoder().encodeToString(key.value)
            val algorithmName = key.algorithm.name

            prefs.edit {
                putString("${KEY_PREFIX}_${keyId}_value", keyBase64)
                putString("${KEY_PREFIX}_${keyId}_algorithm", algorithmName)
            }
            logger.debug { "Key saved successfully: keyId=$keyId, algorithm=$algorithmName" }
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to save key: keyId=$keyId, algorithm=${key.algorithm}" }
            false
        }
    }

    override fun deleteKey(keyId: String): Boolean {
        return try {
            prefs.edit {
                remove("${KEY_PREFIX}_${keyId}_value")
                remove("${KEY_PREFIX}_${keyId}_algorithm")
            }
            logger.debug { "Key deleted successfully: keyId=$keyId" }
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete key: keyId=$keyId" }
            false
        }
    }

    override fun deleteAllKeys(): Boolean {
        return try {
            val allKeyIds = queryGateway.getAllKeyIds()
            logger.debug { "Deleting all keys: count=${allKeyIds.size}" }

            prefs.edit {
                allKeyIds.forEach { keyId ->
                    remove("${KEY_PREFIX}_${keyId}_value")
                    remove("${KEY_PREFIX}_${keyId}_algorithm")
                }
            }
            logger.info { "All keys deleted successfully: count=${allKeyIds.size}" }
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete all keys" }
            false
        }
    }

    companion object {
        private const val PREFS_NAME = "cryptographer_keys"
        private const val KEY_PREFIX = "encryption_key"
    }
}

