# Dependency Injection

The Cryptographer application uses **Hilt** (built on Dagger) for dependency injection. Hilt provides a standard way to incorporate Dagger dependency injection into an Android application.

## Overview

Hilt simplifies dependency injection by:
- Reducing boilerplate code
- Providing Android-specific components
- Automatically managing component lifecycles
- Integrating with Android classes

## Setup

### Application Class

The application class is annotated with `@HiltAndroidApp`:

```kotlin
@HiltAndroidApp
class CryptographerApplication : Application() {
    // Application initialization
}
```

Registered in `AndroidManifest.xml`:

```xml
<application
    android:name=".CryptographerApplication"
    ...>
</application>
```

### DI Module

Dependency injection is configured in `setup/ioc/AppModule.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAesEncryptionService(): AesEncryptionService {
        return AesEncryptionService()
    }
    
    @Provides
    @Singleton
    fun provideChaCha20EncryptionService(): ChaCha20EncryptionService {
        return ChaCha20EncryptionService()
    }
    
    // More providers...
}
```

## Component Hierarchy

Hilt provides Android-specific components:

```
Application
  └── SingletonComponent (Application scope)
        └── ActivityComponent (Activity scope)
              └── ViewModelComponent (ViewModel scope)
```

## Injection Points

### ViewModels

ViewModels are annotated with `@HiltViewModel`:

```kotlin
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    private val generateKeyCommand: AesGenerateAndSaveKeyCommandHandler,
    private val readAllKeysQuery: ReadAllKeysQueryHandler
) : ViewModel() {
    // ViewModel logic
}
```

### Activities

Activities are annotated with `@AndroidEntryPoint`:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Activity code
}
```

### Constructor Injection

Dependencies are injected via constructor:

```kotlin
class AesGenerateAndSaveKeyCommandHandler @Inject constructor(
    private val encryptionService: AesEncryptionService,
    private val keyStorage: KeyCommandGateway
) {
    // Handler logic
}
```

## Scopes

### Singleton Scope

Services and use cases are provided as singletons:

```kotlin
@Provides
@Singleton
fun provideAesEncryptionService(): AesEncryptionService {
    return AesEncryptionService()
}
```

### ViewModel Scope

ViewModels are scoped to their lifecycle:

```kotlin
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    // Dependencies injected here
) : ViewModel()
```

## Provided Dependencies

### Domain Services

```kotlin
@Provides
@Singleton
fun provideAesEncryptionService(): AesEncryptionService

@Provides
@Singleton
fun provideChaCha20EncryptionService(): ChaCha20EncryptionService

@Provides
@Singleton
fun provideTextService(): TextService
```

### Command Handlers

```kotlin
// Automatically injected via constructor
class AesGenerateAndSaveKeyCommandHandler @Inject constructor(...)
class ChaCha20GenerateAndSaveKeyCommandHandler @Inject constructor(...)
class DeleteKeyCommandHandler @Inject constructor(...)
// ... more handlers
```

### Query Handlers

```kotlin
// Automatically injected via constructor
class ReadAllKeysQueryHandler @Inject constructor(...)
class ReadKeyByIdQueryHandler @Inject constructor(...)
// ... more handlers
```

### Infrastructure Adapters

```kotlin
// Automatically injected via constructor
class KeyCommandGatewayAdapter @Inject constructor(
    private val context: Context
) : KeyCommandGateway

class KeyQueryGatewayAdapter @Inject constructor(
    private val context: Context
) : KeyQueryGateway
```

## Context Injection

Android `Context` is provided by Hilt:

```kotlin
class KeyCommandGatewayAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : KeyCommandGateway {
    // Use context for SharedPreferences, etc.
}
```

## Testing

Hilt provides test-specific components for testing:

```kotlin
@HiltAndroidTest
class KeyGenerationViewModelTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Before
    fun init() {
        hiltRule.inject()
    }
    
    // Test code
}
```

## Benefits

1. **Reduced Boilerplate**: Less manual dependency management
2. **Type Safety**: Compile-time dependency checking
3. **Testability**: Easy to provide test doubles
4. **Lifecycle Awareness**: Automatic component lifecycle management
5. **Scalability**: Easy to add new dependencies

## Best Practices

1. **Use Constructor Injection**: Prefer constructor injection over field injection
2. **Scope Appropriately**: Use singleton for stateless services
3. **Avoid Circular Dependencies**: Design dependencies carefully
4. **Provide Interfaces**: Inject interfaces, not concrete classes
5. **Test with Test Doubles**: Use Hilt's test components

## Learn More

- [Clean Architecture](clean-architecture.md) - Overall architecture
- [Application Layer](../layers/application.md) - How DI is used in application layer
- [Hilt Documentation](https://dagger.dev/hilt/) - Official Hilt documentation

