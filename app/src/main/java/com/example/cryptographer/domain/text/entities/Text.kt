package com.example.cryptographer.domain.text.entities

import com.example.cryptographer.domain.common.entities.BaseEntity
import com.example.cryptographer.domain.text.value_objects.TextEncoding
import com.example.cryptographer.domain.text.value_objects.ValidatedText

/**
 * Entity representing text data in the domain.
 * This is a pure business object without framework dependencies.
 *
 * Uses ValidatedText as Value Object to ensure content is always validated.
 *
 * Note: Text entities should be created through TextService, not directly.
 */
class Text(
    id: String,
    val content: ValidatedText,
    val encoding: TextEncoding = TextEncoding.UTF8,
    createdAt: java.time.Instant = java.time.Instant.now(),
    updatedAt: java.time.Instant = java.time.Instant.now()
) : BaseEntity<String>(id, createdAt, updatedAt) {

    val length: Int
        get() = content.content.length

}
