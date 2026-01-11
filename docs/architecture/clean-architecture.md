# Clean Architecture

The Cryptographer application follows **Clean Architecture** principles as defined by Robert C. Martin (Uncle Bob). This ensures the business logic remains independent of frameworks, UI, and external systems.

## Core Concept

> A solid architecture postpones decisions until the last responsible moment.

The best architecture often emerges late in development, once the codebase takes shape. Clean Architecture helps achieve this by separating concerns into distinct layers.

## Dependency Rule

The fundamental rule of Clean Architecture is the **Dependency Inversion Principle**:

> Dependencies point inward toward the domain.

This means:
- Inner layers (Domain) have **no dependencies** on outer layers
- Outer layers depend on inner layers through **interfaces**
- Business logic is **isolated** from frameworks and UI

## Layer Structure

```
┌─────────────────────────────────────┐
│     Presentation Layer (UI)        │
│  - Compose Screens                  │
│  - ViewModels                       │
│  - UI Components                    │
└──────────────┬──────────────────────┘
               │ depends on
┌──────────────▼──────────────────────┐
│     Application Layer (Use Cases)    │
│  - Commands                         │
│  - Queries                          │
│  - Orchestration                    │
└──────────────┬──────────────────────┘
               │ depends on
┌──────────────▼──────────────────────┐
│        Domain Layer (Business)       │
│  - Entities                          │
│  - Value Objects                    │
│  - Domain Services                  │
│  - Business Rules                    │
└─────────────────────────────────────┘
               ▲
               │ implemented by
┌──────────────┴──────────────────────┐
│   Infrastructure Layer (Adapters)   │
│  - Storage Adapters                 │
│  - ID Generators                   │
│  - External Services                │
└─────────────────────────────────────┘
```

## Layer Details

### Domain Layer (Inner)

**Pure business logic** with no Android dependencies.

**Rules:**
- ✅ Uses only Kotlin standard library
- ✅ No Android imports
- ✅ No framework dependencies
- ✅ Communicates via interfaces

**Contains:**
- Entities (`Text`, `EncryptionKey`, `EncryptedText`)
- Value Objects (`EncryptionAlgorithm`, `TextEncoding`)
- Domain Services (`AesEncryptionService`, `ChaCha20EncryptionService`)
- Domain Events

**Example:**

```kotlin
// Domain entity - no Android dependencies
data class EncryptionKey(
    val id: KeyId,
    val algorithm: EncryptionAlgorithm,
    val keyBytes: ByteArray
) {
    fun isValid(): Boolean {
        return keyBytes.size == algorithm.keySizeInBytes
    }
}
```

### Application Layer

**Orchestrates** domain logic through commands and queries.

**Rules:**
- ✅ Depends only on Domain layer
- ✅ Uses CQRS pattern
- ✅ Handles application-specific errors
- ✅ Coordinates domain services

**Contains:**
- Command handlers
- Query handlers
- View models (DTOs)
- Application errors

**Example:**

```kotlin
// Command handler - orchestrates domain logic
class AesGenerateAndSaveKeyCommandHandler @Inject constructor(
    private val encryptionService: AesEncryptionService,
    private val keyStorage: KeyCommandGateway
) {
    suspend fun handle(command: AesGenerateAndSaveKeyCommand): Result<KeyView> {
        // Use domain service
        val key = encryptionService.generateKey(command.algorithm)
        // Save via infrastructure adapter
        keyStorage.save(key)
        return Result.success(key.toView())
    }
}
```

### Infrastructure Layer

**Adapters** that connect domain to external systems.

**Rules:**
- ✅ Implements domain interfaces
- ✅ Handles Android-specific code
- ✅ Manages external dependencies
- ✅ Converts between domain and external models

**Contains:**
- Storage adapters (SharedPreferences, Room, etc.)
- ID generators
- External service clients

**Example:**

```kotlin
// Infrastructure adapter - implements domain interface
class KeyCommandGatewayAdapter @Inject constructor(
    private val context: Context
) : KeyCommandGateway {
    override suspend fun save(key: EncryptionKey) {
        // Android-specific storage implementation
        val prefs = context.getSharedPreferences("keys", Context.MODE_PRIVATE)
        prefs.edit().putString(key.id.value, key.toJson()).apply()
    }
}
```

### Presentation Layer

**UI components** and state management.

**Rules:**
- ✅ Depends on Application layer
- ✅ Uses Jetpack Compose
- ✅ Manages UI state
- ✅ Handles user interactions

**Contains:**
- Compose screens
- ViewModels
- UI components
- Navigation

**Example:**

```kotlin
// ViewModel - coordinates UI and application layer
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    private val generateKeyCommand: AesGenerateAndSaveKeyCommandHandler
) : ViewModel() {
    private val _uiState = MutableStateFlow(KeyGenerationUiState())
    val uiState: StateFlow<KeyGenerationUiState> = _uiState.asStateFlow()
    
    fun generateKey(algorithm: EncryptionAlgorithm) {
        viewModelScope.launch {
            generateKeyCommand.handle(
                AesGenerateAndSaveKeyCommand(algorithm)
            ).fold(
                onSuccess = { _uiState.value = it },
                onFailure = { /* handle error */ }
            )
        }
    }
}
```

## Benefits

### 1. Testability

Domain logic can be tested without Android dependencies:

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
```

### 2. Independence

Business logic is independent of:
- UI framework (can switch from Compose to XML)
- Storage mechanism (can switch from SharedPreferences to Room)
- External libraries

### 3. Maintainability

Clear separation makes code:
- Easier to understand
- Easier to modify
- Less prone to bugs

### 4. Scalability

Easy to add new features:
- New encryption algorithm? Add to Domain layer
- New UI screen? Add to Presentation layer
- New storage? Implement Infrastructure adapter

## Data Flow Example

Encrypting text:

```
1. User enters text in UI (Presentation)
   ↓
2. ViewModel calls command handler (Application)
   ↓
3. Command handler uses encryption service (Domain)
   ↓
4. Service encrypts text (Domain)
   ↓
5. Command handler saves via storage adapter (Infrastructure)
   ↓
6. ViewModel updates UI state (Presentation)
   ↓
7. UI displays encrypted text (Presentation)
```

## Learn More

- [CQRS Pattern](cqrs.md) - Command/Query separation
- [Dependency Injection](dependency-injection.md) - Hilt setup
- [Domain Layer](../layers/domain.md) - Domain layer details
- [Application Layer](../layers/application.md) - Application layer details

