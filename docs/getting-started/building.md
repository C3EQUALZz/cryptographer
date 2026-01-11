# Building the Project

This guide covers how to build the Cryptographer application for different environments.

## Build Types

The project supports two build types:

- **Debug**: Development build with debugging enabled
- **Release**: Production build with optimizations

## Building from Command Line

### Debug Build

```bash
./gradlew assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release Build

```bash
./gradlew assembleRelease
```

The APK will be generated at:
```
app/build/outputs/apk/release/app-release.apk
```

!!! warning "Signing Required"
    Release builds require a signing configuration. See [Configuration](configuration.md) for details.

## Building from Android Studio

1. Select **Build → Make Project** (or press `Ctrl + F9`)
2. For APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
3. Wait for the build to complete

## Build Variants

The project currently has a single build variant. To check available variants:

```bash
./gradlew tasks --all | grep assemble
```

## Running Tests

### Unit Tests

```bash
./gradlew test
```

Test reports are available at:
```
app/build/reports/tests/test/index.html
```

### Android Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

Requires a connected device or running emulator.

## Code Quality Checks

### Format Code

```bash
./gradlew spotlessApply
```

### Check Formatting

```bash
./gradlew spotlessCheck
```

### Static Analysis

```bash
./gradlew detekt
```

Detekt reports are available at:
```
app/build/reports/detekt/detekt.html
```

### Run All Checks

```bash
./gradlew check
```

This runs:
- Spotless formatting check
- Detekt static analysis
- Unit tests

## Build Configuration

Key build configuration files:

- **Project-level**: `build.gradle.kts`
- **App-level**: `app/build.gradle.kts`
- **Dependencies**: `gradle/libs.versions.toml`
- **Properties**: `gradle.properties`

## Build Optimization

### Enable Build Cache

The project uses Gradle build cache by default. To verify:

```bash
./gradlew build --build-cache
```

### Parallel Execution

Gradle runs tasks in parallel by default. To control:

```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=4
```

### Daemon Configuration

Gradle daemon improves build performance:

```properties
# gradle.properties
org.gradle.daemon=true
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

## Troubleshooting

### Build Fails with Out of Memory

Increase heap size in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

### Slow Build Times

1. Enable build cache
2. Use Gradle daemon
3. Increase worker count
4. Use `--no-daemon` only when necessary

### Dependency Resolution Issues

```bash
# Refresh dependencies
./gradlew --refresh-dependencies

# Clean and rebuild
./gradlew clean build
```

## Next Steps

- Learn about [Configuration](configuration.md)
- Explore [Development Setup](../development/setup.md)
- Check [CI/CD](../development/ci-cd.md) for automated builds

