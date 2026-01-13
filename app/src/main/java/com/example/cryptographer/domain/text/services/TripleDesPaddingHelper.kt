package com.example.cryptographer.domain.text.services

/**
 * Helper object for PKCS#5 padding operations used in 3DES encryption.
 * Extracted to reduce function count in TripleDesEncryptionService.
 */
internal object TripleDesPaddingHelper {
    private const val BLOCK_SIZE = 8
    private const val MIN_PAD_LENGTH = 1
    private const val MAX_PAD_LENGTH = 8
    private const val BYTE_MASK = 0xFF

    /**
     * Pads data to block size using PKCS#5 padding.
     */
    fun padData(data: ByteArray): ByteArray {
        val padLength = BLOCK_SIZE - (data.size % BLOCK_SIZE)
        val padded = ByteArray(data.size + padLength)
        System.arraycopy(data, 0, padded, 0, data.size)
        for (i in data.size until padded.size) {
            padded[i] = padLength.toByte()
        }
        return padded
    }

    /**
     * Removes PKCS#5 padding from data.
     * Returns original data if padding is invalid.
     */
    fun removePadding(data: ByteArray): ByteArray {
        if (data.isEmpty()) {
            return data
        }

        val padLength = extractPadLength(data)
        if (!isValidPadLength(padLength) || !isPaddingValid(data, padLength)) {
            return data // Invalid padding, return as-is
        }

        return data.sliceArray(0 until (data.size - padLength))
    }

    private fun extractPadLength(data: ByteArray): Int {
        return data[data.size - 1].toInt() and BYTE_MASK
    }

    private fun isValidPadLength(padLength: Int): Boolean {
        return padLength in MIN_PAD_LENGTH..MAX_PAD_LENGTH
    }

    private fun isPaddingValid(data: ByteArray, padLength: Int): Boolean {
        val paddingStart = data.size - padLength
        for (i in paddingStart until data.size) {
            if ((data[i].toInt() and BYTE_MASK) != padLength) {
                return false
            }
        }
        return true
    }
}
