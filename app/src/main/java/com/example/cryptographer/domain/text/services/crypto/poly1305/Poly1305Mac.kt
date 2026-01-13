package com.example.cryptographer.domain.text.services.crypto.poly1305

/**
 * Poly1305 MAC (Message Authentication Code) implementation.
 *
 * Poly1305 is a one-time authenticator designed by Daniel J. Bernstein.
 * It generates a 128-bit (16-byte) authentication tag.
 *
 * Follows Single Responsibility Principle: responsible only for generating
 * and verifying Poly1305 authentication tags.
 *
 * Reference: RFC 8439 Section 2.5
 * Implementation based on Base-2^26 scalar multiplication approach from Crypto++.
 */
internal class Poly1305Mac {
    private val tagSize = 16 // bytes (128 bits)

    /**
     * Generates a Poly1305 authentication tag.
     *
     * @param message Message to authenticate
     * @param key 32-byte one-time key (r || s, where r is 16 bytes and s is 16 bytes)
     * @return 16-byte authentication tag
     */
    fun generateTag(message: ByteArray, key: ByteArray): ByteArray {
        require(key.size == 32) { "Poly1305 key must be 32 bytes" }

        // Extract r (first 16 bytes) and clamp it
        val r = clampR(key.copyOfRange(0, 16))

        // Extract s (last 16 bytes) - this is the nonce for final addition
        val s = key.copyOfRange(16, 32)

        // Convert r to 4 32-bit words (little-endian) with clamping
        val r0 = bytesToWord32(r, 0) and 0x0FFFFFFF
        val r1 = bytesToWord32(r, 4) and 0x0FFFFFFC
        val r2 = bytesToWord32(r, 8) and 0x0FFFFFFC
        val r3 = bytesToWord32(r, 12) and 0x0FFFFFFC

        // Precompute s1, s2, s3 for faster multiplication (s1 = r1 + (r1 >> 2))
        val s1 = r1 + (r1 shr 2)
        val s2 = r2 + (r2 shr 2)
        val s3 = r3 + (r3 shr 2)

        // Accumulator (5 limbs, each 32 bits)
        var h0 = 0
        var h1 = 0
        var h2 = 0
        var h3 = 0
        var h4 = 0

        // Process message in 16-byte blocks
        var offset = 0
        while (offset < message.size) {
            val blockSize = minOf(16, message.size - offset)
            val block = ByteArray(16)
            System.arraycopy(message, offset, block, 0, blockSize)

            // Poly1305 padbit: 1 for full blocks, 0 for last (potentially partial) block
            val padBit = if (blockSize == 16) 1 else 0
            if (blockSize < 16) {
                block[blockSize] = 1
                // Zero out the rest
                for (i in blockSize + 1 until 16) {
                    block[i] = 0
                }
            }

            // h += m[i] (add message block to accumulator)
            val m0 = bytesToWord32(block, 0).toLong() and 0xFFFFFFFFL
            val m1 = bytesToWord32(block, 4).toLong() and 0xFFFFFFFFL
            val m2 = bytesToWord32(block, 8).toLong() and 0xFFFFFFFFL
            val m3 = bytesToWord32(block, 12).toLong() and 0xFFFFFFFFL

            // Add block to accumulator with carry propagation (exactly as in C++ code)
            var d0: Long = h0.toLong() + m0
            h0 = d0.toInt()
            var d1: Long = h1.toLong() + (d0 shr 32) + m1
            h1 = d1.toInt()
            var d2: Long = h2.toLong() + (d1 shr 32) + m2
            h2 = d2.toInt()
            var d3: Long = h3.toLong() + (d2 shr 32) + m3
            h3 = d3.toInt()
            h4 += (d3 shr 32).toInt() + padBit

            // h *= r "%" p (multiply by r and reduce modulo 2^130 - 5)
            d0 = (h0.toLong() * r0) +
                (h1.toLong() * s3) +
                (h2.toLong() * s2) +
                (h3.toLong() * s1)

            d1 = (h0.toLong() * r1) +
                (h1.toLong() * r0) +
                (h2.toLong() * s3) +
                (h3.toLong() * s2) +
                (h4.toLong() * s1)

            d2 = (h0.toLong() * r2) +
                (h1.toLong() * r1) +
                (h2.toLong() * r0) +
                (h3.toLong() * s3) +
                (h4.toLong() * s2)

            d3 = (h0.toLong() * r3) +
                (h1.toLong() * r2) +
                (h2.toLong() * r1) +
                (h3.toLong() * r0) +
                (h4.toLong() * s3)

            val h4New = h4.toLong() * r0

            // Combine results: h4:h0 = h4New<<128 + d3<<96 + d2<<64 + d1<<32 + d0
            h0 = d0.toInt()
            d1 += d0 shr 32
            h1 = d1.toInt()
            d2 += d1 shr 32
            h2 = d2.toInt()
            d3 += d2 shr 32
            h3 = d3.toInt()
            h4 = h4New.toInt() + (d3 shr 32).toInt()

            // Partial reduction: (h4:h0 += (h4:h0>>130) * 5) %= 2^130
            // c = (h4 >> 2) + (h4 & ~3)
            var c = (h4 shr 2) + (h4 and (0xFFFFFFFC.toInt().inv()))
            h4 = h4 and 3
            h0 += c * 5
            c = constantTimeCarry(h0, c * 5)
            h1 += c
            c = constantTimeCarry(h1, c)
            h2 += c
            c = constantTimeCarry(h2, c)
            h3 += c
            h4 += constantTimeCarry(h3, c)

            offset += blockSize
        }

        // Final reduction: compare to modulus by computing h + -p
        val g0 = (h0.toLong() + 5).toInt()
        var t: Long = h1.toLong() + (g0.toLong() shr 32)
        val g1 = t.toInt()
        t = h2.toLong() + (t shr 32)
        val g2 = t.toInt()
        t = h3.toLong() + (t shr 32)
        val g3 = t.toInt()
        val g4 = h4 + (t shr 32).toInt()

        // If there was carry into 131st bit, h3:h0 = g3:g0
        val mask = 0 - (g4 shr 2)
        val g0Masked = g0 and mask
        val g1Masked = g1 and mask
        val g2Masked = g2 and mask
        val g3Masked = g3 and mask
        val maskInv = mask.inv()
        h0 = (h0 and maskInv) or g0Masked
        h1 = (h1 and maskInv) or g1Masked
        h2 = (h2 and maskInv) or g2Masked
        h3 = (h3 and maskInv) or g3Masked

        // mac = (h + nonce) % (2^128)
        val n0 = bytesToWord32(s, 0)
        val n1 = bytesToWord32(s, 4)
        val n2 = bytesToWord32(s, 8)
        val n3 = bytesToWord32(s, 12)

        t = h0.toLong() + n0.toLong()
        h0 = t.toInt()
        t = h1.toLong() + (t shr 32) + n1.toLong()
        h1 = t.toInt()
        t = h2.toLong() + (t shr 32) + n2.toLong()
        h2 = t.toInt()
        t = h3.toLong() + (t shr 32) + n3.toLong()
        h3 = t.toInt()

        // Convert to byte array (little-endian)
        val mac = ByteArray(tagSize)
        word32ToBytes(h0, mac, 0)
        word32ToBytes(h1, mac, 4)
        word32ToBytes(h2, mac, 8)
        word32ToBytes(h3, mac, 12)

        return mac
    }

