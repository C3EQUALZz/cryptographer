package com.example.cryptographer.infrastructure.key

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.entity.EncryptionAlgorithm
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Infrastructure adapter for storing and retrieving encryption keys.
 * Uses SharedPreferences for simple key-value storage.
 * 
 * Note: In production, consider using Android Keystore for better security.
 */
@Singleton
class KeyStorageAdapter @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves an encryption key with a given identifier.
     */
    fun saveKey(keyId: String, key: EncryptionKey): Boolean {
        return try {
            val keyBase64 = Base64.getEncoder().encodeToString(key.value)
            val algorithmName = key.algorithm.name
            
            prefs.edit()
                .putString("${KEY_PREFIX}_${keyId}_value", keyBase64)
                .putString("${KEY_PREFIX}_${keyId}_algorithm", algorithmName)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves an encryption key by identifier.
     */
    fun getKey(keyId: String): EncryptionKey? {
        return try {
            val keyBase64 = prefs.getString("${KEY_PREFIX}_${keyId}_value", null)
            val algorithmName = prefs.getString("${KEY_PREFIX}_${keyId}_algorithm", null)
            
            if (keyBase64 == null || algorithmName == null) {
                return null
            }
            
            val keyBytes = Base64.getDecoder().decode(keyBase64)
            val algorithm = EncryptionAlgorithm.valueOf(algorithmName)
            
            EncryptionKey(
                value = keyBytes,
                algorithm = algorithm
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets all saved key identifiers.
     */
    fun getAllKeyIds(): List<String> {
        val allKeys = prefs.all.keys
        return allKeys
            .filter { it.startsWith("${KEY_PREFIX}_") && it.endsWith("_value") }
            .map { it.removePrefix("${KEY_PREFIX}_").removeSuffix("_value") }
            .distinct()
    }

    /**
     * Deletes a key by identifier.
     */
    fun deleteKey(keyId: String): Boolean {
        return try {
            prefs.edit()
                .remove("${KEY_PREFIX}_${keyId}_value")
                .remove("${KEY_PREFIX}_${keyId}_algorithm")
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val PREFS_NAME = "cryptographer_keys"
        private const val KEY_PREFIX = "encryption_key"
    }
}

