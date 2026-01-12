package com.example.cryptographer.domain.text.services.chacha20

/**
 * Poly1305 MAC (Message Authentication Code) implementation.
 * Implements Poly1305 as specified in RFC 8439.
 *
 * This class follows Single Responsibility Principle - it only handles
 * Poly1305 MAC computation.
 */
internal object Poly1305 {
    private const val TAG_SIZE = 16 // bytes (128 bits)
    private const val BLOCK_SIZE = 16 // bytes

    /**
     * Clamps a 32-byte key to get the r value for Poly1305.
     * Sets certain bits to zero as specified in RFC 8439.
     *
     * @param key 32-byte key
     * @return Clamped r value (16 bytes)
     */
    private fun clamp(key: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be exactly 32 bytes" }
        val r = ByteArray(16)
        key.copyInto(r, 0, 0, 16)

        // Clamp r according to RFC 8439
        r[3] = (r[3].toInt() and 0x0F).toByte()
        r[7] = (r[7].toInt() and 0x0F).toByte()
        r[11] = (r[11].toInt() and 0x0F).toByte()
        r[15] = (r[15].toInt() and 0x0F).toByte()
        r[4] = (r[4].toInt() and 0xFC).toByte()
        r[8] = (r[8].toInt() and 0xFC).toByte()
        r[12] = (r[12].toInt() and 0xFC).toByte()

        return r
    }

    /**
     * Converts bytes to little-endian integer.
     */
    private fun bytesToInt(bytes: ByteArray, offset: Int): Long {
        return (bytes[offset].toLong() and 0xFF) or
            ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
            ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
            ((bytes[offset + 3].toLong() and 0xFF) shl 24)
    }

    /**
     * Converts integer to little-endian bytes.
     */
    private fun intToBytes(value: Long, bytes: ByteArray, offset: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    /**
     * Computes Poly1305 MAC.
     *
     * @param message Message to authenticate
     * @param key 32-byte key (first 16 bytes used for r, last 16 bytes for s)
     * @return 16-byte authentication tag
     */
    fun compute(message: ByteArray, key: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be exactly 32 bytes" }

        val r = clamp(key)
        val s = ByteArray(16)
        key.copyInto(s, 0, 16, 32)

        // Convert r and s to integers (little-endian)
        val r0 = bytesToInt(r, 0)
        val r1 = bytesToInt(r, 4)
        val r2 = bytesToInt(r, 8)
        val r3 = bytesToInt(r, 12)

        val s0 = bytesToInt(s, 0)
        val s1 = bytesToInt(s, 4)
        val s2 = bytesToInt(s, 8)
        val s3 = bytesToInt(s, 12)

        // Mask for 26-bit limbs
        val mask26 = (1L shl 26) - 1
        val mask32 = 0xFFFFFFFFL

        // Split r into 26-bit limbs
        val r0L = r0 and mask26
        val r0H = r0 ushr 26
        val r1L = r1 and mask26
        val r1H = r1 ushr 26
        val r2L = r2 and mask26
        val r2H = r2 ushr 26
        val r3L = r3 and mask26
        val r3H = r3 ushr 26

        // Accumulator (5 limbs of 26 bits each)
        var h0 = 0L
        var h1 = 0L
        var h2 = 0L
        var h3 = 0L
        var h4 = 0L

        // Process message in 16-byte blocks
        var offset = 0
        while (offset < message.size) {
            val blockSize = minOf(BLOCK_SIZE, message.size - offset)
            val block = ByteArray(BLOCK_SIZE)
            message.copyInto(block, 0, offset, offset + blockSize)

            // Add 0x01 padding byte after incomplete blocks (RFC 8439 section 2.5.1)
            // This padding is added at position blockSize, which affects the little-endian interpretation
            if (blockSize < BLOCK_SIZE) {
                block[blockSize] = 0x01
            }

            // Convert block to integer (little-endian)
            // Note: The padding byte at position blockSize will be included in the appropriate word
            val block0 = bytesToInt(block, 0)
            val block1 = bytesToInt(block, 4)
            val block2 = bytesToInt(block, 8)
            val block3 = bytesToInt(block, 12)

            // Split block into 26-bit limbs
            val block0L = block0 and mask26
            val block0H = block0 ushr 26
            val block1L = block1 and mask26
            val block1H = block1 ushr 26
            val block2L = block2 and mask26
            val block2H = block2 ushr 26
            val block3L = block3 and mask26
            val block3H = block3 ushr 26

            // Add block to accumulator
            h0 += block0L
            h1 += block0H + block1L
            h2 += block1H + block2L
            h3 += block2H + block3L
            h4 += block3H

            // Multiply by r and reduce
            val d0 = h0 * r0L + h1 * r3H + h2 * r2H + h3 * r1H + h4 * r0H
            var d1 = h0 * r1L + h1 * r0L + h2 * r3H + h3 * r2H + h4 * r1H
            var d2 = h0 * r2L + h1 * r1L + h2 * r0L + h3 * r3H + h4 * r2H
            var d3 = h0 * r3L + h1 * r2L + h2 * r1L + h3 * r0L + h4 * r3H
            var d4 = h0 * r0H + h1 * r3L + h2 * r2L + h3 * r1L + h4 * r0L

            // Carry propagation
            var c = d0 ushr 26
            h0 = d0 and mask26
            d1 += c

            c = d1 ushr 26
            h1 = d1 and mask26
            d2 += c

            c = d2 ushr 26
            h2 = d2 and mask26
            d3 += c

            c = d3 ushr 26
            h3 = d3 and mask26
            d4 += c

            c = d4 ushr 26
            h4 = d4 and mask26
            h0 += c * 5

            c = h0 ushr 26
            h0 = h0 and mask26
            h1 += c

            offset += blockSize
        }

        // Final reduction
        var c = h1 ushr 26
        h1 = h1 and mask26
        h2 += c

        c = h2 ushr 26
        h2 = h2 and mask26
        h3 += c

        c = h3 ushr 26
        h3 = h3 and mask26
        h4 += c

        c = h4 ushr 26
        h0 += c * 5

        c = h0 ushr 26
        h0 = h0 and mask26
        h1 += c

        // Add s
        h0 = (h0 + s0) and mask32
        h1 = (h1 + s1) and mask32
        h2 = (h2 + s2) and mask32
        h3 = (h3 + s3) and mask32

        // Convert to bytes (little-endian)
        val tag = ByteArray(TAG_SIZE)
        intToBytes(h0, tag, 0)
        intToBytes(h1, tag, 4)
        intToBytes(h2, tag, 8)
        intToBytes(h3, tag, 12)

        return tag
    }
}
