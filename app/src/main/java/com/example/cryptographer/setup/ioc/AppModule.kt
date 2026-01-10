package com.example.cryptographer.setup.ioc

import com.example.cryptographer.application.commands.text.convert_encoding.ConvertTextEncodingCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommandHandler
import com.example.cryptographer.application.commands.key.delete_all.DeleteAllKeysCommandHandler
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommandHandler
import com.example.cryptographer.application.commands.key.create.AesGenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.create.ChaCha20GenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.application.queries.key.read_all.LoadAllKeysQueryHandler
import com.example.cryptographer.application.queries.key.read_by_id.LoadKeyQueryHandler
import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.application.commands.language.update.SaveLanguageCommandHandler
import com.example.cryptographer.application.commands.theme.update.SaveThemeCommandHandler
import com.example.cryptographer.application.common.ports.settings.SettingsCommandGateway
import com.example.cryptographer.application.common.ports.settings.SettingsQueryGateway
import com.example.cryptographer.application.queries.language.read.LoadLanguageQueryHandler
import com.example.cryptographer.application.queries.theme.read.LoadThemeQueryHandler
import com.example.cryptographer.infrastructure.key.KeyCommandGatewayAdapter
import com.example.cryptographer.infrastructure.key.KeyQueryGatewayAdapter
import com.example.cryptographer.infrastructure.settings.SettingsCommandGatewayAdapter
import com.example.cryptographer.infrastructure.settings.SettingsQueryGatewayAdapter
import com.example.cryptographer.infrastructure.text.UuidTextIdGenerator
import com.example.cryptographer.presentation.encoding.EncodingPresenter
import com.example.cryptographer.presentation.encryption.EncryptionPresenter
import com.example.cryptographer.presentation.key.KeyGenerationPresenter
import com.example.cryptographer.presentation.main.MainPresenter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing application-wide dependencies.
 *
 * Note: This module is automatically discovered by Hilt through annotation processing.
 * The "unused" warning from IDEA is expected - Hilt uses this module via reflection.
 *
 * Scope strategy:
 * - No @Singleton annotation: New instances are created for each injection.
 *   This is safe for stateless classes (services, use cases) and avoids potential issues
 *   with shared state or lifecycle management.
 */
@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused") // Hilt uses this module via annotation processing
object AppModule {

    /**
     * Provides AES encryption service.
     * Creates a new instance for each injection (stateless, so safe).
     */
    @Provides
    fun provideAesEncryptionService(): AesEncryptionService {
        return AesEncryptionService()
    }

    /**
     * Provides ChaCha20 encryption service.
     * Creates a new instance for each injection (stateless, so safe).
     * Note: ChaCha20 is available in Java 11+ and Android API 28+.
     */
    @Provides
    fun provideChaCha20EncryptionService(): ChaCha20EncryptionService {
        return ChaCha20EncryptionService()
    }

    /**
     * Provides KeyCommandGateway implementation.
     * Uses KeyCommandGatewayAdapter as the implementation.
     */
    @Provides
    fun provideKeyCommandGateway(
        adapter: KeyCommandGatewayAdapter
    ): KeyCommandGateway {
        return adapter
    }

    /**
     * Provides KeyQueryGateway implementation.
     * Uses KeyQueryGatewayAdapter as the implementation.
     */
    @Provides
    fun provideKeyQueryGateway(
        adapter: KeyQueryGatewayAdapter
    ): KeyQueryGateway {
        return adapter
    }

    /**
     * Provides AesGenerateAndSaveKeyCommandHandler.
     */
    @Provides
    fun provideAesGenerateAndSaveKeyCommandHandler(
        aesEncryptionService: AesEncryptionService,
        commandGateway: KeyCommandGateway
    ): AesGenerateAndSaveKeyCommandHandler {
        return AesGenerateAndSaveKeyCommandHandler(aesEncryptionService, commandGateway)
    }

