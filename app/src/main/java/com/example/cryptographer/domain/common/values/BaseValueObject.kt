package com.example.cryptographer.domain.common.values

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
 * - Implement validation in init{} block or factory method
 * - Override toString() for meaningful string representation
 *
 * Note:
 * - Value objects must have at least one field
 * - Validation should be performed in init{} block (after field initialization)
 *   or in factory method (before construction)
 * - Value objects are compared by their field values
 *
 * IMPORTANT: Do not call validate() from init{} block in BaseValueObject,
 * because fields may not be initialized yet. Instead, implement validation
 * in the child class's init{} block or factory method.
 */
abstract class BaseValueObject {
    /**
     * Returns a string representation of this value object.
     *
     * Should provide meaningful information about the value object's state.
     */
    abstract override fun toString(): String
}
