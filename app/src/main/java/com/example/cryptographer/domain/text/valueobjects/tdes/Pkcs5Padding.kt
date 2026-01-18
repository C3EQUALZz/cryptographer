package com.example.cryptographer.domain.text.valueobjects.tdes

/**
 * PKCS#5 padding for 64-bit block ciphers (e.g., DES/3DES).
 */
internal object Pkcs5Padding {
    private const val BLOCK_SIZE = 8
    private const val MIN_PAD_LENGTH = 1
    private const val MAX_PAD_LENGTH = 8
    private const val BYTE_MASK = 0xFF

    /**
     * Pads data to block size using PKCS#5 padding.
     */
    fun pad(data: ByteArray): ByteArray {
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
    fun unpad(data: ByteArray): ByteArray {
        val padLength = if (data.isEmpty()) 0 else extractPadLength(data)
        val canUnpad = data.isNotEmpty() &&
            isValidPadLength(padLength) &&
            isPaddingValid(data, padLength)

        return if (canUnpad) {
            data.sliceArray(0 until (data.size - padLength))
        } else {
            data
        }
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
