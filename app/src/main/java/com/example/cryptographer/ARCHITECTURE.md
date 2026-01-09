# Clean Architecture Structure

This project follows Clean Architecture principles with the following layers:

## Domain Layer
**Location:** `domain/`

Pure business logic, no Android dependencies.

- **Entities:** Business objects (`Text`, `EncryptionKey`, `EncryptedText`)
- **Use Cases:** Business operations (`GenerateEncryptionKeyUseCase`, `EncryptTextUseCase`, etc.)
- **Services:** Domain services (`AesEncryptionService`)
- **Repository Interfaces:** Contracts for data operations (`TextRepository`)

## Infrastructure Layer
**Location:** `infrastructure/`

Adapters that connect domain to external systems (Android, databases, etc.).

- **KeyStorageAdapter:** Stores encryption keys using SharedPreferences
  - Note: In production, consider using Android Keystore for better security

## Presentation Layer
**Location:** `presentation/`

UI and ViewModels for user interaction.

- **ViewModels:** State management and business logic coordination
  - `KeyGenerationViewModel`: Manages key generation and storage
- **Screens:** Compose UI components
  - `KeyGenerationScreen`: UI for generating and viewing keys

## Dependency Injection

Using **Hilt** (built on Dagger) for dependency injection.

### Setup:
1. **Application Class:** `CryptographerApplication` annotated with `@HiltAndroidApp`
2. **DI Module:** `di/AppModule.kt` provides domain services and use cases
3. **ViewModels:** Annotated with `@HiltViewModel` for automatic injection
4. **Activities:** Annotated with `@AndroidEntryPoint` for Hilt support

### Key Dependencies:
- `AesEncryptionService` → Provided as Singleton
- `GenerateEncryptionKeyUseCase` → Provided as Singleton
- `KeyStorageAdapter` → Auto-injected via `@Inject` constructor

## Current Features

✅ Key Generation
- Generate AES-128, AES-192, AES-256 keys
- Save generated keys locally
- View saved keys
- Delete saved keys
- Copy key to clipboard

## Next Steps

- [ ] Add encryption/decryption UI
- [ ] Implement Android Keystore for secure key storage
- [ ] Add navigation between screens
- [ ] Add data layer with Room database for text storage
- [ ] Implement other encryption algorithms (RSA, ChaCha20, etc.)

