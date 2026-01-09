package com.example.cryptographer.setup.ioc

import com.example.cryptographer.domain.text.service.AesEncryptionService
import com.example.cryptographer.domain.text.usecase.DeleteAllKeysUseCase
import com.example.cryptographer.domain.text.usecase.DeleteKeyUseCase
import com.example.cryptographer.domain.text.usecase.GenerateEncryptionKeyUseCase
import com.example.cryptographer.domain.text.usecase.LoadAllKeysUseCase
import com.example.cryptographer.domain.text.usecase.LoadKeyUseCase
import com.example.cryptographer.domain.text.usecase.SaveKeyUseCase
import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
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
     * Provides GenerateEncryptionKeyUseCase.
     * Creates a new instance for each injection (stateless, so safe).
     */
    @Provides
    fun provideGenerateEncryptionKeyUseCase(
        aesEncryptionService: AesEncryptionService
    ): GenerateEncryptionKeyUseCase {
        return GenerateEncryptionKeyUseCase(aesEncryptionService)
    }

    /**
     * Provides SaveKeyUseCase.
     */
    @Provides
    fun provideSaveKeyUseCase(
        keyStorageAdapter: KeyStorageAdapter
    ): SaveKeyUseCase {
        return SaveKeyUseCase(keyStorageAdapter)
    }

    /**
     * Provides LoadKeyUseCase.
     */
    @Provides
    fun provideLoadKeyUseCase(
        keyStorageAdapter: KeyStorageAdapter
    ): LoadKeyUseCase {
        return LoadKeyUseCase(keyStorageAdapter)
    }

    /**
     * Provides DeleteKeyUseCase.
     */
    @Provides
    fun provideDeleteKeyUseCase(
        keyStorageAdapter: KeyStorageAdapter
    ): DeleteKeyUseCase {
        return DeleteKeyUseCase(keyStorageAdapter)
    }

    /**
     * Provides DeleteAllKeysUseCase.
     */
    @Provides
    fun provideDeleteAllKeysUseCase(
        keyStorageAdapter: KeyStorageAdapter
    ): DeleteAllKeysUseCase {
        return DeleteAllKeysUseCase(keyStorageAdapter)
    }

    /**
     * Provides LoadAllKeysUseCase.
     */
    @Provides
    fun provideLoadAllKeysUseCase(
        keyStorageAdapter: KeyStorageAdapter
    ): LoadAllKeysUseCase {
        return LoadAllKeysUseCase(keyStorageAdapter)
    }

    /**
     * Provides KeyGenerationPresenter.
     */
    @Provides
    fun provideKeyGenerationPresenter(
        generateEncryptionKeyUseCase: GenerateEncryptionKeyUseCase,
        saveKeyUseCase: SaveKeyUseCase,
        loadKeyUseCase: LoadKeyUseCase,
        deleteKeyUseCase: DeleteKeyUseCase,
        deleteAllKeysUseCase: DeleteAllKeysUseCase,
        loadAllKeysUseCase: LoadAllKeysUseCase
    ): KeyGenerationPresenter {
        return KeyGenerationPresenter(
            generateEncryptionKeyUseCase = generateEncryptionKeyUseCase,
            saveKeyUseCase = saveKeyUseCase,
            loadKeyUseCase = loadKeyUseCase,
            deleteKeyUseCase = deleteKeyUseCase,
            deleteAllKeysUseCase = deleteAllKeysUseCase,
            loadAllKeysUseCase = loadAllKeysUseCase
        )
    }
}

