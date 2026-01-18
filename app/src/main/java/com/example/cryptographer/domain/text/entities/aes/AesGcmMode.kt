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
    private const val BITS_IN_BYTE = 8
    private const val BYTE_MASK = 0xFF

    internal class EncryptParams(
        val plaintext: ByteArray,
        val iv: ByteArray,
        val aad: ByteArray,
        val roundKeys: AesRoundKeys,
        val numRounds: AesNumRounds,
    )

    internal class DecryptParams(
        val ciphertext: ByteArray,
        val tag: ByteArray,
        val iv: ByteArray,
        val aad: ByteArray,
        val roundKeys: AesRoundKeys,
        val numRounds: AesNumRounds,
    )

    /**
     * Encrypts data and generates authentication tag using AES-GCM.
     *
     * Uses domain entities (AesRoundKeys, AesNumRounds, AesBlock) for type safety and validation.
     *
     * @param params Encrypt parameters (plaintext, iv, aad, round keys, rounds)
     * @return Pair of (ciphertext, authentication tag)
     */
    fun encrypt(params: EncryptParams): Pair<ByteArray, ByteArray> {
        logger.debug {
            "Starting AES-GCM encryption: " +
                "plaintextSize=${params.plaintext.size} bytes, " +
                "ivSize=${params.iv.size} bytes, " +
                "aadSize=${params.aad.size} bytes"
        }

        require(params.iv.size == GCM_IV_LENGTH) { "IV must be $GCM_IV_LENGTH bytes" }
        require(params.roundKeys.numRounds == params.numRounds.rounds) {
            "Round keys rounds (${params.roundKeys.numRounds}) must match numRounds (${params.numRounds.rounds})"
        }

        // Generate H (hash subkey) by encrypting zero block
        logger.trace { "Generating H (hash subkey) by encrypting zero block" }
        val zeroBlock = AesBlock.createZero()
        val hBlock = AesCore.encryptBlock(zeroBlock, params.roundKeys, params.numRounds)
        logger.trace { "H (hash subkey) generated successfully" }

        // Initialize counter with IV || 0x00000001
        logger.trace { "Initializing counter J0: IV || 0x00000001" }
        val j0Bytes = ByteArray(BLOCK_SIZE)
        System.arraycopy(params.iv, 0, j0Bytes, 0, GCM_IV_LENGTH)
        j0Bytes[BLOCK_SIZE - 1] = 0x01
        val j0 = AesBlock.create(j0Bytes).getOrThrow()
        logger.trace { "Counter J0 initialized: ${j0Bytes.size} bytes" }

        // Encrypt plaintext in counter mode
        logger.debug {
            "Encrypting plaintext in counter mode (CTR): plaintextSize=${params.plaintext.size} bytes"
        }
        val ciphertext = encryptCounterMode(params.plaintext, j0, params.roundKeys, params.numRounds)
        logger.debug { "Counter mode encryption completed: ciphertextSize=${ciphertext.size} bytes" }

        // Generate authentication tag
        logger.debug {
            "Generating authentication tag using GHASH: " +
                "ciphertextSize=${ciphertext.size} bytes, " +
                "aadSize=${params.aad.size} bytes"
        }
        val tagContext = AesGcmAuthenticator.TagContext(
            j0 = j0,
            h = hBlock,
            roundKeys = params.roundKeys,
            numRounds = params.numRounds,
        )
        val tag = AesGcmAuthenticator.generateTag(params.aad, ciphertext, tagContext)
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
     * @param params Decrypt parameters (ciphertext, tag, iv, aad, round keys, rounds)
     * @return Decrypted plaintext, or null if authentication fails
     */
    fun decrypt(params: DecryptParams): ByteArray? {
        logger.debug {
            "Starting AES-GCM decryption: " +
                "ciphertextSize=${params.ciphertext.size} bytes, " +
                "tagSize=${params.tag.size} bytes, " +
                "ivSize=${params.iv.size} bytes, " +
                "aadSize=${params.aad.size} bytes"
        }

        require(params.iv.size == GCM_IV_LENGTH) { "IV must be $GCM_IV_LENGTH bytes" }
        require(params.tag.size == GCM_TAG_LENGTH) { "Tag must be $GCM_TAG_LENGTH bytes" }
        require(params.roundKeys.numRounds == params.numRounds.rounds) {
            "Round keys rounds (${params.roundKeys.numRounds}) must match numRounds (${params.numRounds.rounds})"
        }

        // Generate H (hash subkey) by encrypting zero block
        logger.trace { "Generating H (hash subkey) by encrypting zero block" }
        val zeroBlock = AesBlock.createZero()
        val hBlock = AesCore.encryptBlock(zeroBlock, params.roundKeys, params.numRounds)
        logger.trace { "H (hash subkey) generated successfully" }

        // Initialize counter with IV || 0x00000001
        logger.trace { "Initializing counter J0: IV || 0x00000001" }
        val j0Bytes = ByteArray(BLOCK_SIZE)
        System.arraycopy(params.iv, 0, j0Bytes, 0, GCM_IV_LENGTH)
        j0Bytes[BLOCK_SIZE - 1] = 0x01
        val j0 = AesBlock.create(j0Bytes).getOrThrow()
        logger.trace { "Counter J0 initialized: ${j0Bytes.size} bytes" }

        // Verify authentication tag
        logger.debug {
            "Verifying authentication tag using GHASH: " +
                "ciphertextSize=${params.ciphertext.size} bytes, " +
                "aadSize=${params.aad.size} bytes"
        }

        val tagContext = AesGcmAuthenticator.TagContext(
            j0 = j0,
            h = hBlock,
            roundKeys = params.roundKeys,
            numRounds = params.numRounds,
        )
        val expectedTag = AesGcmAuthenticator.generateTag(params.aad, params.ciphertext, tagContext)

        if (!constantTimeEquals(params.tag, expectedTag)) {
            logger.warn {
                "AES-GCM decryption failed: " +
                    "authentication tag verification failed - data may have been tampered with"
            }
            return null // Authentication failed
        }
        logger.trace { "Authentication tag verification passed" }

        // Decrypt ciphertext in counter mode
        logger.debug {
            "Decrypting ciphertext in counter mode (CTR): ciphertextSize=${params.ciphertext.size} bytes"
        }
        val decryptedData = encryptCounterMode(
            params.ciphertext,
            j0,
            params.roundKeys,
            params.numRounds,
        )
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
            val counterBlock = AesBlock.create(counterBytes).getOrThrow()
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
     * Increments counter (treats last 32 bits as big-endian integer).
     */
    private fun incrementCounter(counter: ByteArray) {
        require(counter.size == BLOCK_SIZE) { "Counter must be block size" }

        var carry = 1
        for (i in BLOCK_SIZE - 1 downTo 0) {
            val sum = (counter[i].toInt() and BYTE_MASK) + carry
            counter[i] = (sum and BYTE_MASK).toByte()
            carry = sum ushr BITS_IN_BYTE
            if (carry == 0) break
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
