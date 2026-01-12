# Presentation Layer

The Presentation layer contains the UI components and state management. It uses Jetpack Compose for the UI and ViewModels for state management.

## Location

```
presentation/
├── key/               # Key generation screen
├── encryption/        # Encryption/decryption screen
├── encoding/          # Encoding conversion screen
├── main/              # Main navigation
└── common/            # Shared UI components
```

## Principles

- ✅ **UI only**: Contains only UI-related code
- ✅ **State management**: Uses ViewModels and StateFlow
- ✅ **Compose**: Uses Jetpack Compose for UI
- ✅ **Reactive**: UI reacts to state changes

## ViewModels

ViewModels manage UI state and coordinate with application layer:

### KeyGenerationViewModel

```kotlin
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    private val generateKeyCommand: AesGenerateAndSaveKeyCommandHandler,
    private val readAllKeysQuery: ReadAllKeysQueryHandler,
    private val deleteKeyCommand: DeleteKeyCommandHandler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(KeyGenerationUiState())
    val uiState: StateFlow<KeyGenerationUiState> = _uiState.asStateFlow()
    
    fun generateKey(algorithm: EncryptionAlgorithm) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            generateKeyCommand.handle(
                AesGenerateAndSaveKeyCommand(algorithm)
            ).fold(
                onSuccess = { keyView ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedKey = keyView,
                        error = null
                    )
                    loadKeys()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun loadKeys() {
        viewModelScope.launch {
            readAllKeysQuery.handle(ReadAllKeysQuery())
                .fold(
                    onSuccess = { keys ->
                        _uiState.value = _uiState.value.copy(savedKeys = keys)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
                )
        }
    }
}
```

### UI State

```kotlin
data class KeyGenerationUiState(
    val isLoading: Boolean = false,
    val generatedKey: KeyView? = null,
    val savedKeys: List<KeyView> = emptyList(),
    val error: String? = null
)
```

## Compose Screens

### KeyGenerationScreen

```kotlin
@Composable
fun KeyGenerationScreen(viewModel: KeyGenerationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        KeyGenerationScreenTitle()
        
        AlgorithmSelectionCard(
            selectedAlgorithm = uiState.selectedAlgorithm,
            onAlgorithmSelected = { /* ... */ }
        )
        
        GenerateKeyButton(
            isLoading = uiState.isLoading,
            onClick = { viewModel.generateKey(uiState.selectedAlgorithm) }
        )
        
        uiState.generatedKey?.let { key ->
            GeneratedKeyCard(key = key)
        }
        
        SavedKeysSection(keys = uiState.savedKeys)
        
        uiState.error?.let { error ->
            KeyGenerationErrorCard(error = error)
        }
    }
}
```

## UI Components

Reusable Compose components:

### GeneratedKeyCard

```kotlin
@Composable
fun GeneratedKeyCard(
    key: KeyView,
    clipboard: Clipboard
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Generated Key",
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = key.keyBase64,
                style = MaterialTheme.typography.bodyMedium
            )
            
            OutlinedButton(
                onClick = {
                    // Copy to clipboard
                }
            ) {
                Text("Copy")
            }
        }
    }
}
```

## Navigation

Navigation between screens:

```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "key_generation"
    ) {
        composable("key_generation") {
            KeyGenerationScreen(viewModel = hiltViewModel())
        }
        composable("encryption") {
            EncryptionScreen(viewModel = hiltViewModel())
        }
        composable("encoding") {
            EncodingScreen(viewModel = hiltViewModel())
        }
    }
}
```

## State Management

### StateFlow

ViewModels expose state via `StateFlow`:

```kotlin
val uiState: StateFlow<KeyGenerationUiState> = _uiState.asStateFlow()
```

### Collecting State

UI collects state in Compose:

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

## Error Handling

Errors are displayed in UI:

```kotlin
uiState.error?.let { error ->
    KeyGenerationErrorCard(error = error)
}
```

## Learn More

- [Application Layer](application.md) - Commands and queries used
- [Clean Architecture](../clean-architecture.md) - Overall architecture
- [Dependency Injection](../dependency-injection.md) - How ViewModels are injected



