package com.example.cryptographer.presentation.tdes

import com.example.cryptographer.application.commands.file.decrypt.TripleDesDecryptFileCommand
import com.example.cryptographer.application.commands.file.decrypt.TripleDesDecryptFileCommandHandler
import com.example.cryptographer.application.commands.file.encrypt.TripleDesEncryptFileCommand
import com.example.cryptographer.application.commands.file.encrypt.TripleDesEncryptFileCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64
import javax.inject.Inject

/**
 * Presenter for Triple DES file encryption/decryption screen.
 */
class TripleDesFilePresenter @Inject constructor(
    private val tripleDesEncryptHandler: TripleDesEncryptFileCommandHandler,
    private val tripleDesDecryptHandler: TripleDesDecryptFileCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    fun encryptFile(inputPath: String, outputPath: String, key: EncryptionKey): Result<EncryptedFileInfo> {
        return try {
            logger.debug { "3DES file presenter: encrypt file, algorithm=${key.algorithm}" }

            if (key.algorithm != EncryptionAlgorithm.TDES_112 && key.algorithm != EncryptionAlgorithm.TDES_168) {
                Result.failure(AppError("Invalid algorithm for 3DES file encryption: ${key.algorithm}"))
            } else {
                val command = TripleDesEncryptFileCommand(inputPath, outputPath, key)
                val result = tripleDesEncryptHandler(command).getOrElse { error ->
                    logger.error(error) { "3DES file encryption failed: ${error.message}" }
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
            logger.error(e) { "3DES file presenter error: ${e.message}" }
            Result.failure(e)
        }
    }

    fun decryptFile(inputPath: String, outputPath: String, key: EncryptionKey): Result<DecryptedFileInfo> {
        return try {
            logger.debug { "3DES file presenter: decrypt file, algorithm=${key.algorithm}" }

            val result = if (
                key.algorithm != EncryptionAlgorithm.TDES_112 &&
                key.algorithm != EncryptionAlgorithm.TDES_168
            ) {
                Result.failure(AppError("Invalid algorithm for 3DES file decryption: ${key.algorithm}"))
            } else {
                val command = TripleDesDecryptFileCommand(inputPath, outputPath, key)
                tripleDesDecryptHandler(command)
            }

            result.map { DecryptedFileInfo(it.outputPath) }
        } catch (e: AppError) {
            logger.error(e) { "3DES file presenter error: ${e.message}" }
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
