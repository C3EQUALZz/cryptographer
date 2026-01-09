package com.example.cryptographer.di

import android.content.Context
import com.example.cryptographer.domain.text.service.AesEncryptionService
import com.example.cryptographer.domain.text.usecase.GenerateEncryptionKeyUseCase
import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides AES encryption service.
     */
    @Provides
    @Singleton
    fun provideAesEncryptionService(): AesEncryptionService {
        return AesEncryptionService()
    }

    /**
     * Provides GenerateEncryptionKeyUseCase.
     */
    @Provides
    @Singleton
    fun provideGenerateEncryptionKeyUseCase(
        aesEncryptionService: AesEncryptionService
    ): GenerateEncryptionKeyUseCase {
        return GenerateEncryptionKeyUseCase(aesEncryptionService)
    }

    /**
     * Provides KeyStorageAdapter.
     * KeyStorageAdapter is already annotated with @Singleton and @Inject,
     * but we can also provide it explicitly here if needed.
     * Since it has @Inject constructor, Hilt will automatically provide it.
     */
}

