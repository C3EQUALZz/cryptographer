package com.example.cryptographer.infrastructure.errors

/**
 * Error thrown when data serialization or deserialization fails.
 *
 * This error is thrown when there are issues with converting data to/from
 * a serialized format (e.g., JSON, Base64, binary).
 *
 * @param operation The serialization operation that failed ("serialize" or "deserialize")
 * @param dataType The type of data being serialized/deserialized
 * @param details Additional details about the failure
 */
class SerializationError(
    val operation: String,
    val dataType: String,
    details: String? = null,
    cause: Throwable? = null,
) : InfrastructureError(
    message = "Serialization operation failed: " +
        "operation=$operation, " +
        "dataType=$dataType" +
        "${if (details != null) ", details=$details" else ""}",
    cause = cause,
)
