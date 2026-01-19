package com.example.cryptographer.presentation.aes

import com.example.cryptographer.application.commands.file.decrypt.AesDecryptFileCommand
import com.example.cryptographer.application.commands.file.decrypt.AesDecryptFileCommandHandler
import com.example.cryptographer.application.commands.file.encrypt.AesEncryptFileCommand
import com.example.cryptographer.application.commands.file.encrypt.AesEncryptFileCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64
import javax.inject.Inject

/**
 * Presenter for AES file encryption/decryption screen.
 */
class AesFilePresenter @Inject constructor(
    private val aesEncryptHandler: AesEncryptFileCommandHandler,
    private val aesDecryptHandler: AesDecryptFileCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    fun encryptFile(inputPath: String, outputPath: String, key: EncryptionKey): Result<EncryptedFileInfo> {
        return try {
            logger.debug { "AES file presenter: encrypt file, algorithm=${key.algorithm}" }

            if (key.algorithm !in listOf(
                    EncryptionAlgorithm.AES_128,
                    EncryptionAlgorithm.AES_192,
                    EncryptionAlgorithm.AES_256,
                )
            ) {
                Result.failure(AppError("Invalid algorithm for AES file encryption: ${key.algorithm}"))
            } else {
                val command = AesEncryptFileCommand(inputPath, outputPath, key)
                val result = aesEncryptHandler(command).getOrElse { error ->
                    logger.error(error) { "AES file encryption failed: ${error.message}" }
                    return Result.failure(error)
                }

                val ivBase64 = result.initializationVector?.let { Base64.getEncoder().encodeToString(it) }
                Result.success(
                    EncryptedFileInfo(
                        outputPath = result.outputPath,
                        ivBase64 = ivBase64,
                    ),
                )
            }
        } catch (e: AppError) {
            logger.error(e) { "AES file presenter error: ${e.message}" }
            Result.failure(e)
        }
    }

    fun decryptFile(inputPath: String, outputPath: String, key: EncryptionKey): Result<DecryptedFileInfo> {
        return try {
            logger.debug { "AES file presenter: decrypt file, algorithm=${key.algorithm}" }

            val result = if (key.algorithm !in listOf(
                    EncryptionAlgorithm.AES_128,
                    EncryptionAlgorithm.AES_192,
                    EncryptionAlgorithm.AES_256,
                )
            ) {
                Result.failure(AppError("Invalid algorithm for AES file decryption: ${key.algorithm}"))
            } else {
                val command = AesDecryptFileCommand(inputPath, outputPath, key)
                aesDecryptHandler(command)
            }

            result.map { DecryptedFileInfo(it.outputPath) }
        } catch (e: AppError) {
            logger.error(e) { "AES file presenter error: ${e.message}" }
            Result.failure(e)
        }
    }
}

data class EncryptedFileInfo(
    val outputPath: String,
    val ivBase64: String?,
)

data class DecryptedFileInfo(
    val outputPath: String,
)
