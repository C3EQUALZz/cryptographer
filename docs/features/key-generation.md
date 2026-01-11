# Key Generation

The Key Generation feature allows users to generate encryption keys for various algorithms and manage them locally.

## Supported Algorithms

- **AES-128**: 128-bit Advanced Encryption Standard
- **AES-192**: 192-bit Advanced Encryption Standard
- **AES-256**: 256-bit Advanced Encryption Standard
- **ChaCha20-256**: 256-bit ChaCha20 stream cipher

## Features

### Generate Key

1. Select an encryption algorithm
2. Click "Generate Key" button
3. Key is generated using secure random number generation
4. Generated key is displayed in Base64 format

### Save Key

Generated keys are automatically saved to local storage (SharedPreferences).

### View Saved Keys

All saved keys are displayed in a list showing:
- Key ID (first 8 characters)
- Algorithm type
- Full key (Base64 encoded)

### Delete Key

- Delete individual keys
- Delete all keys at once

### Copy to Clipboard

Copy generated or saved keys to clipboard for easy sharing.

## Usage

### Generating a Key

```kotlin
// In ViewModel
fun generateKey(algorithm: EncryptionAlgorithm) {
    viewModelScope.launch {
        generateKeyCommand.handle(
            AesGenerateAndSaveKeyCommand(algorithm)
        ).fold(
            onSuccess = { keyView ->
                // Update UI state
            },
            onFailure = { error ->
                // Handle error
            }
        )
    }
}
```

### Loading Saved Keys

```kotlin
fun loadKeys() {
    viewModelScope.launch {
        readAllKeysQuery.handle(ReadAllKeysQuery())
            .fold(
                onSuccess = { keys ->
                    // Update UI state
                },
                onFailure = { error ->
                    // Handle error
                }
            )
    }
}
```

## Security Considerations

!!! warning "Key Storage"
    Currently, keys are stored in SharedPreferences. For production use:
    
    - Implement Android Keystore for secure storage
    - Encrypt keys before storing
    - Implement proper access controls

## UI Components

- **AlgorithmSelectionCard**: Select encryption algorithm
- **GenerateKeyButton**: Trigger key generation
- **GeneratedKeyCard**: Display generated key
- **SavedKeysSection**: List of saved keys
- **DeleteAllKeysDialog**: Confirm deletion

## Learn More

- [Encryption & Decryption](encryption.md) - Using generated keys
- [Architecture - Domain Layer](architecture/layers/domain.md) - Key generation logic
- [Architecture - Application Layer](architecture/layers/application.md) - Command handlers

