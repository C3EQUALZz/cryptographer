package com.example.cryptographer.application.commands.file.encrypt

import com.example.cryptographer.application.common.ports.file.FileGateway
import com.example.cryptographer.application.common.views.EncryptedFileView
import com.example.cryptographer.application.errors.FileWriteError
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.services.TripleDesEncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for encrypting files using Triple DES algorithm.
 */
class TripleDesEncryptFileCommandHandler(
    private val tripleDesEncryptionService: TripleDesEncryptionService,
    private val fileGateway: FileGateway,
) {
    private val logger = KotlinLogging.logger {}

    private companion object {
        const val IV_LENGTH = 8
    }

    operator fun invoke(command: TripleDesEncryptFileCommand): Result<EncryptedFileView> {
        return try {
            logger.debug { "3DES file encryption: input=${command.inputPath}, output=${command.outputPath}" }
            readInputBytes(command.inputPath)
                .flatMap { inputBytes -> encryptBytes(inputBytes, command.key) }
                .flatMap { encryptedText -> buildPayload(encryptedText) }
                .flatMap { payload -> writeOutput(command.outputPath, payload) }
        } catch (e: AppError) {
            logger.error(e) { "3DES file encryption failed: ${e.message}" }
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
            .onFailure { error -> logger.error(error) { "3DES file encryption failed: read error" } }
    }

    private fun encryptBytes(inputBytes: ByteArray, key: EncryptionKey): Result<EncryptedText> {
        return tripleDesEncryptionService.encrypt(inputBytes, key)
            .onFailure { error -> logger.error(error) { "3DES file encryption failed: encryption error" } }
    }

    private fun buildPayload(encryptedText: EncryptedText): Result<EncryptedPayload> {
        val iv = encryptedText.initializationVector
        val ivError = when {
            iv == null -> FileWriteError("3DES encryption failed: IV is missing")
            iv.size != IV_LENGTH -> FileWriteError("3DES encryption failed: IV length mismatch")
            else -> null
        }

        return if (ivError != null) {
            Result.failure(ivError)
        } else {
            val ivBytes = requireNotNull(iv)
            val outputBytes = ByteArray(ivBytes.size + encryptedText.encryptedData.size)
            System.arraycopy(ivBytes, 0, outputBytes, 0, ivBytes.size)
            System.arraycopy(
                encryptedText.encryptedData,
                0,
                outputBytes,
                ivBytes.size,
                encryptedText.encryptedData.size,
            )
            Result.success(EncryptedPayload(outputBytes, ivBytes))
        }
    }

    private fun writeOutput(outputPath: String, payload: EncryptedPayload): Result<EncryptedFileView> {
        val writeResult = fileGateway.writeBytes(outputPath, payload.outputBytes)
        val error = writeResult.exceptionOrNull()
        return if (error != null) {
            logger.error(error) { "3DES file encryption failed: write error" }
            Result.failure(error)
        } else {
            logger.info {
                "3DES file encryption successful: output=$outputPath, " +
                    "size=${payload.outputBytes.size} bytes"
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
