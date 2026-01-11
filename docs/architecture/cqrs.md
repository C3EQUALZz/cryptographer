# CQRS Pattern

The Cryptographer application uses the **CQRS (Command Query Responsibility Segregation)** pattern to separate operations that modify state (commands) from operations that read state (queries).

## Overview

CQRS separates the responsibility of:
- **Commands**: Operations that change state (write operations)
- **Queries**: Operations that read state (read operations)

This separation provides:
- Clear intent in code
- Better scalability
- Easier testing
- Simplified error handling

## Structure

```
application/
├── commands/          # Write operations
│   ├── key/
│   │   ├── create/    # Create key commands
│   │   ├── delete/    # Delete key commands
│   │   └── deleteall/ # Delete all keys commands
│   ├── text/
│   │   ├── encrypt/   # Encrypt text commands
│   │   ├── decrypt/   # Decrypt text commands
│   │   └── convertencoding/ # Convert encoding commands
│   └── ...
└── queries/           # Read operations
    ├── key/
    │   ├── readall/   # Read all keys queries
    │   └── readbyid/  # Read key by ID queries
    └── ...
```

## Commands

Commands represent **intentions to change state**. Each command has:

- **Command**: Data class representing the command
- **Command Handler**: Handles the command execution

### Command Structure

```kotlin
// Command - data class
data class AesGenerateAndSaveKeyCommand(
    val algorithm: EncryptionAlgorithm
)

// Command Handler
class AesGenerateAndSaveKeyCommandHandler @Inject constructor(
    private val encryptionService: AesEncryptionService,
    private val keyStorage: KeyCommandGateway
) {
    suspend fun handle(command: AesGenerateAndSaveKeyCommand): Result<KeyView> {
        // 1. Generate key using domain service
        val key = encryptionService.generateKey(command.algorithm)
        
        // 2. Save key via infrastructure adapter
        keyStorage.save(key)
        
        // 3. Return result
        return Result.success(key.toView())
    }
}
```

### Available Commands

#### Key Commands

- **`AesGenerateAndSaveKeyCommand`**: Generate and save AES key
- **`ChaCha20GenerateAndSaveKeyCommand`**: Generate and save ChaCha20 key
- **`DeleteKeyCommand`**: Delete a specific key
- **`DeleteAllKeysCommand`**: Delete all saved keys

#### Text Commands

- **`AesEncryptTextCommand`**: Encrypt text using AES
- **`ChaCha20EncryptTextCommand`**: Encrypt text using ChaCha20
- **`AesDecryptTextCommand`**: Decrypt text using AES
- **`ChaCha20DecryptTextCommand`**: Decrypt text using ChaCha20
- **`ConvertTextEncodingCommand`**: Convert text encoding

#### Settings Commands

- **`UpdateLanguageCommand`**: Update app language
- **`UpdateThemeCommand`**: Update app theme

## Queries

Queries represent **read operations**. Each query has:

- **Query**: Data class representing the query
- **Query Handler**: Handles the query execution

### Query Structure

```kotlin
// Query - data class
data class ReadAllKeysQuery(
    // No parameters needed for reading all keys
)

// Query Handler
class ReadAllKeysQueryHandler @Inject constructor(
    private val keyStorage: KeyQueryGateway
) {
    suspend fun handle(query: ReadAllKeysQuery): Result<List<KeyView>> {
        // 1. Read keys from storage
        val keys = keyStorage.readAll()
        
        // 2. Convert to views
        val views = keys.map { it.toView() }
        
        // 3. Return result
        return Result.success(views)
    }
}
```

### Available Queries

#### Key Queries

- **`ReadAllKeysQuery`**: Read all saved keys
- **`ReadKeyByIdQuery`**: Read a specific key by ID

#### Settings Queries

- **`ReadLanguageQuery`**: Read current language setting
- **`ReadThemeQuery`**: Read current theme setting

## Views (DTOs)

Views are Data Transfer Objects used to pass data between layers:

```kotlin
// View - DTO for presentation
data class KeyView(
    val id: String,
    val algorithm: String,
    val keyBase64: String
)

// Conversion from domain entity
fun EncryptionKey.toView(): KeyView {
    return KeyView(
        id = this.id.value,
        algorithm = this.algorithm.name,
        keyBase64 = Base64.encodeToString(this.keyBytes, Base64.NO_WRAP)
    )
}
```

## Usage in ViewModels

ViewModels use command and query handlers:

```kotlin
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    private val generateKeyCommand: AesGenerateAndSaveKeyCommandHandler,
    private val readAllKeysQuery: ReadAllKeysQueryHandler
) : ViewModel() {
    
    fun generateKey(algorithm: EncryptionAlgorithm) {
        viewModelScope.launch {
            generateKeyCommand.handle(
                AesGenerateAndSaveKeyCommand(algorithm)
            ).fold(
                onSuccess = { /* update UI state */ },
                onFailure = { /* handle error */ }
            )
        }
    }
    
    fun loadKeys() {
        viewModelScope.launch {
            readAllKeysQuery.handle(ReadAllKeysQuery())
                .fold(
                    onSuccess = { /* update UI state */ },
                    onFailure = { /* handle error */ }
                )
        }
    }
}
```

## Error Handling

Commands and queries return `Result<T>` for error handling:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()
}

// Usage
result.fold(
    onSuccess = { key -> /* handle success */ },
    onFailure = { error -> /* handle error */ }
)
```

## Benefits

1. **Clear Intent**: Commands vs queries are explicit
2. **Separation of Concerns**: Write and read logic separated
3. **Testability**: Easy to test commands and queries independently
4. **Scalability**: Can optimize reads and writes separately
5. **Maintainability**: Clear structure makes code easier to understand

## Learn More

- [Clean Architecture](clean-architecture.md) - Overall architecture
- [Application Layer](../layers/application.md) - Application layer details
- [Dependency Injection](dependency-injection.md) - How handlers are injected

