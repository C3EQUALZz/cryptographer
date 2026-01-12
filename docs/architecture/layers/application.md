# Application Layer

The Application layer orchestrates domain logic through commands and queries (CQRS pattern). It coordinates between the domain layer and infrastructure layer.

## Location

```
application/
├── commands/          # Command handlers
│   ├── key/
│   ├── text/
│   └── settings/
├── queries/          # Query handlers
│   ├── key/
│   └── settings/
├── common/           # Shared application code
│   ├── ports/        # Interfaces
│   └── views/        # DTOs
└── errors/           # Application errors
```

## Principles

- ✅ **Orchestrates domain logic**: Coordinates domain services
- ✅ **CQRS pattern**: Separates commands and queries
- ✅ **Interface-based**: Uses ports (interfaces) for infrastructure
- ✅ **Error handling**: Converts domain errors to application errors

## Commands

Commands represent write operations:

### Key Commands

#### Generate and Save Key

```kotlin
data class AesGenerateAndSaveKeyCommand(
    val algorithm: EncryptionAlgorithm
)

class AesGenerateAndSaveKeyCommandHandler @Inject constructor(
    private val encryptionService: AesEncryptionService,
    private val keyStorage: KeyCommandGateway
) {
    suspend fun handle(command: AesGenerateAndSaveKeyCommand): Result<KeyView> {
        return try {
            // 1. Generate key using domain service
            val key = encryptionService.generateKey(command.algorithm)
            
            // 2. Save via infrastructure adapter
            keyStorage.save(key)
            
            // 3. Convert to view
            Result.success(key.toView())
        } catch (e: Exception) {
            Result.failure(KeyGenerationError(e.message ?: "Unknown error"))
        }
    }
}
```

#### Delete Key

```kotlin
data class DeleteKeyCommand(
    val keyId: String
)

class DeleteKeyCommandHandler @Inject constructor(
    private val keyStorage: KeyCommandGateway
) {
    suspend fun handle(command: DeleteKeyCommand): Result<Unit> {
        return try {
            keyStorage.delete(KeyId(command.keyId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(KeyDeleteError(e.message ?: "Unknown error"))
        }
    }
}
```

### Text Commands

#### Encrypt Text

```kotlin
data class AesEncryptTextCommand(
    val text: String,
    val keyId: String,
    val algorithm: EncryptionAlgorithm
)

class AesEncryptTextCommandHandler @Inject constructor(
    private val encryptionService: AesEncryptionService,
    private val keyStorage: KeyQueryGateway
) {
    suspend fun handle(command: AesEncryptTextCommand): Result<EncryptedTextView> {
        return try {
            // 1. Get key
            val key = keyStorage.readById(KeyId(command.keyId))
                ?: return Result.failure(KeyNotFoundError("Key not found"))
            
            // 2. Create text entity
            val text = Text.create(command.text, TextEncoding.UTF8)
                .getOrElse { return Result.failure(it) }
            
            // 3. Encrypt
            val encrypted = encryptionService.encrypt(text, key)
                .getOrElse { return Result.failure(it) }
            
            // 4. Convert to view
            Result.success(encrypted.toView())
        } catch (e: Exception) {
            Result.failure(EncryptionError(e.message ?: "Unknown error"))
        }
    }
}
```

## Queries

Queries represent read operations:

### Key Queries

#### Read All Keys

```kotlin
data class ReadAllKeysQuery(
    // No parameters
)

class ReadAllKeysQueryHandler @Inject constructor(
    private val keyStorage: KeyQueryGateway
) {
    suspend fun handle(query: ReadAllKeysQuery): Result<List<KeyView>> {
        return try {
            val keys = keyStorage.readAll()
            val views = keys.map { it.toView() }
            Result.success(views)
        } catch (e: Exception) {
            Result.failure(KeyReadError(e.message ?: "Unknown error"))
        }
    }
}
```

## Views (DTOs)

Views are Data Transfer Objects for presentation:

```kotlin
data class KeyView(
    val id: String,
    val algorithm: String,
    val keyBase64: String
)

data class EncryptedTextView(
    val encryptedBase64: String,
    val ivBase64: String,
    val algorithm: String
)

// Conversion functions
fun EncryptionKey.toView(): KeyView {
    return KeyView(
        id = this.id.value,
        algorithm = this.algorithm.name,
        keyBase64 = Base64.encodeToString(this.keyBytes, Base64.NO_WRAP)
    )
}
```

## Ports (Interfaces)

Ports define contracts for infrastructure adapters:

```kotlin
interface KeyCommandGateway {
    suspend fun save(key: EncryptionKey)
    suspend fun delete(keyId: KeyId)
    suspend fun deleteAll()
}

interface KeyQueryGateway {
    suspend fun readAll(): List<EncryptionKey>
    suspend fun readById(keyId: KeyId): EncryptionKey?
}
```

## Error Handling

Application errors wrap domain errors:

```kotlin
sealed class ApplicationError : AppError {
    data class KeyGenerationError(override val message: String) : ApplicationError()
    data class KeyDeleteError(override val message: String) : ApplicationError()
    data class KeyNotFoundError(override val message: String) : ApplicationError()
    data class EncryptionError(override val message: String) : ApplicationError()
    data class DecryptionError(override val message: String) : ApplicationError()
}
```

## Learn More

- [CQRS Pattern](../cqrs.md) - Command/Query separation
- [Infrastructure Layer](infrastructure.md) - How adapters implement ports
- [Presentation Layer](presentation.md) - How ViewModels use commands/queries



