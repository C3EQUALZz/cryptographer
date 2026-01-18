package com.example.cryptographer.domain.text.entities.aes

import com.example.cryptographer.domain.text.valueobjects.aes.AesBlock
import com.example.cryptographer.domain.text.valueobjects.aes.AesNumRounds
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * AES-GCM (Galois/Counter Mode) implementation.
 *
 * GCM is an authenticated encryption mode that provides both confidentiality and authenticity.
 * It uses Counter mode for encryption and GHASH for authentication.
 *
 * Follows NIST SP 800-38D specification.
 *
 * This is domain logic representing the AES-GCM encryption mode.
 */
internal object AesGcmMode {
    private val logger = KotlinLogging.logger {}
    private const val BLOCK_SIZE = 16 // bytes (128 bits)
    private const val GCM_TAG_LENGTH = 16 // bytes (128 bits)
    private const val GCM_IV_LENGTH = 12 // bytes (96 bits)

    /**
     * Encrypts data and generates authentication tag using AES-GCM.
     *
     * Uses domain entities (AesRoundKeys, AesNumRounds, AesBlock) for type safety and validation.
     *
     * @param plaintext Data to encrypt
     * @param iv Initialization vector (12 bytes)
     * @param aad Additional authenticated data (optional)
     * @param roundKeys Round keys entity
     * @param numRounds Number of rounds value object
     * @return Pair of (ciphertext, authentication tag)
     */
    fun encrypt(
        plaintext: ByteArray,
        iv: ByteArray,
        aad: ByteArray,
        roundKeys: AesRoundKeys,
        numRounds: AesNumRounds,
    ): Pair<ByteArray, ByteArray> {
        logger.debug {
            "Starting AES-GCM encryption: " +
                "plaintextSize=${plaintext.size} bytes, " +
                "ivSize=${iv.size} bytes, " +
                "aadSize=${aad.size} bytes"
        }

        require(iv.size == GCM_IV_LENGTH) { "IV must be $GCM_IV_LENGTH bytes" }
        require(roundKeys.numRounds == numRounds.rounds) {
            "Round keys rounds (${roundKeys.numRounds}) must match numRounds (${numRounds.rounds})"
        }

        // Generate H (hash subkey) by encrypting zero block
        logger.trace { "Generating H (hash subkey) by encrypting zero block" }
        val zeroBlock = AesBlock.Companion.createZero()
        val hBlock = AesCore.encryptBlock(zeroBlock, roundKeys, numRounds)
        logger.trace { "H (hash subkey) generated successfully" }

        // Initialize counter with IV || 0x00000001
        logger.trace { "Initializing counter J0: IV || 0x00000001" }
        val j0Bytes = ByteArray(BLOCK_SIZE)
        System.arraycopy(iv, 0, j0Bytes, 0, GCM_IV_LENGTH)
        j0Bytes[BLOCK_SIZE - 1] = 0x01
        val j0 = AesBlock.Companion.create(j0Bytes).getOrThrow()
        logger.trace { "Counter J0 initialized: ${j0Bytes.size} bytes" }

        // Encrypt plaintext in counter mode
        logger.debug { "Encrypting plaintext in counter mode (CTR): plaintextSize=${plaintext.size} bytes" }
        val ciphertext = encryptCounterMode(plaintext, j0, roundKeys, numRounds)
        logger.debug { "Counter mode encryption completed: ciphertextSize=${ciphertext.size} bytes" }

        // Generate authentication tag
        logger.debug {
            "Generating authentication tag using GHASH: " +
                "ciphertextSize=${ciphertext.size} bytes, " +
                "aadSize=${aad.size} bytes"
        }
        val tag = generateTag(aad, ciphertext, j0, hBlock, roundKeys, numRounds)
        logger.debug { "Authentication tag generated: tagSize=${tag.size} bytes" }

        logger.debug {
            "AES-GCM encryption completed: " +
                "ciphertextSize=${ciphertext.size} bytes, " +
                "tagSize=${tag.size} bytes"
        }
        return Pair(ciphertext, tag)
    }

