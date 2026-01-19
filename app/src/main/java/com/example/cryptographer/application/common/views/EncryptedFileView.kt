package com.example.cryptographer.application.common.views

/**
 * View representing encrypted file result.
 */
data class EncryptedFileView(
    val outputPath: String,
    val initializationVector: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedFileView

        if (outputPath != other.outputPath) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = outputPath.hashCode()
        result = 31 * result + (initializationVector?.contentHashCode() ?: 0)
        return result
    }
}
