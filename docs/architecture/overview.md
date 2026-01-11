# Architecture Overview

The Cryptographer application is built using **Clean Architecture** principles, ensuring a modular, testable, and maintainable codebase. This section provides an overview of the architectural design.

## Project Structure

The codebase is organized following Clean Architecture with clear layer separation:

```
app/src/main/java/com/example/cryptographer/
├── domain/              # Business logic (no Android dependencies)
│   ├── text/            # Text encryption domain
│   └── common/          # Shared domain concepts
├── application/         # Use cases and orchestration (CQRS)
│   ├── commands/        # Command handlers
│   ├── queries/         # Query handlers
│   └── common/          # Shared application code
├── infrastructure/      # External adapters
│   ├── key/             # Key storage adapters
│   └── settings/        # Settings storage adapters
├── presentation/        # UI layer
│   ├── key/             # Key generation screen
│   ├── encryption/      # Encryption/decryption screen
│   ├── encoding/        # Encoding conversion screen
│   └── main/            # Main navigation
└── setup/               # Configuration and setup
    ├── configs/         # Theme and styling
    ├── i18n/            # Internationalization
    └── ioc/             # Dependency injection
```

## Key Components

- **Jetpack Compose**: Powers the modern declarative UI
- **Hilt**: Handles dependency injection
- **Coroutines + Flow**: Manages asynchronous operations and reactive streams
- **Material Design 3**: Provides the design system
- **Clean Architecture**: Ensures separation of concerns
- **CQRS**: Separates commands and queries
- **DDD**: Domain-Driven Design principles

## Architecture Layers

The application follows Clean Architecture with four primary layers:

### 1. Domain Layer
**Location:** `domain/`

Pure business logic with no Android dependencies.

- **Entities**: `Text`, `EncryptionKey`, `EncryptedText`
- **Value Objects**: `EncryptionAlgorithm`, `TextEncoding`, `ValidatedText`
- **Services**: `AesEncryptionService`, `ChaCha20EncryptionService`, `TextService`
- **Domain Events**: Base domain event infrastructure

### 2. Application Layer
**Location:** `application/`

Orchestrates domain logic through commands and queries (CQRS pattern).

- **Commands**: Key generation, encryption, decryption, encoding conversion
- **Queries**: Key retrieval, settings retrieval
- **Views**: Data transfer objects for presentation
- **Error Handling**: Application-specific error types

### 3. Infrastructure Layer
**Location:** `infrastructure/`

Adapters connecting domain to external systems.

- **Key Storage**: Local key persistence adapters
- **Settings Storage**: User preferences and settings adapters
- **ID Generation**: UUID-based ID generation

### 4. Presentation Layer
**Location:** `presentation/`

UI components and state management.

- **Screens**: Compose UI screens
- **ViewModels**: State management with `StateFlow`
- **Components**: Reusable Compose UI components
- **Navigation**: Screen navigation logic

## Dependency Flow

```
Presentation → Application → Domain
                    ↓
               Infrastructure
```

**Key Principles:**

- Inner layers (Domain) have no dependencies on outer layers
- Dependencies point inward toward the domain
- Business logic is isolated from frameworks and UI
- Each layer has clear responsibilities

## Design Patterns

### CQRS (Command Query Responsibility Segregation)

Commands and queries are separated:
- **Commands**: Modify state (generate key, encrypt text)
- **Queries**: Read state (get keys, get settings)

### MVVM (Model-View-ViewModel)

- **Model**: Domain entities and business logic
- **View**: Compose UI components
- **ViewModel**: State management and UI logic

### Dependency Injection

Using Hilt for dependency injection:
- Services provided as singletons
- ViewModels automatically injected
- Clear dependency graph

## Benefits

1. **Testability**: Domain logic can be tested without Android dependencies
2. **Maintainability**: Clear separation makes code easier to understand and modify
3. **Scalability**: Easy to add new features without affecting existing code
4. **Flexibility**: Can swap implementations (e.g., storage adapters) easily

## Learn More

- [Clean Architecture](clean-architecture.md) - Detailed layer explanation
- [CQRS Pattern](cqrs.md) - Command/Query separation
- [Dependency Injection](dependency-injection.md) - Hilt configuration
- [Layers](../architecture/layers/domain.md) - Detailed layer documentation

