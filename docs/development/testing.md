# Testing

The Cryptographer project includes comprehensive testing strategies for all layers of the application.

## Test Structure

```
app/src/
├── test/              # Unit tests
└── androidTest/       # Instrumented tests
```

## Unit Tests

Unit tests test individual components in isolation.

### Domain Layer Tests

Test domain logic without Android dependencies:

```kotlin
class EncryptionKeyTest {
    @Test
    fun `key validation works correctly`() {
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
}
```

### Application Layer Tests

Test command and query handlers:

```kotlin
class AesGenerateAndSaveKeyCommandHandlerTest {
    @Test
    fun `generates and saves key successfully`() = runTest {
        // Arrange
        val handler = AesGenerateAndSaveKeyCommandHandler(
            encryptionService = mockk(),
            keyStorage = mockk()
        )
        
        // Act
        val result = handler.handle(
            AesGenerateAndSaveKeyCommand(EncryptionAlgorithm.AES_256)
        )
        
        // Assert
        assertTrue(result.isSuccess)
    }
}
```

## Running Tests

### All Unit Tests

```bash
./gradlew test
```

### Specific Test Class

```bash
./gradlew test --tests "EncryptionKeyTest"
```

### Specific Test Method

```bash
./gradlew test --tests "EncryptionKeyTest.key validation works correctly"
```

## Test Reports

Test reports are generated at:

```
app/build/reports/tests/test/index.html
```

## Testing Libraries

### JUnit

Standard testing framework:

```kotlin
import org.junit.Test
import org.junit.Assert.assertTrue
```

### MockK

Mocking library for Kotlin:

```kotlin
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify

val mockService = mockk<AesEncryptionService>()
every { mockService.generateKey(any()) } returns mockKey
```

### Coroutines Test

Testing coroutines:

```kotlin
import kotlinx.coroutines.test.runTest

@Test
fun `async operation works`() = runTest {
    // Test coroutine code
}
```

## Test Coverage

### Generate Coverage Report

```bash
./gradlew test jacocoTestReport
```

Coverage report:

```
app/build/reports/jacoco/test/html/index.html
```

## Best Practices

### 1. Test Behavior, Not Implementation

```kotlin
// Good - tests behavior
@Test
fun `encrypts text correctly`() {
    val result = encryptionService.encrypt(text, key)
    assertTrue(result.isSuccess)
}

// Bad - tests implementation details
@Test
fun `calls encryption method`() {
    verify { encryptionService.encrypt(any(), any()) }
}
```

### 2. Use Descriptive Test Names

```kotlin
// Good
@Test
fun `encryption key validation works correctly`()

// Bad
@Test
fun testKey()
```

### 3. Arrange-Act-Assert Pattern

```kotlin
@Test
fun `test example`() {
    // Arrange
    val key = EncryptionKey(...)
    
    // Act
    val result = key.isValid()
    
    // Assert
    assertTrue(result)
}
```

### 4. Test Edge Cases

```kotlin
@Test
fun `handles empty text`() {
    val result = Text.create("")
    assertTrue(result.isFailure)
}

@Test
fun `handles null input`() {
    // Test null handling
}
```

## Learn More

- [Development Setup](setup.md) - Development environment
- [Code Quality](code-quality.md) - Quality tools
- [CI/CD](ci-cd.md) - Automated testing

