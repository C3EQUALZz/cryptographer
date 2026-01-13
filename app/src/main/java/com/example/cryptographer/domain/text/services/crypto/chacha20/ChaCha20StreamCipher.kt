package com.example.cryptographer.domain.text.services.crypto.chacha20

/**
 * ChaCha20 stream cipher implementation.
 *
 * Generates a key stream by calling the ChaCha20 block function multiple times
 * and XORing it with the input data.
 *
 * Follows Single Responsibility Principle: responsible only for generating key stream
 * and applying it to data (encryption/decryption use the same operation).
 */
internal class ChaCha20StreamCipher(
    private val core: ChaCha20Core = ChaCha20Core,
) {
    private val blockSize = 64 // bytes

    /**
     * Encrypts or decrypts data using ChaCha20 stream cipher.
     *
     * Encryption and decryption are identical operations in stream ciphers:
     * output = input XOR keyStream
     *
     * @param input Input data to encrypt/decrypt
     * @param key 32-byte key
     * @param nonce 12-byte nonce
     * @param initialCounter Initial block counter (usually 0 or 1 for AEAD)
     * @return Encrypted/decrypted data
     */
    fun process(input: ByteArray, key: ByteArray, nonce: ByteArray, initialCounter: Int = 0): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes" }
        require(nonce.size == 12) { "Nonce must be 12 bytes" }

        val output = ByteArray(input.size)
        var inputOffset = 0
        var outputOffset = 0
        var counter = initialCounter

        // Process data in 64-byte blocks
        while (inputOffset < input.size) {
            val block = core.block(key, counter, nonce)
            val remaining = input.size - inputOffset
            val blockLength = minOf(blockSize, remaining)

            // XOR input with key stream
            for (i in 0 until blockLength) {
                output[outputOffset + i] = (input[inputOffset + i].toInt() xor block[i].toInt()).toByte()
            }

            inputOffset += blockLength
            outputOffset += blockLength
            counter++
        }

        return output
    }
}
