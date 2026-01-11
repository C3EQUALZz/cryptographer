# Development Setup

This guide covers setting up the development environment for the Cryptographer project.

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 17 or later
- **Git**: For version control
- **Android SDK**: API 33+ installed

## Initial Setup

### 1. Clone Repository

```bash
git clone https://github.com/C3EQUALZz/cryptographer
cd cryptographer
```

### 2. Open in Android Studio

1. Launch Android Studio
2. **File → Open**
3. Select the `cryptographer` directory
4. Click **OK**

### 3. Sync Gradle

Android Studio will automatically sync Gradle. If not:

1. **File → Sync Project with Gradle Files**
2. Wait for sync to complete

### 4. Install Git Hooks (Optional)

```bash
./gradlew installGitHooks
```

This installs pre-commit hooks that run:
- Spotless formatting check
- Detekt static analysis

## Project Structure

```
cryptographer/
├── app/                    # Application module
│   ├── src/
│   │   ├── main/          # Main source code
│   │   ├── test/          # Unit tests
│   │   └── androidTest/   # Instrumented tests
│   └── build.gradle.kts   # App build configuration
├── gradle/                 # Gradle configuration
│   └── libs.versions.toml # Version catalog
├── docs/                   # Documentation
├── build.gradle.kts        # Project build configuration
├── gradle.properties       # Gradle properties
└── mkdocs.yml             # Documentation configuration
```

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

- Write code following Clean Architecture principles
- Add tests for new functionality
- Ensure code passes quality checks

### 3. Run Quality Checks

```bash
# Format code
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Run static analysis
./gradlew detekt

# Run tests
./gradlew test
```

### 4. Commit Changes

```bash
git add .
git commit -m "feat: add new feature"
```

Pre-commit hooks will run automatically if installed.

### 5. Push and Create Merge Request

```bash
git push origin feature/your-feature-name
```

Create a merge request in GitLab.

## Code Style

### Kotlin Style Guide

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

### Formatting

Code is automatically formatted using Spotless (ktlint):

```bash
./gradlew spotlessApply
```

### Naming Conventions

- **Classes**: PascalCase (`KeyGenerationViewModel`)
- **Functions**: camelCase (`generateKey`)
- **Variables**: camelCase (`uiState`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_KEY_SIZE`)
- **Packages**: lowercase (`com.example.cryptographer`)

## Testing

### Unit Tests

```bash
./gradlew test
```

Test location: `app/src/test/`

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

Requires connected device or emulator.

Test location: `app/src/androidTest/`

## Debugging

### Run in Debug Mode

1. Click **Run** button (▶️)
2. Select **Debug** configuration
3. Set breakpoints as needed

### View Logs

Use Android Studio's Logcat to view application logs.

## Common Tasks

### Add New Dependency

1. Add to `gradle/libs.versions.toml`:

```toml
[libraries]
new-library = { group = "com.example", name = "library", version.ref = "version" }
```

2. Reference in `app/build.gradle.kts`:

```kotlin
implementation(libs.new.library)
```

3. Sync Gradle

### Add New Screen

1. Create screen in `presentation/`
2. Create ViewModel with `@HiltViewModel`
3. Add navigation route
4. Add UI components

### Add New Feature

1. Define domain entities/value objects
2. Create domain services
3. Add command/query handlers
4. Create ViewModel
5. Build UI with Compose

## Troubleshooting

### Gradle Sync Fails

1. **Invalidate Caches**: File → Invalidate Caches → Invalidate and Restart
2. **Clean Build**: Build → Clean Project
3. **Check JDK**: File → Project Structure → SDK Location

### Build Errors

1. **SDK Issues**: Install required SDK via SDK Manager
2. **Dependency Conflicts**: Run `./gradlew --refresh-dependencies`
3. **Kotlin Version**: Check `gradle/libs.versions.toml`

### Test Failures

1. Check test output in `app/build/reports/tests/`
2. Run tests individually to isolate issues
3. Check test dependencies

## Learn More

- [Code Quality](code-quality.md) - Quality tools and checks
- [Testing](testing.md) - Testing strategies
- [CI/CD](ci-cd.md) - Continuous Integration

