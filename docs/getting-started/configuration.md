# Configuration

This guide covers the configuration options available in the Cryptographer application.

## Build Configuration

### Version Catalog

The project uses Gradle's version catalog for dependency management:

**Location**: `gradle/libs.versions.toml`

This file centralizes all dependency versions and plugin versions, making it easy to update dependencies across the project.

### Key Configuration Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Project-level build configuration |
| `app/build.gradle.kts` | App-level build configuration |
| `gradle.properties` | Gradle properties and settings |
| `detekt.yml` | Detekt static analysis configuration |
| `proguard-rules.pro` | ProGuard rules for release builds |

## Application Configuration

### SDK Versions

Configured in `app/build.gradle.kts`:

```kotlin
android {
    compileSdk {
        version = release(36)
    }
    
    defaultConfig {
        minSdk = 33
        targetSdk = 36
    }
}
```

### Build Types

#### Debug

- Debugging enabled
- No code obfuscation
- Includes debug symbols

#### Release

- Code obfuscation (ProGuard) - currently disabled
- Optimizations enabled
- Requires signing configuration

### Code Quality Configuration

#### Spotless (Code Formatting)

Configured in `app/build.gradle.kts`:

- **Kotlin files**: Uses ktlint with custom rules
- **Gradle files**: ktlint formatting
- **XML files**: 4-space indentation

#### Detekt (Static Analysis)

Configuration file: `detekt.yml`

- Custom rules configuration
- Baseline file: `detekt-baseline.xml`
- JVM target: 17

## Dependency Injection

### Hilt Configuration

**Module**: `setup/ioc/AppModule.kt`

Provides:
- Encryption services (AES, ChaCha20)
- Use cases
- Storage adapters

### ViewModel Injection

ViewModels are annotated with `@HiltViewModel`:

```kotlin
@HiltViewModel
class KeyGenerationViewModel @Inject constructor(
    // Dependencies injected automatically
) : ViewModel()
```

## Theme Configuration

### Color Scheme

**Location**: `setup/configs/theme/Color.kt`

Defines Material Design 3 color palette:
- Primary colors
- Secondary colors
- Surface colors
- Error colors

### Typography

**Location**: `setup/configs/theme/Type.kt`

Defines text styles:
- Display styles
- Headline styles
- Body styles
- Label styles

## Internationalization

### Supported Languages

Configured in `setup/i18n/LocaleHelper.kt`

Currently supports:
- English (default)
- Russian

### Adding New Languages

1. Create `values-<locale>/strings.xml` in `app/src/main/res/`
2. Add translations
3. Update `LocaleHelper.kt` if needed

## Storage Configuration

### Key Storage

Currently uses SharedPreferences (development).

!!! warning "Production Recommendation"
    For production, implement Android Keystore for secure key storage.

### Settings Storage

User preferences stored via SharedPreferences:
- Theme mode (light/dark)
- Language preference

## ProGuard Rules

**Location**: `app/proguard-rules.pro`

Currently minimal rules. For release builds, add:
- Keep rules for reflection
- Keep rules for serialization
- Optimization rules

## Git Hooks

Install git hooks for pre-commit checks:

```bash
./gradlew installGitHooks
```

Hooks run:
- Spotless formatting check
- Detekt static analysis

## Environment-Specific Configuration

### Local Development

No special configuration needed. Uses default debug build.

### CI/CD

GitLab CI configuration (`.gitlab-ci.yml`):
- Uses Docker image with Android SDK
- Caches Gradle dependencies
- Runs tests and builds

## Customization

### Changing App Name

Edit `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">Your App Name</string>
```

### Changing Package Name

1. Update `namespace` in `app/build.gradle.kts`
2. Refactor package structure
3. Update `AndroidManifest.xml`

### Adding New Dependencies

1. Add to `gradle/libs.versions.toml`
2. Reference in `app/build.gradle.kts`
3. Sync Gradle

## Next Steps

- Learn about [Architecture](../architecture/overview.md)
- Explore [Development Setup](../development/setup.md)
- Check [Code Quality](../development/code-quality.md) tools
