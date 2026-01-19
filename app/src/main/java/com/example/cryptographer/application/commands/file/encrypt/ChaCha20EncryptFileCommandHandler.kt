package com.example.cryptographer.application.commands.file.encrypt

import com.example.cryptographer.application.common.ports.file.FileGateway
import com.example.cryptographer.application.common.views.EncryptedFileView
import com.example.cryptographer.application.errors.FileWriteError
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for encrypting files using ChaCha20-Poly1305 algorithm.
 */
class ChaCha20EncryptFileCommandHandler(
    private val chaCha20EncryptionService: ChaCha20EncryptionService,
    private val fileGateway: FileGateway,
) {
    private val logger = KotlinLogging.logger {}

    private companion object {
        const val NONCE_LENGTH = 12
    }

    operator fun invoke(command: ChaCha20EncryptFileCommand): Result<EncryptedFileView> {
        return try {
            logger.debug { "ChaCha20 file encryption: input=${command.inputPath}, output=${command.outputPath}" }
            readInputBytes(command.inputPath)
                .flatMap { inputBytes -> encryptBytes(inputBytes, command.key) }
                .flatMap { encryptedText -> buildPayload(encryptedText) }
                .flatMap { payload -> writeOutput(command.outputPath, payload) }
        } catch (e: AppError) {
            logger.error(e) { "ChaCha20 file encryption failed: ${e.message}" }
            Result.failure(e)
        }
    }

    private data class EncryptedPayload(
        val outputBytes: ByteArray,
        val initializationVector: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EncryptedPayload

            if (!outputBytes.contentEquals(other.outputBytes)) return false
            if (!initializationVector.contentEquals(other.initializationVector)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = outputBytes.contentHashCode()
            result = 31 * result + initializationVector.contentHashCode()
            return result
        }
    }

    private fun readInputBytes(inputPath: String): Result<ByteArray> {
        return fileGateway.readBytes(inputPath)
            .onFailure { error -> logger.error(error) { "ChaCha20 file encryption failed: read error" } }
    }

    private fun encryptBytes(inputBytes: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return chaCha20EncryptionService.encrypt(inputBytes, key)
            .onFailure { error -> logger.error(error) { "ChaCha20 file encryption failed: encryption error" } }
    }

    private fun buildPayload(encryptedText: EncryptedText): Result<EncryptedPayload> {
        val nonce = encryptedText.initializationVector
        val nonceError = when {
            nonce == null -> FileWriteError("ChaCha20 encryption failed: nonce is missing")
            nonce.size != NONCE_LENGTH -> FileWriteError("ChaCha20 encryption failed: nonce length mismatch")
            else -> null
        }

        return if (nonceError != null) {
            Result.failure(nonceError)
        } else {
            val nonceBytes = requireNotNull(nonce)
            val outputBytes = ByteArray(nonceBytes.size + encryptedText.encryptedData.size)
            System.arraycopy(nonceBytes, 0, outputBytes, 0, nonceBytes.size)
            System.arraycopy(
                encryptedText.encryptedData,
                0,
                outputBytes,
                nonceBytes.size,
                encryptedText.encryptedData.size,
            )
            Result.success(EncryptedPayload(outputBytes, nonceBytes))
        }
    }

    private fun writeOutput(outputPath: String, payload: EncryptedPayload): Result<EncryptedFileView> {
        val writeResult = fileGateway.writeBytes(outputPath, payload.outputBytes)
        val error = writeResult.exceptionOrNull()
        return if (error != null) {
            logger.error(error) { "ChaCha20 file encryption failed: write error" }
            Result.failure(error)
        } else {
            logger.info {
                "ChaCha20 file encryption successful: " +
                    "output=$outputPath, size=${payload.outputBytes.size} bytes"
            }
            Result.success(
                EncryptedFileView(
                    outputPath = outputPath,
                    initializationVector = payload.initializationVector,
                ),
            )
        }
    }

    private inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
        return fold(onSuccess = transform, onFailure = { Result.failure(it) })
    }
}
