# Code Quality

The Cryptographer project uses several tools to ensure code quality and consistency.

## Tools

### Spotless (Code Formatting)

**Purpose**: Enforces consistent code formatting using ktlint.

**Configuration**: `app/build.gradle.kts`

```kotlin
spotless {
    kotlin {
        target("**/*.kt")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}
```

**Usage**:

```bash
# Format code
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck
```

### Detekt (Static Analysis)

**Purpose**: Analyzes Kotlin code for potential issues and code smells.

**Configuration**: `detekt.yml`

**Usage**:

```bash
# Run analysis
./gradlew detekt

# Generate report
./gradlew detekt
# Report: app/build/reports/detekt/detekt.html
```

**Key Rules**:

- Complexity checks
- Code smell detection
- Style violations
- Performance issues

### Git Hooks

**Purpose**: Run quality checks before commits.

**Installation**:

```bash
./gradlew installGitHooks
```

**What Runs**:

- Spotless formatting check
- Detekt static analysis

## Quality Checks

### Running All Checks

```bash
./gradlew check
```

This runs:
- Spotless formatting check
- Detekt static analysis
- Unit tests

### Pre-commit Checks

If git hooks are installed, checks run automatically on commit.

## Code Standards

### Kotlin Style

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

### Architecture Rules

- Domain layer: No Android dependencies
- Application layer: Depends only on domain
- Infrastructure: Implements application interfaces
- Presentation: Depends on application layer

### Naming Conventions

- **Classes**: PascalCase
- **Functions**: camelCase
- **Variables**: camelCase
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: lowercase

## Best Practices

### 1. Keep Functions Small

```kotlin
// Good
fun encryptText(text: String): Result<EncryptedText> {
    return encryptionService.encrypt(text)
}

// Bad
fun encryptText(text: String): Result<EncryptedText> {
    // 50+ lines of logic
}
```

### 2. Use Meaningful Names

```kotlin
// Good
fun generateEncryptionKey(algorithm: EncryptionAlgorithm)

// Bad
fun genKey(alg: EncryptionAlgorithm)
```

### 3. Handle Errors Properly

```kotlin
// Good
result.fold(
    onSuccess = { /* handle success */ },
    onFailure = { error -> /* handle error */ }
)

// Bad
try {
    val result = operation()
} catch (e: Exception) {
    // Swallow error
}
```

### 4. Write Tests

```kotlin
@Test
fun `encryption key validation works correctly`() {
    val key = EncryptionKey(...)
    assertTrue(key.isValid())
}
```

## Continuous Integration

GitLab CI runs quality checks automatically:

- **Validate Stage**: Spotless and Detekt
- **Test Stage**: Unit tests
- **Build Stage**: APK generation

See [CI/CD](ci-cd.md) for details.

## Learn More

- [Development Setup](setup.md) - Development environment
- [Testing](testing.md) - Testing strategies
- [CI/CD](ci-cd.md) - Continuous Integration