    /**
     * Decrypts data and verifies authentication tag using AES-GCM.
     *
     * Uses domain entities (AesRoundKeys, AesNumRounds, AesBlock) for type safety and validation.
     *
     * @param ciphertext Encrypted data
     * @param tag Authentication tag
     * @param iv Initialization vector (12 bytes)
     * @param aad Additional authenticated data (optional)
     * @param roundKeys Round keys entity
     * @param numRounds Number of rounds value object
     * @return Decrypted plaintext, or null if authentication fails
     */
    fun decrypt(
        ciphertext: ByteArray,
        tag: ByteArray,
        iv: ByteArray,
        aad: ByteArray,
        roundKeys: AesRoundKeys,
        numRounds: AesNumRounds,
    ): ByteArray? {
        logger.debug {
            "Starting AES-GCM decryption: " +
                "ciphertextSize=${ciphertext.size} bytes, " +
                "tagSize=${tag.size} bytes, " +
                "ivSize=${iv.size} bytes, " +
                "aadSize=${aad.size} bytes"
        }

        require(iv.size == GCM_IV_LENGTH) { "IV must be $GCM_IV_LENGTH bytes" }
        require(tag.size == GCM_TAG_LENGTH) { "Tag must be $GCM_TAG_LENGTH bytes" }
        require(roundKeys.numRounds == numRounds.rounds) {
            "Round keys rounds (${roundKeys.numRounds}) must match numRounds (${numRounds.rounds})"
        }

        // Generate H (hash subkey) by encrypting zero block
        logger.trace { "Generating H (hash subkey) by encrypting zero block" }
        val zeroBlock = AesBlock.Companion.createZero()
        val hBlock = AesCore.encryptBlock(zeroBlock, roundKeys, numRounds)
        logger.trace { "H (hash subkey) generated successfully" }

        // Initialize counter with IV || 0x00000001
        logger.trace { "Initializing counter J0: IV || 0x00000001" }
        val j0Bytes = ByteArray(BLOCK_SIZE)
        System.arraycopy(iv, 0, j0Bytes, 0, GCM_IV_LENGTH)
        j0Bytes[BLOCK_SIZE - 1] = 0x01
        val j0 = AesBlock.Companion.create(j0Bytes).getOrThrow()
        logger.trace { "Counter J0 initialized: ${j0Bytes.size} bytes" }

        // Verify authentication tag
        logger.debug {
            "Verifying authentication tag using GHASH: " +
                "ciphertextSize=${ciphertext.size} bytes, " +
                "aadSize=${aad.size} bytes"
        }

        val expectedTag = generateTag(aad, ciphertext, j0, hBlock, roundKeys, numRounds)

        if (!constantTimeEquals(tag, expectedTag)) {
            logger.warn {
                "AES-GCM decryption failed: " +
                    "authentication tag verification failed - data may have been tampered with"
            }
            return null // Authentication failed
        }
        logger.trace { "Authentication tag verification passed" }

        // Decrypt ciphertext in counter mode
        logger.debug { "Decrypting ciphertext in counter mode (CTR): ciphertextSize=${ciphertext.size} bytes" }
        val decryptedData = encryptCounterMode(ciphertext, j0, roundKeys, numRounds)
        logger.debug { "Counter mode decryption completed: plaintextSize=${decryptedData.size} bytes" }

        logger.debug { "AES-GCM decryption completed successfully: plaintextSize=${decryptedData.size} bytes" }
        return decryptedData
    }

    /**
     * Encrypts/decrypts data in counter mode.
     * Counter mode uses the same operation for encryption and decryption.
     *
     * Uses domain entities for block encryption.
     */
    private fun encryptCounterMode(
        data: ByteArray,
        initialCounter: AesBlock,
        roundKeys: AesRoundKeys,
        numRounds: AesNumRounds,
    ): ByteArray {
        logger.trace { "Starting counter mode: dataSize=${data.size} bytes, blockSize=$BLOCK_SIZE bytes" }
        val result = ByteArray(data.size)
        val counterBytes = initialCounter.bytes.copyOf()
        val totalBlocks = (data.size + BLOCK_SIZE - 1) / BLOCK_SIZE
        logger.trace { "Processing $totalBlocks blocks in counter mode" }

        // Process data in 16-byte blocks
        for (i in data.indices step BLOCK_SIZE) {
            // Encrypt counter to generate keystream
            val counterBlock = AesBlock.Companion.create(counterBytes).getOrThrow()
            val keystreamBlock = AesCore.encryptBlock(counterBlock, roundKeys, numRounds)
            val keystream = keystreamBlock.bytes

            // XOR keystream with data
            val blockSize = minOf(BLOCK_SIZE, data.size - i)
            for (j in 0 until blockSize) {
                result[i + j] = (data[i + j].toInt() xor keystream[j].toInt()).toByte()
            }

            // Increment counter
            incrementCounter(counterBytes)
        }

        logger.trace { "Counter mode completed: processed $totalBlocks blocks, resultSize=${result.size} bytes" }
        return result
    }

