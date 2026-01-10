package com.example.cryptographer.domain.common.errors

import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm

/**
 * Domain error indicating that an encryption algorithm is not supported.
 *
 * This error should be thrown when an algorithm is not supported by a specific encryption service.
 */
class UnsupportedAlgorithmError(
    algorithm: EncryptionAlgorithm,
    serviceName: String
) : DomainError("Algorithm $algorithm is not supported by $serviceName") {
    val algorithm: EncryptionAlgorithm = algorithm
    val serviceName: String = serviceName
}

