package com.example.cryptographer.domain.text.services.crypto.chacha20poly1305

import com.example.cryptographer.domain.text.services.crypto.chacha20.ChaCha20Core
import com.example.cryptographer.domain.text.services.crypto.chacha20.ChaCha20StreamCipher
import com.example.cryptographer.domain.text.services.crypto.poly1305.Poly1305Mac

/**
 * ChaCha20-Poly1305 AEAD (Authenticated Encryption with Associated Data) implementation.
 *
 * Combines ChaCha20 stream cipher for encryption and Poly1305 for authentication.
 * Follows RFC 8439 specification.
 *
 * This class follows the Composition pattern and Dependency Inversion Principle:
 * - Depends on abstractions (could use interfaces if needed)
 * - Composes ChaCha20StreamCipher and Poly1305Mac
 * - Single Responsibility: coordinates encryption/decryption with authentication
 */
internal class ChaCha20Poly1305Aead(
    private val core: ChaCha20Core = ChaCha20Core,
    private val streamCipher: ChaCha20StreamCipher = ChaCha20StreamCipher(),
    private val mac: Poly1305Mac = Poly1305Mac(),
) {
    private val keyLength = 32 // bytes (256 bits)
    private val nonceLength = 12 // bytes (96 bits)
    private val tagLength = 16 // bytes (128 bits)

    /**
     * Encrypts data and generates an authentication tag.
     *
     * According to RFC 8439, the process is:
     * 1. Generate Poly1305 one-time key using ChaCha20 with counter=0
     * 2. Encrypt plaintext using ChaCha20 with counter=1
     * 3. Generate authentication tag over ciphertext and AAD
     *
     * @param plaintext Data to encrypt
     * @param key 32-byte encryption key
     * @param nonce 12-byte nonce
     * @param aad Associated Authenticated Data (optional, can be empty)
     * @return Pair of (ciphertext, authentication tag)
     */
    fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray = ByteArray(0),
    ): Pair<ByteArray, ByteArray> {
        require(key.size == keyLength) { "Key must be $keyLength bytes" }
        require(nonce.size == nonceLength) { "Nonce must be $nonceLength bytes" }

        // Step 1: Generate Poly1305 one-time key using ChaCha20 with counter=0
        val polyKey = generatePoly1305Key(key, nonce)

        // Step 2: Encrypt plaintext using ChaCha20 with counter=1
        val ciphertext = streamCipher.process(plaintext, key, nonce, initialCounter = 1)

        // Step 3: Generate authentication tag
        // Poly1305 authenticates: AAD || ciphertext || AAD length (8 bytes) || ciphertext length (8 bytes)
        val macData = constructMacData(aad, ciphertext)
        val tag = mac.generateTag(macData, polyKey)

        return Pair(ciphertext, tag)
    }

    /**
     * Decrypts data and verifies the authentication tag.
     *
     * @param ciphertext Encrypted data
     * @param tag Authentication tag
     * @param key 32-byte encryption key
     * @param nonce 12-byte nonce
     * @param aad Associated Authenticated Data (optional, can be empty)
     * @return Decrypted plaintext, or null if authentication fails
     */
    fun decrypt(
        ciphertext: ByteArray,
        tag: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray = ByteArray(0),
    ): ByteArray? {
        require(key.size == keyLength) { "Key must be $keyLength bytes" }
        require(nonce.size == nonceLength) { "Nonce must be $nonceLength bytes" }
        require(tag.size == tagLength) { "Tag must be $tagLength bytes" }

        // Step 1: Generate Poly1305 one-time key using ChaCha20 with counter=0
        val polyKey = generatePoly1305Key(key, nonce)

        // Step 2: Verify authentication tag
        val macData = constructMacData(aad, ciphertext)
        val expectedTag = mac.generateTag(macData, polyKey)

        if (!constantTimeEquals(tag, expectedTag)) {
            return null // Authentication failed
        }

        // Step 3: Decrypt ciphertext using ChaCha20 with counter=1
        val plaintext = streamCipher.process(ciphertext, key, nonce, initialCounter = 1)

        return plaintext
    }

    /**
     * Generates the Poly1305 one-time key using ChaCha20.
     *
     * Uses ChaCha20 block function with counter=0 to generate 32 bytes (256 bits) for the Poly1305 key.
     * According to RFC 8439, this is the first 32 bytes of the ChaCha20 block output.
     */
    private fun generatePoly1305Key(key: ByteArray, nonce: ByteArray): ByteArray {
        // Generate 64-byte block using ChaCha20 with counter=0
        // The Poly1305 key is the first 32 bytes of the block output
        val block = core.block(key, counter = 0, nonce)
        return block.take(32).toByteArray()
    }

    /**
     * Constructs the data to be authenticated by Poly1305.
     *
     * Format: AAD || ciphertext || len(AAD) (8 bytes, little-endian) || len(ciphertext) (8 bytes, little-endian)
     */
    private fun constructMacData(aad: ByteArray, ciphertext: ByteArray): ByteArray {
        val aadLength = aad.size.toLong()
        val ciphertextLength = ciphertext.size.toLong()

        val macData = ByteArray(aad.size + ciphertext.size + 16)
        var offset = 0

        // Append AAD
        System.arraycopy(aad, 0, macData, offset, aad.size)
        offset += aad.size

        // Append ciphertext
        System.arraycopy(ciphertext, 0, macData, offset, ciphertext.size)
        offset += ciphertext.size

        // Append AAD length (8 bytes, little-endian)
        for (i in 0 until 8) {
            macData[offset + i] = ((aadLength shr (i * 8)) and 0xFF).toByte()
        }
        offset += 8

        // Append ciphertext length (8 bytes, little-endian)
        for (i in 0 until 8) {
            macData[offset + i] = ((ciphertextLength shr (i * 8)) and 0xFF).toByte()
        }

        return macData
    }

    /**
     * Constant-time comparison of two byte arrays.
     *
     * Prevents timing attacks during tag verification.
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