    /**
     * Generates authentication tag using GHASH.
     */
    private fun generateTag(
        aad: ByteArray,
        ciphertext: ByteArray,
        j0: AesBlock,
        h: AesBlock,
        roundKeys: AesRoundKeys,
        numRounds: AesNumRounds,
    ): ByteArray {
        // Construct S = AAD || C || len(AAD) || len(C)
        val s = ByteArray(
            padToBlockMultiple(aad.size) +
                padToBlockMultiple(ciphertext.size) + BLOCK_SIZE,
        )
        var offset = 0

        // Append AAD (padded to block multiple)
        System.arraycopy(aad, 0, s, offset, aad.size)
        offset += padToBlockMultiple(aad.size)

        // Append ciphertext (padded to block multiple)
        System.arraycopy(ciphertext, 0, s, offset, ciphertext.size)
        offset += padToBlockMultiple(ciphertext.size)

        // Append lengths (64 bits each, big-endian)
        appendLength(aad.size.toLong() * 8, s, offset)
        offset += 8
        appendLength(ciphertext.size.toLong() * 8, s, offset)

        // Compute GHASH
        val ghashResult = ghash(s, h.bytes)

        // Compute tag = GHASH ^ E_k(J0)
        val encryptedJ0 = AesCore.encryptBlock(j0, roundKeys, numRounds)
        val tagBytes = ByteArray(GCM_TAG_LENGTH)
        for (i in 0 until GCM_TAG_LENGTH) {
            tagBytes[i] = (ghashResult[i].toInt() xor encryptedJ0.bytes[i].toInt()).toByte()
        }

        return tagBytes
    }

    /**
     * GHASH function - universal hashing over GF(2^128).
     *
     * GHASH computes: Y_0 = 0, Y_i = (Y_{i-1} XOR X_i) * H
     */
    private fun ghash(data: ByteArray, h: ByteArray): ByteArray {
        require(data.size % BLOCK_SIZE == 0) { "Data must be multiple of block size" }
        require(h.size == BLOCK_SIZE) { "H must be block size" }

        var y = ByteArray(BLOCK_SIZE) // Accumulator (initialized to zero)

        // Process each block
        for (i in data.indices step BLOCK_SIZE) {
            val block = data.sliceArray(i until (i + BLOCK_SIZE))

            // XOR block with accumulator: Y_i = Y_{i-1} XOR X_i
            for (j in 0 until BLOCK_SIZE) {
                y[j] = (y[j].toInt() xor block[j].toInt()).toByte()
            }

            // Multiply by H in GF(2^128): Y_i = Y_i * H
            y = gf128Mul(y, h)
        }

        return y
    }

    /**
     * Multiplication in GF(2^128) modulo irreducible polynomial.
     * Irreducible polynomial: x^128 + x^7 + x^2 + x + 1
     *
     * Uses right-to-left binary method for efficiency.
     * Processes bits of X from MSB to LSB, multiplying V by x each iteration.
     */
    private fun gf128Mul(x: ByteArray, y: ByteArray): ByteArray {
        require(x.size == BLOCK_SIZE && y.size == BLOCK_SIZE) { "Both operands must be block size" }

        val result = ByteArray(BLOCK_SIZE)
        val v = y.copyOf()

        // Process each bit of x from most significant to least significant
        for (i in 0 until BLOCK_SIZE) {
            val xByte = x[i].toInt() and 0xFF
            for (j in 0 until 8) {
                val bit = (xByte ushr (7 - j)) and 0x01
                if (bit != 0) {
                    // XOR result with v if bit is set
                    for (k in 0 until BLOCK_SIZE) {
                        result[k] = (result[k].toInt() xor v[k].toInt()).toByte()
                    }
                }
                // Multiply v by x (left shift, apply reduction if needed)
                val carry = (v[BLOCK_SIZE - 1].toInt() and 0x01) != 0
                // Right shift v (divide by x in polynomial representation)
                for (k in BLOCK_SIZE - 1 downTo 1) {
                    v[k] = ((v[k].toInt() ushr 1) or ((v[k - 1].toInt() and 0x01) shl 7)).toByte()
                }
                v[0] = (v[0].toInt() ushr 1).toByte()
                if (carry) {
                    // Apply reduction: R = x^128 + x^7 + x^2 + x + 1
                    // Reduction polynomial: 0xE1000000000000000000000000000000 (but only low byte matters)
                    v[BLOCK_SIZE - 1] = (v[BLOCK_SIZE - 1].toInt() xor 0xE1).toByte()
                }
            }
        }

        return result
    }

    /**
     * Increments counter (treats last 32 bits as big-endian integer).
     */
    private fun incrementCounter(counter: ByteArray) {
        require(counter.size == BLOCK_SIZE) { "Counter must be block size" }

        var carry = 1
        for (i in BLOCK_SIZE - 1 downTo 0) {
            val sum = (counter[i].toInt() and 0xFF) + carry
            counter[i] = (sum and 0xFF).toByte()
            carry = sum ushr 8
            if (carry == 0) break
        }
    }

    /**
     * Pads length to next multiple of block size.
     */
    private fun padToBlockMultiple(length: Int): Int {
        return ((length + BLOCK_SIZE - 1) / BLOCK_SIZE) * BLOCK_SIZE
    }

    /**
     * Appends 64-bit length (in bits) as big-endian bytes.
     */
    private fun appendLength(length: Long, buffer: ByteArray, offset: Int) {
        for (i in 0 until 8) {
            buffer[offset + i] = ((length ushr (56 - i * 8)) and 0xFF).toByte()
        }
    }

    /**
     * Constant-time comparison of two byte arrays.
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
