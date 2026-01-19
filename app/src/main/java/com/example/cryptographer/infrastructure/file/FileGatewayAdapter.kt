package com.example.cryptographer.infrastructure.file

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.cryptographer.application.common.ports.file.FileGateway
import com.example.cryptographer.application.errors.FileReadError
import com.example.cryptographer.application.errors.FileWriteError
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * File gateway adapter that uses the local file system.
 */
class FileGatewayAdapter @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : FileGateway {
    override fun readBytes(path: String): Result<ByteArray> {
        if (path.isBlank()) {
            return Result.failure(FileReadError("Input file path is empty"))
        }

        return try {
            val uri = path.toUri()
            when (uri.scheme) {
                "content" -> readFromContentUri(uri)
                "file" -> readFromFilePath(uri.path.orEmpty())
                else -> readFromFilePath(path)
            }
        } catch (e: IOException) {
            Result.failure(FileReadError("Failed to read input file: $path", e))
        } catch (e: SecurityException) {
            Result.failure(FileReadError("Failed to read input file: $path", e))
        }
    }

    override fun writeBytes(path: String, bytes: ByteArray): Result<Unit> {
        if (path.isBlank()) {
            return Result.failure(FileWriteError("Output file path is empty"))
        }

        return try {
            val uri = path.toUri()
            when (uri.scheme) {
                "content" -> writeToContentUri(uri, bytes)
                "file" -> writeToFilePath(uri.path.orEmpty(), bytes)
                else -> writeToFilePath(path, bytes)
            }
        } catch (e: IOException) {
            Result.failure(FileWriteError("Failed to write output file: $path", e))
        } catch (e: SecurityException) {
            Result.failure(FileWriteError("Failed to write output file: $path", e))
        }
    }

    private fun readFromFilePath(path: String): Result<ByteArray> {
        val file = File(path)
        return if (!file.exists()) {
            Result.failure(FileReadError("Input file not found: $path"))
        } else {
            Result.success(file.readBytes())
        }
    }

    private fun readFromContentUri(uri: Uri): Result<ByteArray> {
        val inputStream = context.contentResolver.openInputStream(uri)
        return if (inputStream == null) {
            Result.failure(FileReadError("Input file not found: $uri"))
        } else {
            Result.success(inputStream.use { it.readBytes() })
        }
    }

    private fun writeToFilePath(path: String, bytes: ByteArray): Result<Unit> {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return Result.success(Unit)
    }

    private fun writeToContentUri(uri: Uri, bytes: ByteArray): Result<Unit> {
        val outputStream = context.contentResolver.openOutputStream(uri)
        return if (outputStream == null) {
            Result.failure(FileWriteError("Failed to open output file: $uri"))
        } else {
            outputStream.use { it.write(bytes) }
            Result.success(Unit)
        }
    }
}
