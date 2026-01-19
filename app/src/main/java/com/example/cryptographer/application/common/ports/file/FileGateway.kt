package com.example.cryptographer.application.common.ports.file

/**
 * Port for file read/write operations.
 *
 * This abstracts file system access away from the application layer.
 */
interface FileGateway {
    fun readBytes(path: String): Result<ByteArray>

    fun writeBytes(path: String, bytes: ByteArray): Result<Unit>
}
