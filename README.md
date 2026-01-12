<h2 align="center">Cryptographer</h2>

*A modern Android application for text encryption, decryption, and encoding conversion built with
Clean Architecture principles.*

Built using the principles of Robert Martin (aka Uncle Bob) and Domain-Driven Design (DDD).

---

## Tech Stack

### Core Technologies

| Tool                  | Role                                          |
|-----------------------|-----------------------------------------------|
| **Kotlin**            | Primary programming language                  |
| **Jetpack Compose**   | Modern declarative UI framework               |
| **Material Design 3** | Material You design system                    |
| **Hilt**              | Dependency injection framework                |
| **Coroutines + Flow** | Asynchronous programming and reactive streams |

### Architecture & Patterns

| Pattern/Concept        | Role                                     |
|------------------------|------------------------------------------|
| **Clean Architecture** | Separation of concerns across layers     |
| **CQRS**               | Command Query Responsibility Segregation |
| **DDD**                | Domain-Driven Design principles          |
| **MVVM**               | Model-View-ViewModel pattern             |

### Code Quality

| Tool          | Role                     |
|---------------|--------------------------|
| **Spotless**  | Code formatting (ktlint) |
| **Detekt**    | Static code analysis     |
| **GitLab CI** | Continuous Integration   |

## Features

- **Key Generation**: Generate encryption keys for AES-128, AES-192, AES-256, and ChaCha20-256
  algorithms
- **Text Encryption**: Encrypt plain text using AES or ChaCha20 with secure random IV generation
- **Text Decryption**: Decrypt encrypted text with support for custom IV (Initialization Vector)
- **Encoding Conversion**: Convert text between UTF-8, ASCII, and BASE64 encodings
- **Key Management**: Save, view, and delete encryption keys locally
- **Clipboard Integration**: Copy encrypted/decrypted text and keys to clipboard
- **Multi-language Support**: Internationalization support for multiple languages
- **Theme Support**: Light and dark theme modes

## Supported Algorithms

- **AES-128**: Advanced Encryption Standard with 128-bit key
- **AES-192**: Advanced Encryption Standard with 192-bit key
- **AES-256**: Advanced Encryption Standard with 256-bit key
- **ChaCha20-256**: ChaCha20 stream cipher with 256-bit key

## Supported Encodings

- **UTF-8**: Unicode Transformation Format
- **ASCII**: American Standard Code for Information Interchange
- **BASE64**: Base64 encoding scheme

## Quick Start

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 33+ (minimum SDK version)
- Git

### Setup

```sh
git clone <repository-url>
cd cryptographer

# Open in Android Studio
# Sync Gradle files
# Run the app
```

### Building the Project

```sh
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Code formatting
./gradlew spotlessApply

# Static analysis
./gradlew detekt
```

### Configuration

The project uses Gradle's version catalog (`gradle/libs.versions.toml`) for dependency management.
Key configuration files:

- **Build configuration**: `app/build.gradle.kts`
- **Project configuration**: `build.gradle.kts`
- **Code quality**: `detekt.yml`, Spotless configuration in `build.gradle.kts`

## Architecture

### Clean Architecture Layers

The project follows Clean Architecture with clear separation of concerns:

#### Domain Layer

**Location:** `domain/`

Pure business logic with no Android dependencies.

- **Entities**: `Text`, `EncryptionKey`, `EncryptedText`
- **Value Objects**: `EncryptionAlgorithm`, `TextEncoding`, `ValidatedText`
- **Services**: `AesEncryptionService`, `ChaCha20EncryptionService`, `TextService`
- **Domain Events**: Base domain event infrastructure

#### Application Layer

**Location:** `application/`

Orchestrates domain logic through commands and queries (CQRS pattern).

- **Commands**: Key generation, encryption, decryption, encoding conversion
- **Queries**: Key retrieval, settings retrieval
- **Views**: Data transfer objects for presentation
- **Error Handling**: Application-specific error types

#### Infrastructure Layer

**Location:** `infrastructure/`

Adapters connecting domain to external systems.

- **Key Storage**: Local key persistence adapters
- **Settings Storage**: User preferences and settings adapters
- **ID Generation**: UUID-based ID generation

#### Presentation Layer

**Location:** `presentation/`

UI components and state management.

- **Screens**: Compose UI screens (`KeyGenerationScreen`, `EncryptionScreen`, `EncodingScreen`)
- **ViewModels**: State management with `StateFlow`
- **Components**: Reusable Compose UI components
- **Navigation**: Screen navigation logic

### Dependency Flow

```
Presentation → Application → Domain
                   ↓
              Infrastructure
```

**Key Principles:**

- Inner layers (Domain) have no dependencies on outer layers
- Dependencies point inward toward the domain
- Business logic is isolated from frameworks and UI

### Dependency Injection

Using **Hilt** for dependency injection:

- **Application Class**: `CryptographerApplication` annotated with `@HiltAndroidApp`
- **DI Module**: `setup/ioc/AppModule.kt` provides domain services
- **ViewModels**: Annotated with `@HiltViewModel`
- **Activities**: Annotated with `@AndroidEntryPoint`

## Development

### Code Quality Tools

The project includes automated code quality checks:

- **Spotless**: Enforces code formatting using ktlint
- **Detekt**: Static code analysis for Kotlin
- **GitLab CI**: Automated builds and tests

### Running Quality Checks

```sh
# Format code
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Run static analysis
./gradlew detekt

# Run all checks
./gradlew check
```

### Git Hooks

Install git hooks for pre-commit checks:

```sh
./gradlew installGitHooks
```

This will run Spotless and Detekt before each commit.

## Project Structure

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

## Security Considerations

- Keys are currently stored using SharedPreferences (for development)
- Encryption uses secure random IV generation
- All cryptographic operations follow Android security best practices

## Testing

The project includes unit tests for domain logic and use cases:

```sh
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## CI/CD

GitLab CI is configured (`.gitlab-ci.yml`) with the following stages:

- **Validate**: Code formatting and static analysis
- **Test**: Unit tests execution
- **Build**: APK generation (debug and release)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please ensure:

1. Code follows the existing architecture patterns
2. All tests pass
3. Code is formatted with Spotless
4. Detekt checks pass
5. Follow Clean Architecture principles

---

For more details, see the documentation.



