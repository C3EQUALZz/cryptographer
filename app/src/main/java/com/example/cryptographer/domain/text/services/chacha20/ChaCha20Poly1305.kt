package com.example.cryptographer.domain.text.services.chacha20

/**
 * ChaCha20-Poly1305 AEAD (Authenticated Encryption with Associated Data) implementation.
 * Combines ChaCha20 stream cipher with Poly1305 MAC for authenticated encryption.
 *
 * This class follows Single Responsibility Principle - it coordinates
 * ChaCha20 encryption and Poly1305 authentication.
 *
 * Implementation follows RFC 8439 specification.
 */
internal class ChaCha20Poly1305(
    private val key: ByteArray,
    private val nonce: ByteArray,
) {
    companion object {
        private const val KEY_SIZE = 32 // bytes (256 bits)
        private const val NONCE_SIZE = 12 // bytes (96 bits)
        private const val TAG_SIZE = 16 // bytes (128 bits)
    }

    init {
        require(key.size == KEY_SIZE) { "Key must be exactly $KEY_SIZE bytes" }
        require(nonce.size == NONCE_SIZE) { "Nonce must be exactly $NONCE_SIZE bytes" }
    }

    /**
     * Derives Poly1305 key from ChaCha20 keystream.
     * Uses the first 32 bytes of the first block of ChaCha20 keystream as the Poly1305 key.
     * According to RFC 8439, the Poly1305 key is the first 32 bytes of the first keystream block.
     *
     * @return 32-byte Poly1305 key
     */
    private fun derivePoly1305Key(): ByteArray {
        // Use counter 0 to generate the first block (64 bytes)
        val initialState = ChaCha20Core.createInitialState(key, nonce, 0)
        val keystreamBlock = ChaCha20Core.block(initialState)
        // Poly1305 key is the first 32 bytes of the keystream block
        return keystreamBlock.sliceArray(0 until 32)
    }

    /**
     * Encrypts plaintext and computes authentication tag.
     *
     * @param plaintext Data to encrypt
     * @param associatedData Additional authenticated data (AAD)
     * @return Pair of (ciphertext, authentication tag)
     */
    fun encrypt(plaintext: ByteArray, associatedData: ByteArray = ByteArray(0)): Pair<ByteArray, ByteArray> {
        // Derive Poly1305 key from first block (counter = 0)
        val poly1305Key = derivePoly1305Key()

        // Encrypt plaintext using ChaCha20 (starting from block 1, counter = 1)
        val ciphertext = encryptWithCounter(plaintext, startCounter = 1)

        // Compute Poly1305 tag
        val tag = computeTag(ciphertext, associatedData, poly1305Key)

        return Pair(ciphertext, tag)
    }

    /**
     * Decrypts ciphertext and verifies authentication tag.
     *
     * @param ciphertext Encrypted data
     * @param tag Authentication tag
     * @param associatedData Additional authenticated data (AAD)
     * @return Decrypted plaintext or null if verification fails
     */
    fun decrypt(ciphertext: ByteArray, tag: ByteArray, associatedData: ByteArray = ByteArray(0)): ByteArray? {
        require(tag.size == TAG_SIZE) { "Tag must be exactly $TAG_SIZE bytes" }

        // Derive Poly1305 key
        val poly1305Key = derivePoly1305Key()

        // Verify tag
        val computedTag = computeTag(ciphertext, associatedData, poly1305Key)
        if (!tag.contentEquals(computedTag)) {
            return null // Authentication failed
        }

        // Decrypt using ChaCha20 (starting from block 1, counter = 1)
        return encryptWithCounter(ciphertext, startCounter = 1)
    }

    /**
     * Encrypts/decrypts data using ChaCha20 with specified starting counter.
     * Encryption and decryption are the same operation (XOR with keystream).
     *
     * @param data Data to process
     * @param startCounter Starting block counter
     * @return Processed data
     */
    private fun encryptWithCounter(data: ByteArray, startCounter: Int): ByteArray {
        if (data.isEmpty()) return ByteArray(0)

        val output = ByteArray(data.size)
        var offset = 0
        var blockCounter = startCounter

        while (offset < data.size) {
            val block = ChaCha20Core.createInitialState(key, nonce, blockCounter)
            val keystreamBlock = ChaCha20Core.block(block)

            val remaining = data.size - offset
            val toProcess = minOf(64, remaining)

            for (i in 0 until toProcess) {
                output[offset + i] = (data[offset + i].toInt() xor keystreamBlock[i].toInt()).toByte()
            }

            offset += toProcess
            blockCounter++
        }

        return output
    }

    /**
     * Computes Poly1305 authentication tag.
     * Formats the input according to RFC 8439: pad(AAD) || pad(ciphertext) || len(AAD) || len(ciphertext)
     *
     * @param ciphertext Encrypted data
     * @param associatedData Additional authenticated data
     * @param poly1305Key Poly1305 key
     * @return 16-byte authentication tag
     */
    private fun computeTag(ciphertext: ByteArray, associatedData: ByteArray, poly1305Key: ByteArray): ByteArray {
        // Build message for Poly1305: pad(AAD) || pad(ciphertext) || len(AAD) || len(ciphertext)
        val aadPadded = padTo16Bytes(associatedData)
        val ciphertextPadded = padTo16Bytes(ciphertext)

        val lenAad = associatedData.size.toLong()
        val lenCiphertext = ciphertext.size.toLong()

        val lenBytes = ByteArray(16)
        writeLittleEndian64(lenAad, lenBytes, 0)
        writeLittleEndian64(lenCiphertext, lenBytes, 8)

        val message = aadPadded + ciphertextPadded + lenBytes

        return Poly1305.compute(message, poly1305Key)
    }

    /**
     * Pads data to multiple of 16 bytes.
     */
    private fun padTo16Bytes(data: ByteArray): ByteArray {
        val remainder = data.size % 16
        if (remainder == 0) {
            return data
        }
        val padded = ByteArray(data.size + (16 - remainder))
        data.copyInto(padded, 0, 0, data.size)
        return padded
    }

    /**
     * Writes 64-bit value in little-endian format.
     */
    private fun writeLittleEndian64(value: Long, bytes: ByteArray, offset: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value ushr 24) and 0xFF).toByte()
        bytes[offset + 4] = ((value ushr 32) and 0xFF).toByte()
        bytes[offset + 5] = ((value ushr 40) and 0xFF).toByte()
        bytes[offset + 6] = ((value ushr 48) and 0xFF).toByte()
        bytes[offset + 7] = ((value ushr 56) and 0xFF).toByte()
    }
}
