package com.example.cryptographer.application.commands.file.decrypt

import com.example.cryptographer.application.common.ports.file.FileGateway
import com.example.cryptographer.application.common.views.DecryptedFileView
import com.example.cryptographer.application.errors.FileReadError
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for decrypting files using ChaCha20-Poly1305 algorithm.
 */
class ChaCha20DecryptFileCommandHandler(
    private val chaCha20EncryptionService: ChaCha20EncryptionService,
    private val fileGateway: FileGateway,
) {
    private val logger = KotlinLogging.logger {}

    private companion object {
        const val NONCE_LENGTH = 12
    }

    operator fun invoke(command: ChaCha20DecryptFileCommand): Result<DecryptedFileView> {
        return try {
            logger.debug { "ChaCha20 file decryption: input=${command.inputPath}, output=${command.outputPath}" }
            readInputBytes(command.inputPath)
                .flatMap { inputBytes -> parseEncryptedInput(inputBytes, command.key.algorithm) }
                .flatMap { encryptedText -> decryptBytes(encryptedText, command.key) }
                .flatMap { decryptedBytes -> writeOutput(command.outputPath, decryptedBytes) }
        } catch (e: AppError) {
            logger.error(e) { "ChaCha20 file decryption failed: ${e.message}" }
            Result.failure(e)
        }
    }

    private fun readInputBytes(inputPath: String): Result<ByteArray> {
        return fileGateway.readBytes(inputPath)
            .onFailure { error -> logger.error(error) { "ChaCha20 file decryption failed: read error" } }
    }

    private fun parseEncryptedInput(inputBytes: ByteArray, algorithm: EncryptionAlgorithm): Result<EncryptedText> {
        return if (inputBytes.size <= NONCE_LENGTH) {
            val error = FileReadError("Encrypted file is too short to contain nonce + data")
            logger.error(error) { "ChaCha20 file decryption failed: invalid input size" }
            Result.failure(error)
        } else {
            val nonce = inputBytes.copyOfRange(0, NONCE_LENGTH)
            val encryptedData = inputBytes.copyOfRange(NONCE_LENGTH, inputBytes.size)
            Result.success(
                EncryptedText(
                    encryptedData = encryptedData,
                    algorithm = algorithm,
                    initializationVector = nonce,
                ),
            )
        }
    }

    private fun decryptBytes(encryptedText: EncryptedText, key: EncryptionKey): Result<ByteArray> {
        return chaCha20EncryptionService.decrypt(encryptedText, key)
            .onFailure { error -> logger.error(error) { "ChaCha20 file decryption failed: decryption error" } }
    }

    private fun writeOutput(outputPath: String, decryptedBytes: ByteArray): Result<DecryptedFileView> {
        val writeResult = fileGateway.writeBytes(outputPath, decryptedBytes)
        val error = writeResult.exceptionOrNull()
        return if (error != null) {
            logger.error(error) { "ChaCha20 file decryption failed: write error" }
            Result.failure(error)
        } else {
            logger.info { "ChaCha20 file decryption successful: output=$outputPath" }
            Result.success(DecryptedFileView(outputPath))
        }
    }

    private inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
        return fold(onSuccess = transform, onFailure = { Result.failure(it) })
    }
}