    /**
     * Clamps r as specified in RFC 8439.
     * r[3] &= 15; r[7] &= 15; r[11] &= 15; r[15] &= 15;
     * r[4] &= 252; r[8] &= 252; r[12] &= 252;
     */
    private fun clampR(r: ByteArray): ByteArray {
        val clamped = r.copyOf()
        clamped[3] = (clamped[3].toInt() and 15).toByte()
        clamped[7] = (clamped[7].toInt() and 15).toByte()
        clamped[11] = (clamped[11].toInt() and 15).toByte()
        clamped[15] = (clamped[15].toInt() and 15).toByte()
        clamped[4] = (clamped[4].toInt() and 252).toByte()
        clamped[8] = (clamped[8].toInt() and 252).toByte()
        clamped[12] = (clamped[12].toInt() and 252).toByte()
        return clamped
    }

    /**
     * Converts 4 bytes (little-endian) to a 32-bit word.
     */
    private fun bytesToWord32(bytes: ByteArray, offset: Int): Int {
        return (
            (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
            )
    }

    /**
     * Converts a 32-bit word to 4 bytes (little-endian).
     */
    private fun word32ToBytes(value: Int, bytes: ByteArray, offset: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    /**
     * Constant-time carry computation.
     * Returns 1 if there's a carry when adding a + b, 0 otherwise.
     * Based on: ((a ^ ((a ^ b) | ((a - b) ^ b))) >> 31)
     */
    private fun constantTimeCarry(a: Int, b: Int): Int {
        val aLong = a.toLong() and 0xFFFFFFFFL
        val bLong = b.toLong() and 0xFFFFFFFFL
        val diff = (aLong - bLong) and 0xFFFFFFFFL
        val result = (aLong xor ((aLong xor bLong) or (diff xor bLong)))
        return ((result ushr 31) and 1L).toInt()
    }
}
