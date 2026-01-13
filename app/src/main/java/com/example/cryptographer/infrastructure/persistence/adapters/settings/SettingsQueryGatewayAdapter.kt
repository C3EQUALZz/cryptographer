package com.example.cryptographer.infrastructure.persistence.adapters.settings

import com.example.cryptographer.application.common.ports.settings.SettingsQueryGateway
import com.example.cryptographer.infrastructure.persistence.KeystoreHelper
import com.example.cryptographer.infrastructure.persistence.dao.SettingsDao
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.GeneralSecurityException
import java.util.Base64
import javax.inject.Inject

/**
 * Infrastructure adapter for SettingsQueryGateway using Room and Android Keystore.
 *
 * This adapter implements the Application layer Gateway interface
 * and provides secure retrieval using:
 * - Room database for persistence
 * - Android Keystore for decrypting settings after retrieval
 *
 * Following Clean Architecture principles:
 * - Infrastructure implements Application Gateway
 * - Uses Room for persistence
 * - Uses Android Keystore for decryption
 * - Adapter pattern for layer translation
 */
class SettingsQueryGatewayAdapter @Inject constructor(
    private val settingsDao: SettingsDao,
    private val keystoreHelper: KeystoreHelper,
) : SettingsQueryGateway {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_LANGUAGE = "en"
    }

    override suspend fun loadThemeMode(): String {
        return try {
            val settingEntity = settingsDao.getSettingByKey(KEY_THEME_MODE)

            if (settingEntity == null) {
                logger.debug { "Theme mode not found, returning default: $DEFAULT_THEME" }
                return DEFAULT_THEME
            }

            // Decrypt the value using Android Keystore
            val encryptedValueBytes = Base64.getDecoder().decode(settingEntity.encryptedValue)
            val decryptedValueBytes = keystoreHelper.decrypt(encryptedValueBytes)
            val themeMode = String(decryptedValueBytes, Charsets.UTF_8)

            logger.debug { "Theme mode loaded successfully: themeMode=$themeMode" }
            themeMode
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to load theme mode: security error, returning default: $DEFAULT_THEME" }
            DEFAULT_THEME
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to load theme mode: illegal state, returning default: $DEFAULT_THEME" }
            DEFAULT_THEME
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to load theme mode: invalid argument, returning default: $DEFAULT_THEME" }
            DEFAULT_THEME
        }
    }

    override suspend fun loadLanguage(): String {
        return try {
            val settingEntity = settingsDao.getSettingByKey(KEY_SELECTED_LANGUAGE)

            if (settingEntity == null) {
                logger.debug { "Language not found, returning default: $DEFAULT_LANGUAGE" }
                return DEFAULT_LANGUAGE
            }

            // Decrypt the value using Android Keystore
            val encryptedValueBytes = Base64.getDecoder().decode(settingEntity.encryptedValue)
            val decryptedValueBytes = keystoreHelper.decrypt(encryptedValueBytes)
            val languageCode = String(decryptedValueBytes, Charsets.UTF_8)

            logger.debug { "Language loaded successfully: languageCode=$languageCode" }
            languageCode
        } catch (e: GeneralSecurityException) {
            logger.error(e) { "Failed to load language: security error, returning default: $DEFAULT_LANGUAGE" }
            DEFAULT_LANGUAGE
        } catch (e: IllegalStateException) {
            logger.error(e) { "Failed to load language: illegal state, returning default: $DEFAULT_LANGUAGE" }
            DEFAULT_LANGUAGE
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to load language: invalid argument, returning default: $DEFAULT_LANGUAGE" }
            DEFAULT_LANGUAGE
        }
    }
}
