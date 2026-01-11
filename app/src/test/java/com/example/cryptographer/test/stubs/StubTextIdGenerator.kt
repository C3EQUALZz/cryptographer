package com.example.cryptographer.test.stubs

import com.example.cryptographer.domain.text.ports.TextIdGeneratorPort
import java.util.UUID

/**
 * Stub implementation of TextIdGeneratorPort for testing.
 * Generates predictable IDs for testing purposes.
 */
class StubTextIdGenerator(
    private var nextId: String = "test-id-1",
) : TextIdGeneratorPort {
    private val generatedIds = mutableListOf<String>()

    override fun generate(): String {
        val id = nextId
        generatedIds.add(id)
        // Auto-increment for convenience
        nextId = if (nextId.startsWith("test-id-")) {
            val number = nextId.substringAfter("test-id-").toIntOrNull() ?: 0
            "test-id-${number + 1}"
        } else {
            UUID.randomUUID().toString()
        }
        return id
    }

    /**
     * Sets the next ID to be generated.
     */
    fun setNextId(id: String) {
        nextId = id
    }

}
