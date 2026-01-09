package com.example.cryptographer.setup.ioc

import com.example.cryptographer.application.commands.text.convert_encoding.ConvertTextEncodingCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.DecryptTextCommandHandler
import com.example.cryptographer.application.commands.key.delete_all.DeleteAllKeysCommandHandler
import com.example.cryptographer.application.commands.key.delete.DeleteKeyCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.EncryptTextCommandHandler
import com.example.cryptographer.application.commands.key.create.GenerateAndSaveKeyCommandHandler
import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.ports.key.KeyQueryGateway
import com.example.cryptographer.application.queries.key.read_all.LoadAllKeysQueryHandler
import com.example.cryptographer.application.queries.key.read_by_id.LoadKeyQueryHandler
import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.infrastructure.key.KeyCommandGatewayAdapter
import com.example.cryptographer.infrastructure.key.KeyQueryGatewayAdapter
import com.example.cryptographer.infrastructure.text.UuidTextIdGenerator
import com.example.cryptographer.presentation.encoding.EncodingPresenter
import com.example.cryptographer.presentation.encryption.EncryptionPresenter
import com.example.cryptographer.presentation.key.KeyGenerationPresenter
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
     * Provides GenerateAndSaveKeyCommandHandler.
     */
    @Provides
    fun provideGenerateAndSaveKeyCommandHandler(
        aesEncryptionService: AesEncryptionService,
        commandGateway: KeyCommandGateway
    ): GenerateAndSaveKeyCommandHandler {
        return GenerateAndSaveKeyCommandHandler(aesEncryptionService, commandGateway)
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
     * Provides EncryptTextCommandHandler.
     * Uses TextService for text validation to ensure consistency.
     */
    @Provides
    fun provideEncryptTextCommandHandler(
        aesEncryptionService: AesEncryptionService,
        textService: TextService
    ): EncryptTextCommandHandler {
        return EncryptTextCommandHandler(aesEncryptionService, textService)
    }

    /**
     * Provides DecryptTextCommandHandler.
     */
    @Provides
    fun provideDecryptTextCommandHandler(
        aesEncryptionService: AesEncryptionService
    ): DecryptTextCommandHandler {
        return DecryptTextCommandHandler(aesEncryptionService)
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
        generateAndSaveKeyHandler: GenerateAndSaveKeyCommandHandler,
        loadKeyHandler: LoadKeyQueryHandler,
        deleteKeyHandler: DeleteKeyCommandHandler,
        deleteAllKeysHandler: DeleteAllKeysCommandHandler,
        loadAllKeysHandler: LoadAllKeysQueryHandler
    ): KeyGenerationPresenter {
        return KeyGenerationPresenter(
            generateAndSaveKeyHandler = generateAndSaveKeyHandler,
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
        encryptTextHandler: EncryptTextCommandHandler,
        decryptTextHandler: DecryptTextCommandHandler
    ): EncryptionPresenter {
        return EncryptionPresenter(
            encryptTextHandler = encryptTextHandler,
            decryptTextHandler = decryptTextHandler
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
}