    /**
     * Provides ChaCha20GenerateAndSaveKeyCommandHandler.
     */
    @Provides
    fun provideChaCha20GenerateAndSaveKeyCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
        commandGateway: KeyCommandGateway
    ): ChaCha20GenerateAndSaveKeyCommandHandler {
        return ChaCha20GenerateAndSaveKeyCommandHandler(chaCha20EncryptionService, commandGateway)
    }

    /**
     * Provides LoadKeyQueryHandler.
     */
    @Provides
    fun provideLoadKeyQueryHandler(
        queryGateway: KeyQueryGateway
    ): LoadKeyQueryHandler {
        return LoadKeyQueryHandler(queryGateway)
    }

    /**
     * Provides DeleteKeyCommandHandler.
     */
    @Provides
    fun provideDeleteKeyCommandHandler(
        commandGateway: KeyCommandGateway
    ): DeleteKeyCommandHandler {
        return DeleteKeyCommandHandler(commandGateway)
    }

    /**
     * Provides DeleteAllKeysCommandHandler.
     */
    @Provides
    fun provideDeleteAllKeysCommandHandler(
        commandGateway: KeyCommandGateway
    ): DeleteAllKeysCommandHandler {
        return DeleteAllKeysCommandHandler(commandGateway)
    }

    /**
     * Provides LoadAllKeysQueryHandler.
     */
    @Provides
    fun provideLoadAllKeysQueryHandler(
        queryGateway: KeyQueryGateway
    ): LoadAllKeysQueryHandler {
        return LoadAllKeysQueryHandler(queryGateway)
    }

    /**
     * Provides AesEncryptTextCommandHandler.
     * Uses TextService for text validation to ensure consistency.
     */
    @Provides
    fun provideAesEncryptTextCommandHandler(
        aesEncryptionService: AesEncryptionService,
        textService: TextService
    ): AesEncryptTextCommandHandler {
        return AesEncryptTextCommandHandler(aesEncryptionService, textService)
    }

    /**
     * Provides ChaCha20EncryptTextCommandHandler.
     * Uses TextService for text validation to ensure consistency.
     */
    @Provides
    fun provideChaCha20EncryptTextCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
        textService: TextService
    ): ChaCha20EncryptTextCommandHandler {
        return ChaCha20EncryptTextCommandHandler(chaCha20EncryptionService, textService)
    }

    /**
     * Provides AesDecryptTextCommandHandler.
     */
    @Provides
    fun provideAesDecryptTextCommandHandler(
        aesEncryptionService: AesEncryptionService
    ): AesDecryptTextCommandHandler {
        return AesDecryptTextCommandHandler(aesEncryptionService)
    }

    /**
     * Provides ChaCha20DecryptTextCommandHandler.
     */
    @Provides
    fun provideChaCha20DecryptTextCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService
    ): ChaCha20DecryptTextCommandHandler {
        return ChaCha20DecryptTextCommandHandler(chaCha20EncryptionService)
    }

    /**
     * Provides ConvertTextEncodingCommandHandler.
     */
    @Provides
    fun provideConvertTextEncodingCommandHandler(): ConvertTextEncodingCommandHandler {
        return ConvertTextEncodingCommandHandler()
    }

    /**
     * Provides TextIdGeneratorPort implementation.
     * Uses UUID for generating unique text IDs.
     */
    @Provides
    fun provideTextIdGeneratorPort(): TextIdGeneratorPort {
        return UuidTextIdGenerator()
    }

    /**
     * Provides TextService.
     * Domain service for creating Text entities with ID generation.
     */
    @Provides
    fun provideTextService(
        textIdGenerator: TextIdGeneratorPort
    ): TextService {
        return TextService(textIdGenerator)
    }

    /**
     * Provides KeyGenerationPresenter.
     */
    @Provides
    fun provideKeyGenerationPresenter(
        aesGenerateAndSaveKeyHandler: AesGenerateAndSaveKeyCommandHandler,
        chaCha20GenerateAndSaveKeyHandler: ChaCha20GenerateAndSaveKeyCommandHandler,
        loadKeyHandler: LoadKeyQueryHandler,
        deleteKeyHandler: DeleteKeyCommandHandler,
        deleteAllKeysHandler: DeleteAllKeysCommandHandler,
        loadAllKeysHandler: LoadAllKeysQueryHandler
    ): KeyGenerationPresenter {
        return KeyGenerationPresenter(
            aesGenerateAndSaveKeyHandler = aesGenerateAndSaveKeyHandler,
            chaCha20GenerateAndSaveKeyHandler = chaCha20GenerateAndSaveKeyHandler,
            loadKeyHandler = loadKeyHandler,
            deleteKeyHandler = deleteKeyHandler,
            deleteAllKeysHandler = deleteAllKeysHandler,
            loadAllKeysHandler = loadAllKeysHandler
        )
    }

    /**
     * Provides EncryptionPresenter.
     */
    @Provides
    fun provideEncryptionPresenter(
        aesEncryptHandler: AesEncryptTextCommandHandler,
        chaCha20EncryptHandler: ChaCha20EncryptTextCommandHandler,
        aesDecryptHandler: AesDecryptTextCommandHandler,
        chaCha20DecryptHandler: ChaCha20DecryptTextCommandHandler
    ): EncryptionPresenter {
        return EncryptionPresenter(
            aesEncryptHandler = aesEncryptHandler,
            chaCha20EncryptHandler = chaCha20EncryptHandler,
            aesDecryptHandler = aesDecryptHandler,
            chaCha20DecryptHandler = chaCha20DecryptHandler
        )
    }

    /**
     * Provides EncodingPresenter.
     */
    @Provides
    fun provideEncodingPresenter(
        convertTextEncodingHandler: ConvertTextEncodingCommandHandler
    ): EncodingPresenter {
        return EncodingPresenter(convertTextEncodingHandler)
    }

    /**
     * Provides SettingsCommandGateway implementation.
     * Uses SettingsCommandGatewayAdapter as the implementation.
     */
    @Provides
    fun provideSettingsCommandGateway(
        adapter: SettingsCommandGatewayAdapter
    ): SettingsCommandGateway {
        return adapter
    }

    /**
     * Provides SettingsQueryGateway implementation.
     * Uses SettingsQueryGatewayAdapter as the implementation.
     */
    @Provides
    fun provideSettingsQueryGateway(
        adapter: SettingsQueryGatewayAdapter
    ): SettingsQueryGateway {
        return adapter
    }

    /**
     * Provides SaveThemeCommandHandler.
     */
    @Provides
    fun provideSaveThemeCommandHandler(
        commandGateway: SettingsCommandGateway
    ): SaveThemeCommandHandler {
        return SaveThemeCommandHandler(commandGateway)
    }

    /**
     * Provides LoadThemeQueryHandler.
     */
    @Provides
    fun provideLoadThemeQueryHandler(
        queryGateway: SettingsQueryGateway
    ): LoadThemeQueryHandler {
        return LoadThemeQueryHandler(queryGateway)
    }

    /**
     * Provides SaveLanguageCommandHandler.
     */
    @Provides
    fun provideSaveLanguageCommandHandler(
        commandGateway: SettingsCommandGateway
    ): SaveLanguageCommandHandler {
        return SaveLanguageCommandHandler(commandGateway)
    }

    /**
     * Provides LoadLanguageQueryHandler.
     */
    @Provides
    fun provideLoadLanguageQueryHandler(
        queryGateway: SettingsQueryGateway
    ): LoadLanguageQueryHandler {
        return LoadLanguageQueryHandler(queryGateway)
    }

    /**
     * Provides MainPresenter.
     */
    @Provides
    fun provideMainPresenter(
        saveThemeHandler: SaveThemeCommandHandler,
        loadThemeHandler: LoadThemeQueryHandler,
        saveLanguageHandler: SaveLanguageCommandHandler,
        loadLanguageHandler: LoadLanguageQueryHandler
    ): MainPresenter {
        return MainPresenter(
            saveThemeHandler = saveThemeHandler,
            loadThemeHandler = loadThemeHandler,
            saveLanguageHandler = saveLanguageHandler,
            loadLanguageHandler = loadLanguageHandler
        )
    }
}

