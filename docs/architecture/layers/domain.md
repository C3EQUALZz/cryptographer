# Domain Layer

The Domain layer contains the **pure business logic** of the application. It has no dependencies on Android, frameworks, or external libraries (except Kotlin standard library).

## Location

```
domain/
├── text/              # Text encryption domain
│   ├── entities/      # Domain entities
│   ├── services/       # Domain services
│   ├── valueobjects/   # Value objects
│   └── errors/        # Domain errors
└── common/            # Shared domain concepts
    ├── entities/      # Base entities
    ├── errors/        # Base errors
    └── valueobjects/   # Base value objects
```

## Principles

- ✅ **No Android dependencies**: Uses only Kotlin standard library
- ✅ **No framework dependencies**: Independent of Jetpack, Compose, etc.
- ✅ **Pure business logic**: Contains only domain rules and logic
- ✅ **Interface-based**: Communicates with other layers via interfaces

## Entities

Domain entities represent core business concepts:

### EncryptionKey

Represents an encryption key:

```kotlin
data class EncryptionKey(
    val id: KeyId,
    val algorithm: EncryptionAlgorithm,
    val keyBytes: ByteArray
) : BaseEntity<KeyId>() {
    fun isValid(): Boolean {
        return keyBytes.size == algorithm.keySizeInBytes
    }
}
```

### Text

Represents plain text:

```kotlin
data class Text(
    val id: TextId,
    val content: ValidatedText,
    val encoding: TextEncoding
) : BaseEntity<TextId>()
```

### EncryptedText

Represents encrypted text:

```kotlin
data class EncryptedText(
    val id: TextId,
    val encryptedBytes: ByteArray,
    val iv: ByteArray,
    val algorithm: EncryptionAlgorithm
) : BaseEntity<TextId>()
```

## Value Objects

Value objects represent domain concepts without identity:

### EncryptionAlgorithm

```kotlin
enum class EncryptionAlgorithm {
    AES_128,
    AES_192,
    AES_256,
    CHACHA20_256;
    
    val keySizeInBytes: Int
        get() = when (this) {
            AES_128 -> 16
            AES_192 -> 24
            AES_256 -> 32
            CHACHA20_256 -> 32
        }
}
```

### TextEncoding

```kotlin
enum class TextEncoding {
    UTF8,
    ASCII,
    BASE64
}
```

### ValidatedText

```kotlin
data class ValidatedText private constructor(
    val value: String
) {
    companion object {
        fun create(text: String): Result<ValidatedText> {
            return if (text.isNotBlank()) {
                Result.success(ValidatedText(text))
            } else {
                Result.failure(InvalidTextError("Text cannot be empty"))
            }
        }
    }
}
```

## Domain Services

Domain services contain business logic that doesn't naturally fit in entities:

### AesEncryptionService

```kotlin
class AesEncryptionService {
    fun generateKey(algorithm: EncryptionAlgorithm): EncryptionKey {
        val keySize = algorithm.keySizeInBytes
        val keyBytes = ByteArray(keySize)
        SecureRandom().nextBytes(keyBytes)
        return EncryptionKey(
            id = KeyId.generate(),
            algorithm = algorithm,
            keyBytes = keyBytes
        )
    }
    
    fun encrypt(
        text: Text,
        key: EncryptionKey
    ): Result<EncryptedText> {
        // Encryption logic
    }
    
    fun decrypt(
        encryptedText: EncryptedText,
        key: EncryptionKey
    ): Result<Text> {
        // Decryption logic
    }
}
```

### ChaCha20EncryptionService

Similar structure for ChaCha20 encryption.

### TextService

Handles text encoding conversion:

```kotlin
class TextService {
    fun convertEncoding(
        text: Text,
        targetEncoding: TextEncoding
    ): Result<Text> {
        // Encoding conversion logic
    }
}
```

## Domain Errors

Domain-specific errors:

```kotlin
sealed class DomainError : AppError {
    data class InvalidKeyError(override val message: String) : DomainError()
    data class UnsupportedAlgorithmError(override val message: String) : DomainError()
    data class EncryptionError(override val message: String) : DomainError()
    data class DecryptionError(override val message: String) : DomainError()
}
```

## Testing

Domain layer is easily testable without Android:

```kotlin
@Test
fun `encryption key validation works correctly`() {
    val key = EncryptionKey(
        id = KeyId("test"),
        algorithm = EncryptionAlgorithm.AES_256,
        keyBytes = ByteArray(32)
    )
    assertTrue(key.isValid())
}

@Test
fun `invalid key size fails validation`() {
    val key = EncryptionKey(
        id = KeyId("test"),
        algorithm = EncryptionAlgorithm.AES_256,
        keyBytes = ByteArray(16) // Wrong size
    )
    assertFalse(key.isValid())
}
```

## Learn More

- [Clean Architecture](../clean-architecture.md) - Overall architecture
- [Application Layer](application.md) - How domain is used
- [CQRS Pattern](../cqrs.md) - Command/Query pattern



