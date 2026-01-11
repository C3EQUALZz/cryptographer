package com.example.cryptographer.infrastructure.key

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Base64
import javax.inject.Inject

/**
 * Infrastructure adapter for KeyQueryGateway.
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
class KeyQueryGatewayAdapter @Inject constructor(
    @param:ApplicationContext private val context: Context
) : KeyQueryGateway {
    private val logger = KotlinLogging.logger {}

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun getKey(keyId: String): EncryptionKey? {
        return try {
            val keyBase64 = prefs.getString("${KEY_PREFIX}_${keyId}_value", null)
            val algorithmName = prefs.getString("${KEY_PREFIX}_${keyId}_algorithm", null)

            if (keyBase64 == null || algorithmName == null) {
                logger.debug { "Key not found: keyId=$keyId" }
                return null
            }

            val keyBytes = Base64.getDecoder().decode(keyBase64)
            val algorithm = EncryptionAlgorithm.valueOf(algorithmName)

            logger.debug { "Key retrieved successfully: keyId=$keyId, algorithm=$algorithmName" }
            EncryptionKey(
                value = keyBytes,
                algorithm = algorithm
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to retrieve key: keyId=$keyId" }
            null
        }
    }

    override fun getAllKeyIds(): List<String> {
        val allKeys = prefs.all.keys
        return allKeys
            .filter { it.startsWith("${KEY_PREFIX}_") && it.endsWith("_value") }
            .map { it.removePrefix("${KEY_PREFIX}_").removeSuffix("_value") }
            .distinct()
    }

    companion object {
        private const val PREFS_NAME = "cryptographer_keys"
        private const val KEY_PREFIX = "encryption_key"
    }
}

