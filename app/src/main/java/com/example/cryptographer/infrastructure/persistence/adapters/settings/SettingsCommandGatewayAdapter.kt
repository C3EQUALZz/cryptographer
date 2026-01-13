package com.example.cryptographer.infrastructure.persistence.adapters.settings

import com.example.cryptographer.application.common.ports.settings.SettingsCommandGateway
import com.example.cryptographer.infrastructure.persistence.KeystoreHelper
import com.example.cryptographer.infrastructure.persistence.dao.SettingsDao
import com.example.cryptographer.infrastructure.persistence.models.SettingsEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.GeneralSecurityException
import java.time.Instant
import java.util.Base64
import javax.inject.Inject

/**
 * Infrastructure adapter for SettingsCommandGateway using Room and Android Keystore.
 *
 * This adapter implements the Application layer Gateway interface
 * and provides secure storage using:
 * - Room database for persistence
 * - Android Keystore for encrypting settings before storage
 *
 * Following Clean Architecture principles:
 * - Infrastructure implements Application Gateway
 * - Uses Room for persistence
 * - Uses Android Keystore for encryption
 * - Adapter pattern for layer translation
 */
class SettingsCommandGatewayAdapter @Inject constructor(
    private val settingsDao: SettingsDao,
    private val keystoreHelper: KeystoreHelper,
) : SettingsCommandGateway {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
    }

    override suspend fun saveThemeMode(themeMode: String): Boolean {
        return try {
            // Encrypt the theme mode using Android Keystore
            val encryptedValueBytes = keystoreHelper.encrypt(themeMode.toByteArray(Charsets.UTF_8))
            val encryptedValueBase64 = Base64.getEncoder().encodeToString(encryptedValueBytes)

            // Create entity with encrypted value
            val settingEntity = SettingsEntity(
                key = KEY_THEME_MODE,
                encryptedValue = encryptedValueBase64,
                createdAt = Instant.now().toEpochMilli(),
                updatedAt = Instant.now().toEpochMilli(),
            )

            // Save to Room database
            settingsDao.upsertSetting(settingEntity)

            logger.debug {
                "Theme mode saved securely: themeMode=$themeMode, " +
                    "encryptedSize=${encryptedValueBytes.size} bytes"
            }
            true
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to save theme mode: security error, themeMode=$themeMode" }
            false
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to save theme mode: illegal state, themeMode=$themeMode" }
            false
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to save theme mode: invalid argument, themeMode=$themeMode" }
            false
        }
    }

    override suspend fun saveLanguage(languageCode: String): Boolean {
        return try {
            // Encrypt the language code using Android Keystore
            val encryptedValueBytes = keystoreHelper.encrypt(languageCode.toByteArray(Charsets.UTF_8))
            val encryptedValueBase64 = Base64.getEncoder().encodeToString(encryptedValueBytes)

            // Create entity with encrypted value
            val settingEntity = SettingsEntity(
                key = KEY_SELECTED_LANGUAGE,
                encryptedValue = encryptedValueBase64,
                createdAt = Instant.now().toEpochMilli(),
                updatedAt = Instant.now().toEpochMilli(),
            )

            // Save to Room database
            settingsDao.upsertSetting(settingEntity)

            logger.debug {
                "Language saved securely: languageCode=$languageCode, " +
                    "encryptedSize=${encryptedValueBytes.size} bytes"
            }
            true
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to save language: security error, languageCode=$languageCode" }
            false
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to save language: illegal state, languageCode=$languageCode" }
            false
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to save language: invalid argument, languageCode=$languageCode" }
            false
        }
    }
}
