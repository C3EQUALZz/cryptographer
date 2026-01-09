package com.example.cryptographer.domain.common.entities

import com.example.cryptographer.domain.common.errors.InconsistentTimeError
import java.time.Instant
import java.util.Objects

/**
 * Abstract base class for all domain entities.
 *
 * What is domain entity:
 * - https://wempe.dev/blog/domain-driven-design-entities-value-objects
 * - https://medium.com/@michaelmaurice410/what-is-an-entity-unveiling-the-core-of-domain-driven-design-and-clean-architecture-84b492c4398d
 * - https://habr.com/ru/articles/787460/
 * - https://ru.wikipedia.org/wiki/Предметно-ориентированное_проектирование#Сущность
 *
 * Provides the fundamental structure for domain entities with generic
 * identifier support. All concrete domain entities should inherit from
 * this class.
 *
 * Attributes:
 * - id: The unique identifier of the entity. Type is parameterized
 *   to support different identifier types (String, UUID, Int, etc.) for
 *   different entities.
 * - created_at: Timestamp when the entity was created
 * - updated_at: Timestamp when the entity was last updated
 *
 * Type Variables:
 * - OIDType: The type parameter for the entity's identifier.
 *
 * Note:
 * - Uses abstract class for inheritance
 * - Designed to be inherited by concrete entity classes
 * - Supports any identifier type through generics
 * - Forms the foundation of the domain model hierarchy
 * - Prevents modification of ID after creation
 * - Ensures timestamp consistency
 */
abstract class BaseEntity<OIDType>(
    val id: OIDType,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {
    init {
        // Ensure timestamps are consistent
        if (updatedAt.isBefore(createdAt)) {
            throw InconsistentTimeError.Companion.create(updatedAt, createdAt)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this::class != other::class) return false
        if (other !is BaseEntity<*>) return false
        return other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
