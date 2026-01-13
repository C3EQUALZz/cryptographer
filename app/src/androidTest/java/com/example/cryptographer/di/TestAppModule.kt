package com.example.cryptographer.di

import com.example.cryptographer.application.commands.key.create.AesGenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.create.ChaCha20GenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.create.TripleDesGenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommandHandler
import com.example.cryptographer.application.commands.key.deleteall.DeleteAllKeysCommandHandler
import com.example.cryptographer.application.commands.language.update.SaveLanguageCommandHandler
import com.example.cryptographer.application.commands.text.convertencoding.ConvertTextEncodingCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommandHandler
import com.example.cryptographer.application.commands.theme.update.SaveThemeCommandHandler
import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.application.common.ports.settings.SettingsCommandGateway
import com.example.cryptographer.application.common.ports.settings.SettingsQueryGateway
import com.example.cryptographer.application.queries.key.readall.LoadAllKeysQueryHandler
import com.example.cryptographer.application.queries.key.readbyid.LoadKeyQueryHandler
import com.example.cryptographer.application.queries.language.read.LoadLanguageQueryHandler
import com.example.cryptographer.application.queries.theme.read.LoadThemeQueryHandler
import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.infrastructure.persistence.adapters.key.KeyCommandGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.adapters.key.KeyQueryGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.adapters.settings.SettingsCommandGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.adapters.settings.SettingsQueryGatewayAdapter
import com.example.cryptographer.infrastructure.text.UuidTextIdGenerator
import com.example.cryptographer.presentation.aes.AesPresenter
import com.example.cryptographer.presentation.chacha20.ChaCha20Presenter
import com.example.cryptographer.presentation.encoding.EncodingPresenter
import com.example.cryptographer.presentation.key.KeyGenerationPresenter
import com.example.cryptographer.presentation.main.MainPresenter
import com.example.cryptographer.setup.ioc.AppModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

