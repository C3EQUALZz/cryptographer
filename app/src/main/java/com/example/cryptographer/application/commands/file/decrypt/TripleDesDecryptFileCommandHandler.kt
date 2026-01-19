package com.example.cryptographer.application.commands.file.decrypt

import com.example.cryptographer.application.common.ports.file.FileGateway
import com.example.cryptographer.application.common.views.DecryptedFileView
import com.example.cryptographer.application.errors.FileReadError
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.services.TripleDesEncryptionService
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for decrypting files using Triple DES algorithm.
 */
class TripleDesDecryptFileCommandHandler(
    private val tripleDesEncryptionService: TripleDesEncryptionService,
    private val fileGateway: FileGateway,
) {
    private val logger = KotlinLogging.logger {}

    private companion object {
        const val IV_LENGTH = 8
    }

    operator fun invoke(command: TripleDesDecryptFileCommand): Result<DecryptedFileView> {
        return try {
            logger.debug { "3DES file decryption: input=${command.inputPath}, output=${command.outputPath}" }
            readInputBytes(command.inputPath)
                .flatMap { inputBytes -> parseEncryptedInput(inputBytes, command.key.algorithm) }
                .flatMap { encryptedText -> decryptBytes(encryptedText, command.key) }
                .flatMap { decryptedBytes -> writeOutput(command.outputPath, decryptedBytes) }
        } catch (e: AppError) {
            logger.error(e) { "3DES file decryption failed: ${e.message}" }
            Result.failure(e)
        }
    }

    private fun readInputBytes(inputPath: String): Result<ByteArray> {
        return fileGateway.readBytes(inputPath)
            .onFailure { error -> logger.error(error) { "3DES file decryption failed: read error" } }
    }

    private fun parseEncryptedInput(inputBytes: ByteArray, algorithm: EncryptionAlgorithm): Result<EncryptedText> {
        return if (inputBytes.size <= IV_LENGTH) {
            val error = FileReadError("Encrypted file is too short to contain IV + data")
            logger.error(error) { "3DES file decryption failed: invalid input size" }
            Result.failure(error)
        } else {
            val iv = inputBytes.copyOfRange(0, IV_LENGTH)
            val encryptedData = inputBytes.copyOfRange(IV_LENGTH, inputBytes.size)
            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = algorithm,
                    initializationVector = iv,
                ),
            )
        }
    }

    private fun decryptBytes(encryptedText: EncryptedText, key: EncryptionKey): Result<ByteArray> {
        return tripleDesEncryptionService.decrypt(encryptedText, key)
            .onFailure { error -> logger.error(error) { "3DES file decryption failed: decryption error" } }
    }

    private fun writeOutput(outputPath: String, decryptedBytes: ByteArray): Result<DecryptedFileView> {
        val writeResult = fileGateway.writeBytes(outputPath, decryptedBytes)
        val error = writeResult.exceptionOrNull()
        return if (error != null) {
            logger.error(error) { "3DES file decryption failed: write error" }
            Result.failure(error)
        } else {
            logger.info { "3DES file decryption successful: output=$outputPath" }
            Result.success(DecryptedFileView(outputPath))
        }
    }

    private inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
        return fold(onSuccess = transform, onFailure = { Result.failure(it) })
    }
}
