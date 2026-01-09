package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entity.EncryptionKey
import com.example.cryptographer.domain.text.service.AesEncryptionService

/**
 * Use Case for generating AES encryption keys.
 * Encapsulates the logic of generating cryptographically secure keys.
 */
class GenerateEncryptionKeyUseCase(
    private val aesEncryptionService: AesEncryptionService
) {
    /**
     * Generates a new AES encryption key for the specified algorithm.
     *
     * @param algorithm AES algorithm (AES_128, AES_192, AES_256)
     * @return Result with generated key or error
     */
    operator fun invoke(algorithm: EncryptionAlgorithm): Result<EncryptionKey> {
        return aesEncryptionService.generateKey(algorithm)
    }
}

