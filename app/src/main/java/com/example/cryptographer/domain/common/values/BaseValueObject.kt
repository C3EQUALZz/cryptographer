package com.example.cryptographer.domain.common.values

import com.example.cryptographer.domain.common.errors.DomainFieldError

/**
 * Base class for immutable value objects (VO) in the domain.
 *
 * Value objects are defined by their attributes, which must also be immutable.
 * They have no identity and are compared by value.
 *
 * For simple cases where immutability and additional behavior aren't required,
 * consider using type aliases or inline classes as a lightweight alternative
 * to inheriting from this class.
 *
 * All value objects should:
 * - Be immutable (use data class with val properties)
 * - Implement validation in _validate()
 * - Override toString() for meaningful string representation
 *
 * Note:
 * - Value objects must have at least one field
 * - Validation is performed during construction
 * - Value objects are compared by their field values
 */
abstract class BaseValueObject {
    init {
        validate()
    }

    /**
     * Validates that the value object is in a valid state.
     *
     * This method is called during construction to ensure
     * the value object meets all business rules.
     *
     * @throws DomainFieldError if validation fails
     */
    protected abstract fun validate()

    /**
     * Returns a string representation of this value object.
     *
     * Should provide meaningful information about the value object's state.
     */
    abstract override fun toString(): String
}
