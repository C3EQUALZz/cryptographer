# Encryption & Decryption

The Encryption & Decryption feature allows users to encrypt and decrypt text using generated encryption keys.

## Supported Algorithms

- **AES**: Advanced Encryption Standard (128, 192, 256-bit)
- **ChaCha20**: ChaCha20 stream cipher (256-bit)

## Features

### Encryption

1. Select a saved encryption key
2. Enter plain text to encrypt
3. Click "Encrypt" button
4. Encrypted text is displayed in Base64 format
5. IV (Initialization Vector) is generated automatically and displayed

### Decryption

1. Select the same encryption key used for encryption
2. Enter encrypted text (Base64 format)
3. Enter IV (if custom IV was used)
4. Click "Decrypt" button
5. Decrypted plain text is displayed

### IV Management

- **Automatic IV**: Generated automatically during encryption
- **Custom IV**: Can be provided for decryption
- **IV Display**: IV is shown in Base64 format for easy copying

## Usage

### Encrypting Text

```kotlin
// In ViewModel
fun encryptText() {
    viewModelScope.launch {
        encryptCommand.handle(
            AesEncryptTextCommand(
                text = uiState.inputText,
                keyId = uiState.selectedKeyId,
                algorithm = uiState.selectedAlgorithm
            )
        ).fold(
            onSuccess = { encryptedView ->
                // Update UI with encrypted text and IV
            },
            onFailure = { error ->
                // Handle error
            }
        )
    }
}
```

### Decrypting Text

```kotlin
fun decryptText() {
    viewModelScope.launch {
        decryptCommand.handle(
            AesDecryptTextCommand(
                encryptedText = uiState.encryptedTextInput,
                iv = uiState.ivInput,
                keyId = uiState.selectedKeyId,
                algorithm = uiState.selectedAlgorithm
            )
        ).fold(
            onSuccess = { decryptedView ->
                // Update UI with decrypted text
            },
            onFailure = { error ->
                // Handle error
            }
        )
    }
}
```

## Encryption Process

1. **Key Selection**: User selects a saved encryption key
2. **Text Input**: User enters plain text
3. **IV Generation**: Secure random IV is generated
4. **Encryption**: Text is encrypted using selected algorithm
5. **Output**: Encrypted text and IV are displayed

## Decryption Process

1. **Key Selection**: User selects the same key used for encryption
2. **Encrypted Text Input**: User enters encrypted text (Base64)
3. **IV Input**: User enters IV (if custom IV was used)
4. **Decryption**: Text is decrypted using selected algorithm
5. **Output**: Decrypted plain text is displayed

## Security Features

- **Secure Random IV**: Each encryption uses a unique IV
- **Key Validation**: Keys are validated before use
- **Error Handling**: Clear error messages for invalid inputs

## UI Components

- **KeySelectionSection**: Select encryption key
- **EncryptionSection**: Encrypt text input and results
- **DecryptionSection**: Decrypt text input and results
- **EncryptedResultCard**: Display encrypted text and IV
- **DecryptedResultCard**: Display decrypted text

## Error Handling

Common errors:

- **Key Not Found**: Selected key doesn't exist
- **Invalid Key**: Key format is incorrect
- **Decryption Failed**: Wrong key or corrupted data
- **Invalid Base64**: Encrypted text or IV format is invalid

## Learn More

- [Key Generation](key-generation.md) - Generate encryption keys
- [Architecture - Domain Layer](../architecture/layers/domain.md) - Encryption services
- [Architecture - Application Layer](../architecture/layers/application.md) - Command handlers

