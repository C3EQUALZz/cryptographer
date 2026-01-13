package com.example.cryptographer.infrastructure.persistence

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.cryptographer.infrastructure.errors.InfrastructureError
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for working with Android Keystore.
 *
 * Provides secure key storage and encryption/decryption operations
 * using Android Keystore system.
 *
 * Uses AES-256-GCM for encryption of sensitive data.
 */
@Singleton
class KeystoreHelper @Inject constructor() {
    private val logger = KotlinLogging.logger {}

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "cryptographer_master_key"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12 // 96 bits for GCM
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    /**
     * Gets or creates the master key for encrypting data.
     *
     * @return Secret key from Android Keystore
     * @throws com.example.cryptographer.infrastructure.errors.InfrastructureError if key creation or retrieval fails
     */
    fun getOrCreateMasterKey(): SecretKey {
        return try {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                logger.debug { "Master key found in Keystore" }
                (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
            } else {
                logger.info { "Creating new master key in Keystore" }
                createMasterKey()
            }
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to get or create master key: security error" }
            throw InfrastructureError("Failed to access Android Keystore: ${e.message}", e)
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to get or create master key: illegal state" }
            throw InfrastructureError("Failed to access Android Keystore: ${e.message}", e)
        }
    }

    /**
     * Creates a new master key in Android Keystore.
     *
     * @return Created secret key
     * @throws InfrastructureError if key creation fails
     */
    private fun createMasterKey(): SecretKey {
        return try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE,
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to create master key: security error" }
            throw InfrastructureError("Failed to create master key: ${e.message}", e)
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to create master key: illegal state" }
            throw InfrastructureError("Failed to create master key: ${e.message}", e)
        }
    }

    /**
     * Encrypts data using the master key from Android Keystore.
     *
     * @param data Data to encrypt
     * @return Encrypted data with IV prepended (IV || ciphertext)
     * @throws InfrastructureError if encryption fails
     */
    fun encrypt(data: ByteArray): ByteArray {
        return try {
            val masterKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey)

            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data)

            // Prepend IV to encrypted data: IV || ciphertext
            ByteArray(iv.size + encryptedData.size).apply {
                System.arraycopy(iv, 0, this, 0, iv.size)
                System.arraycopy(encryptedData, 0, this, iv.size, encryptedData.size)
            }
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to encrypt data: security error" }
            throw InfrastructureError("Encryption failed: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to encrypt data: invalid argument" }
            throw InfrastructureError("Encryption failed: ${e.message}", e)
        }
    }

    /**
     * Decrypts data using the master key from Android Keystore.
     *
     * @param encryptedData Encrypted data with IV prepended (IV || ciphertext)
     * @return Decrypted data
     * @throws InfrastructureError if decryption fails
     */
    fun decrypt(encryptedData: ByteArray): ByteArray {
        return try {
            require(encryptedData.size >= GCM_IV_LENGTH) {
                "Encrypted data too short. Expected at least $GCM_IV_LENGTH bytes, got ${encryptedData.size}"
            }

            val masterKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)

            // Extract IV and ciphertext
            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH)

            val ciphertext = ByteArray(encryptedData.size - GCM_IV_LENGTH)
            System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.size)

            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)

            cipher.doFinal(ciphertext)
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to decrypt data: security error" }
            throw InfrastructureError("Decryption failed: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to decrypt data: invalid argument" }
            throw InfrastructureError("Decryption failed: ${e.message}", e)
        }
    }
}
