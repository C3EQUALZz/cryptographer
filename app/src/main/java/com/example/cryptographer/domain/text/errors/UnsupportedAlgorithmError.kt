package com.example.cryptographer.domain.text.errors

import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.common.errors.DomainError

/**
 * Domain error indicating that an encryption algorithm is not supported.
 *
 * This error should be thrown when an algorithm is not supported by a specific encryption service.
 */
class UnsupportedAlgorithmError(
    val algorithm: EncryptionAlgorithm,
    val serviceName: String,
) : DomainError("Algorithm $algorithm is not supported by $serviceName")
