package com.example.cryptographer.domain.common.entities

import com.example.cryptographer.domain.common.entities.BaseEntity
import java.time.Instant

/**
 * Base class for aggregate roots in Domain-Driven Design.
 *
 * An aggregate root is a special kind of entity that:
 * - Is the entry point for accessing the aggregate
 * - Maintains consistency boundaries
 * - Can raise domain events
 *
 * All aggregate roots should inherit from this class.
 *
 * Type Variables:
 * - OIDType: The type parameter for the aggregate root's identifier.
 */
abstract class BaseAggregateRoot<OIDType>(
    id: OIDType,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) : BaseEntity<OIDType>(id, createdAt, updatedAt)
