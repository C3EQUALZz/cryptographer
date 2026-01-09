package com.example.cryptographer.domain.common.services

import com.example.cryptographer.domain.common.events.BaseDomainEvent
import java.util.ArrayDeque

/**
 * Base class for domain services.
 *
 * Domain services contain business logic that doesn't naturally fit
 * within a single entity or value object. They can also coordinate
 * operations between multiple entities.
 *
 * This base class provides event recording capabilities for domain services.
 *
 * Note:
 * - Domain services are stateless
 * - They operate on domain entities and value objects
 * - They can raise domain events
 */
abstract class DomainService {
    private val _events: ArrayDeque<BaseDomainEvent> = ArrayDeque()

    /**
     * Records a domain event.
     */
    protected fun recordEvent(event: BaseDomainEvent) {
        _events.add(event)
    }

    /**
     * Records multiple domain events.
     */
    protected fun recordEvents(events: Collection<BaseDomainEvent>) {
        _events.addAll(events)
    }

    /**
     * Gets all recorded domain events without clearing them.
     */
    fun getEvents(): List<BaseDomainEvent> {
        return _events.toList()
    }

    /**
     * Clears all recorded domain events.
     */
    fun clearEvents() {
        _events.clear()
    }

    /**
     * Pulls all recorded domain events and clears them.
     * This is typically called after events have been processed.
     *
     * @return List of domain events that were recorded
     */
    fun pullEvents(): List<BaseDomainEvent> {
        val events = _events.toList()
        _events.clear()
        return events
    }

    /**
     * Checks if there are any pending domain events.
     */
    fun hasEvents(): Boolean {
        return _events.isNotEmpty()
    }
}
