package com.example.cryptographer.domain.common.errors

import java.time.Instant

/**
 * Error thrown when timestamps are inconsistent.
 *
 * For example, when updated_at is earlier than created_at.
 */
class InconsistentTimeError(
    message: String,
    cause: Throwable? = null
) : DomainError(message, cause) {
    companion object {
        fun create(
            updatedAt: Instant,
            createdAt: Instant
        ): InconsistentTimeError {
            val message = buildString {
                append("$updatedAt cannot be earlier than $createdAt")
            }
            return InconsistentTimeError(message)
        }
    }
}
