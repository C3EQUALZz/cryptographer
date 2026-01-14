package com.example.cryptographer.infrastructure.security

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptographer.infrastructure.persistence.KeystoreHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app lock password securely.
 * Uses Android Keystore for encryption and SHA-256 for password hashing.
 */
@Singleton
class PasswordManager @Inject constructor(
    private val keystoreHelper: KeystoreHelper,
    @ApplicationContext private val context: Context,
) {
    private val logger = KotlinLogging.logger {}

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "app_lock_prefs"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LOCK_ENABLED = "lock_enabled"
    }

    /**
     * Checks if app lock is enabled.
     */
    fun isLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOCK_ENABLED, false)
    }

    /**
     * Enables app lock with a password.
     */
    fun setPassword(password: String): Result<Unit> {
        return try {
            val hash = hashPassword(password)
            val encryptedHash = keystoreHelper.encrypt(hash)
            val encodedHash = android.util.Base64.encodeToString(encryptedHash, android.util.Base64.DEFAULT)

            prefs.edit()
                .putString(KEY_PASSWORD_HASH, encodedHash)
                .putBoolean(KEY_LOCK_ENABLED, true)
                .apply()

            logger.info { "Password set successfully" }
            Result.success(Unit)
        } catch (e: RuntimeException) {
            logger.error(e) { "Failed to set password" }
            Result.failure(e)
        } catch (e: java.security.GeneralSecurityException) {
            logger.error(e) { "Failed to set password: security error" }
            Result.failure(e)
        }
    }

    /**
     * Verifies if the provided password is correct.
     */
    fun verifyPassword(password: String): Boolean {
        return try {
            val encodedHash = prefs.getString(KEY_PASSWORD_HASH, null)
                ?: return false

            val encryptedHash = android.util.Base64.decode(encodedHash, android.util.Base64.DEFAULT)
            val storedHash = keystoreHelper.decrypt(encryptedHash)
            val providedHash = hashPassword(password)

            storedHash.contentEquals(providedHash)
        } catch (e: RuntimeException) {
            logger.error(e) { "Failed to verify password" }
            false
        } catch (e: java.security.GeneralSecurityException) {
            logger.error(e) { "Failed to verify password: security error" }
            false
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to verify password: invalid argument" }
            false
        }
    }

    /**
     * Checks if password is set (app lock is configured).
     */
    fun hasPassword(): Boolean {
        return prefs.contains(KEY_PASSWORD_HASH) && isLockEnabled()
    }

    /**
     * Disables app lock and removes password.
     */
    fun removePassword() {
        prefs.edit()
            .remove(KEY_PASSWORD_HASH)
            .putBoolean(KEY_LOCK_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
        logger.info { "Password removed" }
    }

    /**
     * Enables or disables biometric authentication.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
        logger.info { "Biometric enabled: $enabled" }
    }

    /**
     * Checks if biometric authentication is enabled.
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Hashes password using SHA-256.
     */
    private fun hashPassword(password: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }
}
