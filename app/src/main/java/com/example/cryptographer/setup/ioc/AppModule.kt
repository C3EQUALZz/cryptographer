package com.example.cryptographer.setup.ioc

import android.content.Context
import com.example.cryptographer.application.commands.file.decrypt.AesDecryptFileCommandHandler
import com.example.cryptographer.application.commands.file.decrypt.ChaCha20DecryptFileCommandHandler
import com.example.cryptographer.application.commands.file.decrypt.TripleDesDecryptFileCommandHandler
import com.example.cryptographer.application.commands.file.encrypt.AesEncryptFileCommandHandler
import com.example.cryptographer.application.commands.file.encrypt.ChaCha20EncryptFileCommandHandler
import com.example.cryptographer.application.commands.file.encrypt.TripleDesEncryptFileCommandHandler
import com.example.cryptographer.application.commands.key.create.AesGenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.create.ChaCha20GenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.create.TripleDesGenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommandHandler
import com.example.cryptographer.application.commands.key.deleteall.DeleteAllKeysCommandHandler
import com.example.cryptographer.application.commands.language.update.SaveLanguageCommandHandler
import com.example.cryptographer.application.commands.text.convertencoding.ConvertTextEncodingCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.TripleDesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.TripleDesEncryptTextCommandHandler
import com.example.cryptographer.application.commands.theme.update.SaveThemeCommandHandler
import com.example.cryptographer.application.common.ports.file.FileGateway
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
import com.example.cryptographer.domain.text.services.TripleDesEncryptionService
import com.example.cryptographer.infrastructure.file.FileGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.adapters.key.KeyCommandGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.adapters.key.KeyQueryGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.adapters.settings.SettingsCommandGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.adapters.settings.SettingsQueryGatewayAdapter
import com.example.cryptographer.infrastructure.persistence.dao.KeyDao
import com.example.cryptographer.infrastructure.persistence.dao.SettingsDao
import com.example.cryptographer.infrastructure.persistence.database.CryptographerDatabase
import com.example.cryptographer.infrastructure.text.UuidTextIdGenerator
import com.example.cryptographer.presentation.aes.AesFilePresenter
import com.example.cryptographer.presentation.aes.AesPresenter
import com.example.cryptographer.presentation.chacha20.ChaCha20FilePresenter
import com.example.cryptographer.presentation.chacha20.ChaCha20Presenter
import com.example.cryptographer.presentation.encoding.EncodingPresenter
import com.example.cryptographer.presentation.key.KeyGenerationPresenter
import com.example.cryptographer.presentation.main.MainPresenter
import com.example.cryptographer.presentation.tdes.TripleDesFilePresenter
import com.example.cryptographer.presentation.tdes.TripleDesPresenter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
     * Provides Triple DES encryption service.
     * Creates a new instance for each injection (stateless, so safe).
     */
    @Provides
    fun provideTripleDesEncryptionService(): TripleDesEncryptionService {
        return TripleDesEncryptionService()
    }

    /**
     * Provides CryptographerDatabase instance.
     * Creates Room database for secure key storage.
     */
    @Provides
    @Singleton
    fun provideCryptographerDatabase(@ApplicationContext context: Context): CryptographerDatabase {
        return CryptographerDatabase.create(context)
    }

    /**
     * Provides KeyDao from CryptographerDatabase.
     */
    @Provides
    fun provideKeyDao(database: CryptographerDatabase): KeyDao {
        return database.keyDao()
    }

    /**
     * Provides SettingsDao from CryptographerDatabase.
     */
    @Provides
    fun provideSettingsDao(database: CryptographerDatabase): SettingsDao {
        return database.settingsDao()
    }

    /**
     * Provides KeyCommandGateway implementation.
     * Uses KeyCommandGatewayAdapter with Room and Android Keystore.
     */
    @Provides
    fun provideKeyCommandGateway(adapter: KeyCommandGatewayAdapter): KeyCommandGateway {
        return adapter
    }

    /**
     * Provides KeyQueryGateway implementation.
     * Uses KeyQueryGatewayAdapter with Room and Android Keystore.
     */
    @Provides
    fun provideKeyQueryGateway(adapter: KeyQueryGatewayAdapter): KeyQueryGateway {
        return adapter
    }

    /**
     * Provides FileGateway implementation.
     */
    @Provides
    fun provideFileGateway(adapter: FileGatewayAdapter): FileGateway {
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
     * Provides TripleDesGenerateAndSaveKeyCommandHandler.
     */
    @Provides
    fun provideTripleDesGenerateAndSaveKeyCommandHandler(
        tripleDesEncryptionService: TripleDesEncryptionService,
        commandGateway: KeyCommandGateway,
    ): TripleDesGenerateAndSaveKeyCommandHandler {
        return TripleDesGenerateAndSaveKeyCommandHandler(tripleDesEncryptionService, commandGateway)
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
     * Uses TextService for text validation to ensure consistency.
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
     * Uses TextService for text validation to ensure consistency.
     */
    @Provides
    fun provideChaCha20EncryptTextCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
        textService: TextService,
    ): ChaCha20EncryptTextCommandHandler {
        return ChaCha20EncryptTextCommandHandler(chaCha20EncryptionService, textService)
    }

    /**
     * Provides TripleDesEncryptTextCommandHandler.
     * Uses TextService for text validation to ensure consistency.
     */
    @Provides
    fun provideTripleDesEncryptTextCommandHandler(
        tripleDesEncryptionService: TripleDesEncryptionService,
        textService: TextService,
    ): TripleDesEncryptTextCommandHandler {
        return TripleDesEncryptTextCommandHandler(tripleDesEncryptionService, textService)
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
     * Provides TripleDesDecryptTextCommandHandler.
     */
    @Provides
    fun provideTripleDesDecryptTextCommandHandler(
        tripleDesEncryptionService: TripleDesEncryptionService,
    ): TripleDesDecryptTextCommandHandler {
        return TripleDesDecryptTextCommandHandler(tripleDesEncryptionService)
    }

    /**
     * Provides AesEncryptFileCommandHandler.
     */
    @Provides
    fun provideAesEncryptFileCommandHandler(
        aesEncryptionService: AesEncryptionService,
        fileGateway: FileGateway,
    ): AesEncryptFileCommandHandler {
        return AesEncryptFileCommandHandler(aesEncryptionService, fileGateway)
    }

    /**
     * Provides AesDecryptFileCommandHandler.
     */
    @Provides
    fun provideAesDecryptFileCommandHandler(
        aesEncryptionService: AesEncryptionService,
        fileGateway: FileGateway,
    ): AesDecryptFileCommandHandler {
        return AesDecryptFileCommandHandler(aesEncryptionService, fileGateway)
    }

    /**
     * Provides ChaCha20EncryptFileCommandHandler.
     */
    @Provides
    fun provideChaCha20EncryptFileCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
        fileGateway: FileGateway,
    ): ChaCha20EncryptFileCommandHandler {
        return ChaCha20EncryptFileCommandHandler(chaCha20EncryptionService, fileGateway)
    }

    /**
     * Provides ChaCha20DecryptFileCommandHandler.
     */
    @Provides
    fun provideChaCha20DecryptFileCommandHandler(
        chaCha20EncryptionService: ChaCha20EncryptionService,
        fileGateway: FileGateway,
    ): ChaCha20DecryptFileCommandHandler {
        return ChaCha20DecryptFileCommandHandler(chaCha20EncryptionService, fileGateway)
    }

    /**
     * Provides TripleDesEncryptFileCommandHandler.
     */
    @Provides
    fun provideTripleDesEncryptFileCommandHandler(
        tripleDesEncryptionService: TripleDesEncryptionService,
        fileGateway: FileGateway,
    ): TripleDesEncryptFileCommandHandler {
        return TripleDesEncryptFileCommandHandler(tripleDesEncryptionService, fileGateway)
    }

    /**
     * Provides TripleDesDecryptFileCommandHandler.
     */
    @Provides
    fun provideTripleDesDecryptFileCommandHandler(
        tripleDesEncryptionService: TripleDesEncryptionService,
        fileGateway: FileGateway,
    ): TripleDesDecryptFileCommandHandler {
        return TripleDesDecryptFileCommandHandler(tripleDesEncryptionService, fileGateway)
    }

    /**
     * Provides ConvertTextEncodingCommandHandler.
     * Uses TextService for encoding conversion to ensure consistency.
     */
    @Provides
    fun provideConvertTextEncodingCommandHandler(textService: TextService): ConvertTextEncodingCommandHandler {
        return ConvertTextEncodingCommandHandler(textService)
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
        tripleDesGenerateAndSaveKeyHandler: TripleDesGenerateAndSaveKeyCommandHandler,
        loadKeyHandler: LoadKeyQueryHandler,
        deleteKeyHandler: DeleteKeyCommandHandler,
        deleteAllKeysHandler: DeleteAllKeysCommandHandler,
        loadAllKeysHandler: LoadAllKeysQueryHandler,
    ): KeyGenerationPresenter {
        return KeyGenerationPresenter(
            aesGenerateAndSaveKeyHandler = aesGenerateAndSaveKeyHandler,
            chaCha20GenerateAndSaveKeyHandler = chaCha20GenerateAndSaveKeyHandler,
            tripleDesGenerateAndSaveKeyHandler = tripleDesGenerateAndSaveKeyHandler,
            loadKeyHandler = loadKeyHandler,
            deleteKeyHandler = deleteKeyHandler,
            deleteAllKeysHandler = deleteAllKeysHandler,
            loadAllKeysHandler = loadAllKeysHandler,
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
     * Provides TripleDesPresenter.
     */
    @Provides
    fun provideTripleDesPresenter(
        tripleDesEncryptHandler: TripleDesEncryptTextCommandHandler,
        tripleDesDecryptHandler: TripleDesDecryptTextCommandHandler,
    ): TripleDesPresenter {
        return TripleDesPresenter(
            tripleDesEncryptHandler = tripleDesEncryptHandler,
            tripleDesDecryptHandler = tripleDesDecryptHandler,
        )
    }

    /**
     * Provides AesFilePresenter.
     */
    @Provides
    fun provideAesFilePresenter(
        aesEncryptHandler: AesEncryptFileCommandHandler,
        aesDecryptHandler: AesDecryptFileCommandHandler,
    ): AesFilePresenter {
        return AesFilePresenter(
            aesEncryptHandler = aesEncryptHandler,
            aesDecryptHandler = aesDecryptHandler,
        )
    }

    /**
     * Provides ChaCha20FilePresenter.
     */
    @Provides
    fun provideChaCha20FilePresenter(
        chaCha20EncryptHandler: ChaCha20EncryptFileCommandHandler,
        chaCha20DecryptHandler: ChaCha20DecryptFileCommandHandler,
    ): ChaCha20FilePresenter {
        return ChaCha20FilePresenter(
            chaCha20EncryptHandler = chaCha20EncryptHandler,
            chaCha20DecryptHandler = chaCha20DecryptHandler,
        )
    }

    /**
     * Provides TripleDesFilePresenter.
     */
    @Provides
    fun provideTripleDesFilePresenter(
        tripleDesEncryptHandler: TripleDesEncryptFileCommandHandler,
        tripleDesDecryptHandler: TripleDesDecryptFileCommandHandler,
    ): TripleDesFilePresenter {
        return TripleDesFilePresenter(
            tripleDesEncryptHandler = tripleDesEncryptHandler,
            tripleDesDecryptHandler = tripleDesDecryptHandler,
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
     * Uses SettingsCommandGatewayAdapter with Room and Android Keystore.
     */
    @Provides
    fun provideSettingsCommandGateway(adapter: SettingsCommandGatewayAdapter): SettingsCommandGateway {
        return adapter
    }

    /**
     * Provides SettingsQueryGateway implementation.
     * Uses SettingsQueryGatewayAdapter with Room and Android Keystore.
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
