package com.example.cryptographer.presentation.chacha20

import com.example.cryptographer.application.commands.file.decrypt.ChaCha20DecryptFileCommand
import com.example.cryptographer.application.commands.file.decrypt.ChaCha20DecryptFileCommandHandler
import com.example.cryptographer.application.commands.file.encrypt.ChaCha20EncryptFileCommand
import com.example.cryptographer.application.commands.file.encrypt.ChaCha20EncryptFileCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64
import javax.inject.Inject

/**
 * Presenter for ChaCha20 file encryption/decryption screen.
 */
class ChaCha20FilePresenter @Inject constructor(
    private val chaCha20EncryptHandler: ChaCha20EncryptFileCommandHandler,
    private val chaCha20DecryptHandler: ChaCha20DecryptFileCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    fun encryptFile(inputPath: String, outputPath: String, key: EncryptionKey): Result<EncryptedFileInfo> {
        return try {
            logger.debug { "ChaCha20 file presenter: encrypt file, algorithm=${key.algorithm}" }

            if (key.algorithm != EncryptionAlgorithm.CHACHA20_256) {
                Result.failure(AppError("Invalid algorithm for ChaCha20 file encryption: ${key.algorithm}"))
            } else {
                val command = ChaCha20EncryptFileCommand(inputPath, outputPath, key)
                val result = chaCha20EncryptHandler(command).getOrElse { error ->
                    logger.error(error) { "ChaCha20 file encryption failed: ${error.message}" }
                    return Result.failure(error)
                }

                val nonceBase64 = result.initializationVector?.let { Base64.getEncoder().encodeToString(it) }
                Result.success(
                    EncryptedFileInfo(
                        outputPath = result.outputPath,
                        nonceBase64 = nonceBase64,
                    ),
                )
            }
        } catch (e: AppError) {
            logger.error(e) { "ChaCha20 file presenter error: ${e.message}" }
            Result.failure(e)
        }
    }

    fun decryptFile(inputPath: String, outputPath: String, key: EncryptionKey): Result<DecryptedFileInfo> {
        return try {
            logger.debug { "ChaCha20 file presenter: decrypt file, algorithm=${key.algorithm}" }

            val result = if (key.algorithm != EncryptionAlgorithm.CHACHA20_256) {
                Result.failure(AppError("Invalid algorithm for ChaCha20 file decryption: ${key.algorithm}"))
            } else {
                val command = ChaCha20DecryptFileCommand(inputPath, outputPath, key)
                chaCha20DecryptHandler(command)
            }

            result.map { DecryptedFileInfo(it.outputPath) }
        } catch (e: AppError) {
            logger.error(e) { "ChaCha20 file presenter error: ${e.message}" }
            Result.failure(e)
        }
    }
}

data class EncryptedFileInfo(
    val outputPath: String,
    val nonceBase64: String?,
)

data class DecryptedFileInfo(
    val outputPath: String,
)
