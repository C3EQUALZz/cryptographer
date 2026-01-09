package com.example.cryptographer.domain.common.events

import java.time.Instant
import java.util.UUID

/**
 * Base event, from which any domain event should be inherited.
 *
 * Events represent internal operations that have occurred in the domain.
 * They are used for:
 * - Event sourcing
 * - Integration with other bounded contexts
 * - Audit logging
 * - Triggering side effects
 *
 * All domain events should inherit from this class.
 *
 * Attributes:
 * - eventId: Unique identifier for the event
 * - eventTimestamp: Timestamp when the event occurred
 *
 * Note:
 * - Events are immutable
 * - Events should be serializable for persistence
 * - Events should contain all necessary data for event handlers
 */
abstract class BaseDomainEvent(
    val eventId: UUID = UUID.randomUUID(),
    val eventTimestamp: Instant = Instant.now()
) {
    /**
     * Creates a dictionary representation of the event.
     *
     * This can be used for serialization, logging, or debugging.
     *
     * @param exclude Set of field names to exclude from the dictionary
     * @param include Additional fields to include in the dictionary
     * @return Dictionary representation of the event
     */
    fun toDict(
        exclude: Set<String> = emptySet(),
        include: Map<String, Any> = emptyMap()
    ): Map<String, Any> {
        val data = mutableMapOf<String, Any>()

        // Add standard fields
        data["eventId"] = eventId.toString()
        data["eventTimestamp"] = eventTimestamp.toString()
        data["eventType"] = this::class.simpleName ?: "Unknown"

        // Exclude specified fields
        exclude.forEach { data.remove(it) }

        // Include additional fields
        data.putAll(include)

        return data.toMap()
    }

    /**
     * Gets the type name of the event.
     */
    fun getEventType(): String {
        return this::class.simpleName ?: "Unknown"
    }
}

