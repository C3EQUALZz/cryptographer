# Infrastructure Layer

The Infrastructure layer contains adapters that connect the domain to external systems (Android, storage, etc.). It implements the interfaces (ports) defined in the application layer.

## Location

```
infrastructure/
├── key/               # Key storage adapters
├── settings/          # Settings storage adapters
└── text/              # Text ID generation
```

## Principles

- ✅ **Implements ports**: Implements interfaces from application layer
- ✅ **Android-specific**: Contains Android framework code
- ✅ **Adapter pattern**: Adapts external systems to domain interfaces
- ✅ **Isolated**: Changes here don't affect domain logic

## Key Storage Adapters

### KeyCommandGatewayAdapter

Implements `KeyCommandGateway` interface:

```kotlin
class KeyCommandGatewayAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : KeyCommandGateway {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("encryption_keys", Context.MODE_PRIVATE)
    }
    
    override suspend fun save(key: EncryptionKey) {
        withContext(Dispatchers.IO) {
            val keyJson = key.toJson()
            prefs.edit()
                .putString(key.id.value, keyJson)
                .apply()
        }
    }
    
    override suspend fun delete(keyId: KeyId) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .remove(keyId.value)
                .apply()
        }
    }
    
    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .clear()
                .apply()
        }
    }
}
```

### KeyQueryGatewayAdapter

Implements `KeyQueryGateway` interface:

```kotlin
class KeyQueryGatewayAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : KeyQueryGateway {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("encryption_keys", Context.MODE_PRIVATE)
    }
    
    override suspend fun readAll(): List<EncryptionKey> {
        return withContext(Dispatchers.IO) {
            prefs.all.mapNotNull { (_, value) ->
                try {
                    (value as? String)?.let { json ->
                        EncryptionKey.fromJson(json)
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    override suspend fun readById(keyId: KeyId): EncryptionKey? {
        return withContext(Dispatchers.IO) {
            prefs.getString(keyId.value, null)?.let { json ->
                try {
                    EncryptionKey.fromJson(json)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
```

## Settings Storage Adapters

### SettingsCommandGatewayAdapter

```kotlin
class SettingsCommandGatewayAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsCommandGateway {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }
    
    override suspend fun saveLanguage(language: Language) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString("language", language.name)
                .apply()
        }
    }
    
    override suspend fun saveTheme(theme: ThemeMode) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString("theme", theme.name)
                .apply()
        }
    }
}
```

## ID Generation

### UuidTextIdGenerator

```kotlin
class UuidTextIdGenerator @Inject constructor() : TextIdGeneratorPort {
    override fun generate(): TextId {
        return TextId(UUID.randomUUID().toString())
    }
}
```

## Error Handling

Infrastructure errors:

```kotlin
sealed class InfrastructureError : AppError {
    data class StorageError(override val message: String) : InfrastructureError()
    data class SerializationError(override val message: String) : InfrastructureError()
    data class ConfigurationError(override val message: String) : InfrastructureError()
}
```

## Security Considerations

!!! warning "Current Implementation"
    Currently uses SharedPreferences for key storage. For production, consider:
    
    - **Android Keystore**: For secure key storage
    - **Encryption**: Encrypt keys before storing
    - **Access Control**: Implement proper access controls

## Future Improvements

### Android Keystore Integration

```kotlin
class KeyStoreKeyCommandGatewayAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : KeyCommandGateway {
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }
    
    override suspend fun save(key: EncryptionKey) {
        // Store key in Android Keystore
        // More secure than SharedPreferences
    }
}
```

## Testing

Infrastructure adapters can be tested with Android test framework:

```kotlin
@RunWith(AndroidJUnit4::class)
class KeyCommandGatewayAdapterTest {
    
    @Test
    fun `save key stores in SharedPreferences`() {
        // Test implementation
    }
}
```

## Learn More

- [Application Layer](application.md) - Ports that are implemented
- [Domain Layer](domain.md) - Domain entities used
- [Clean Architecture](../clean-architecture.md) - Overall architecture