/**
 * Test module that replaces production dependencies for testing.
 *
 * This module provides all the same dependencies as AppModule,
 * but can be customized for testing (e.g., using mocks or test doubles).
 *
 * Note: No @Singleton annotations - creates new instances for each injection,
 * following the same pattern as AppModule.
 *
 * Usage: Annotate test class with @HiltAndroidTest and this module
 * will automatically replace AppModule.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class],
)
@Suppress("unused") // Hilt uses this module via annotation processing
object TestAppModule {

    /**
     * Provides AES encryption service for testing.
     */
    @Provides
    fun provideAesEncryptionService(): AesEncryptionService {
        return AesEncryptionService()
    }

    /**
     * Provides ChaCha20 encryption service for testing.
     */
    @Provides
    fun provideChaCha20EncryptionService(): ChaCha20EncryptionService {
        return ChaCha20EncryptionService()
    }

    /**
     * Provides KeyCommandGateway implementation.
     */
    @Provides
    fun provideKeyCommandGateway(adapter: KeyCommandGatewayAdapter): KeyCommandGateway {
        return adapter
    }

    /**
     * Provides KeyQueryGateway implementation.
     */
    @Provides
    fun provideKeyQueryGateway(adapter: KeyQueryGatewayAdapter): KeyQueryGateway {
        return adapter
    }

    /**
     * Provides AesGenerateAndSaveKeyCommandHandler.
     */
    @Provides
    fun provideAesGenerateAndSaveKeyCommandHandler(
        aesEncryptionService: AesEncryptionService,
        commandGateway: KeyCommandGateway,
    ): AesGenerateAndSaveKeyCommandHandler {
        return AesGenerateAndSaveKeyCommandHandler(aesEncryptionService, commandGateway)
    }

    /**
     * Provides ChaCha20GenerateAndSaveKeyCommandHandler.
     */
    @Provides
    fun provideChaCha20GenerateAndSaveKeyCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
        commandGateway: KeyCommandGateway,
    ): ChaCha20GenerateAndSaveKeyCommandHandler {
        return ChaCha20GenerateAndSaveKeyCommandHandler(chaCha20EncryptionService, commandGateway)
    }

    /**
     * Provides LoadKeyQueryHandler.
     */
    @Provides
    fun provideLoadKeyQueryHandler(queryGateway: KeyQueryGateway): LoadKeyQueryHandler {
        return LoadKeyQueryHandler(queryGateway)
    }

    /**
     * Provides DeleteKeyCommandHandler.
     */
    @Provides
    fun provideDeleteKeyCommandHandler(commandGateway: KeyCommandGateway): DeleteKeyCommandHandler {
        return DeleteKeyCommandHandler(commandGateway)
    }

    /**
     * Provides DeleteAllKeysCommandHandler.
     */
    @Provides
    fun provideDeleteAllKeysCommandHandler(commandGateway: KeyCommandGateway): DeleteAllKeysCommandHandler {
        return DeleteAllKeysCommandHandler(commandGateway)
    }

    /**
     * Provides LoadAllKeysQueryHandler.
     */
    @Provides
    fun provideLoadAllKeysQueryHandler(queryGateway: KeyQueryGateway): LoadAllKeysQueryHandler {
        return LoadAllKeysQueryHandler(queryGateway)
    }

    /**
     * Provides AesEncryptTextCommandHandler.
     */
    @Provides
    fun provideAesEncryptTextCommandHandler(
        aesEncryptionService: AesEncryptionService,
        textService: TextService,
    ): AesEncryptTextCommandHandler {
        return AesEncryptTextCommandHandler(aesEncryptionService, textService)
    }

    /**
     * Provides ChaCha20EncryptTextCommandHandler.
     */
    @Provides
    fun provideChaCha20EncryptTextCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
        textService: TextService,
    ): ChaCha20EncryptTextCommandHandler {
        return ChaCha20EncryptTextCommandHandler(chaCha20EncryptionService, textService)
    }

    /**
     * Provides AesDecryptTextCommandHandler.
     */
    @Provides
    fun provideAesDecryptTextCommandHandler(aesEncryptionService: AesEncryptionService): AesDecryptTextCommandHandler {
        return AesDecryptTextCommandHandler(aesEncryptionService)
    }

    /**
     * Provides ChaCha20DecryptTextCommandHandler.
     */
    @Provides
    fun provideChaCha20DecryptTextCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
    ): ChaCha20DecryptTextCommandHandler {
        return ChaCha20DecryptTextCommandHandler(chaCha20EncryptionService)
    }

    /**
     * Provides ConvertTextEncodingCommandHandler.
     */
    @Provides
    fun provideConvertTextEncodingCommandHandler(textService: TextService): ConvertTextEncodingCommandHandler {
        return ConvertTextEncodingCommandHandler(textService)
    }

    /**
     * Provides TextIdGeneratorPort implementation.
     */
    @Provides
    fun provideTextIdGeneratorPort(): TextIdGeneratorPort {
        return UuidTextIdGenerator()
    }

    /**
     * Provides TextService.
     */
    @Provides
    fun provideTextService(textIdGenerator: TextIdGeneratorPort): TextService {
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
        loadAllKeysHandler: LoadAllKeysQueryHandler,
        tripleDesGenerateAndSaveKeyCommandHandler: TripleDesGenerateAndSaveKeyCommandHandler,
    ): KeyGenerationPresenter {
        return KeyGenerationPresenter(
            aesGenerateAndSaveKeyHandler = aesGenerateAndSaveKeyHandler,
            chaCha20GenerateAndSaveKeyHandler = chaCha20GenerateAndSaveKeyHandler,
            loadKeyHandler = loadKeyHandler,
            deleteKeyHandler = deleteKeyHandler,
            deleteAllKeysHandler = deleteAllKeysHandler,
            loadAllKeysHandler = loadAllKeysHandler,
            tripleDesGenerateAndSaveKeyHandler = tripleDesGenerateAndSaveKeyCommandHandler,
        )
    }

    /**
     * Provides AesPresenter.
     */
    @Provides
    fun provideAesPresenter(
        aesEncryptHandler: AesEncryptTextCommandHandler,
        aesDecryptHandler: AesDecryptTextCommandHandler,
    ): AesPresenter {
        return AesPresenter(
            aesEncryptHandler = aesEncryptHandler,
            aesDecryptHandler = aesDecryptHandler,
        )
    }

    /**
     * Provides ChaCha20Presenter.
     */
    @Provides
    fun provideChaCha20Presenter(
        chaCha20EncryptHandler: ChaCha20EncryptTextCommandHandler,
        chaCha20DecryptHandler: ChaCha20DecryptTextCommandHandler,
    ): ChaCha20Presenter {
        return ChaCha20Presenter(
            chaCha20EncryptHandler = chaCha20EncryptHandler,
            chaCha20DecryptHandler = chaCha20DecryptHandler,
        )
    }

    /**
     * Provides EncodingPresenter.
     */
    @Provides
    fun provideEncodingPresenter(convertTextEncodingHandler: ConvertTextEncodingCommandHandler): EncodingPresenter {
        return EncodingPresenter(convertTextEncodingHandler)
    }

    /**
     * Provides SettingsCommandGateway implementation.
     */
    @Provides
    fun provideSettingsCommandGateway(adapter: SettingsCommandGatewayAdapter): SettingsCommandGateway {
        return adapter
    }

    /**
     * Provides SettingsQueryGateway implementation.
     */
    @Provides
    fun provideSettingsQueryGateway(adapter: SettingsQueryGatewayAdapter): SettingsQueryGateway {
        return adapter
    }

    /**
     * Provides SaveThemeCommandHandler.
     */
    @Provides
    fun provideSaveThemeCommandHandler(commandGateway: SettingsCommandGateway): SaveThemeCommandHandler {
        return SaveThemeCommandHandler(commandGateway)
    }

    /**
     * Provides LoadThemeQueryHandler.
     */
    @Provides
    fun provideLoadThemeQueryHandler(queryGateway: SettingsQueryGateway): LoadThemeQueryHandler {
        return LoadThemeQueryHandler(queryGateway)
    }

    /**
     * Provides SaveLanguageCommandHandler.
     */
    @Provides
    fun provideSaveLanguageCommandHandler(commandGateway: SettingsCommandGateway): SaveLanguageCommandHandler {
        return SaveLanguageCommandHandler(commandGateway)
    }

    /**
     * Provides LoadLanguageQueryHandler.
     */
    @Provides
    fun provideLoadLanguageQueryHandler(queryGateway: SettingsQueryGateway): LoadLanguageQueryHandler {
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
        loadLanguageHandler: LoadLanguageQueryHandler,
    ): MainPresenter {
        return MainPresenter(
            saveThemeHandler = saveThemeHandler,
            loadThemeHandler = loadThemeHandler,
            saveLanguageHandler = saveLanguageHandler,
            loadLanguageHandler = loadLanguageHandler,
        )
    }
}
