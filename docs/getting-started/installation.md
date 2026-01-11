# Installation

This guide covers the prerequisites and installation steps for the Cryptographer Android application.

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 17 or later
- **Android SDK**: 
  - Minimum SDK: 33
  - Target SDK: 36
  - Compile SDK: 36
- **Git**: For cloning the repository

## Installation Steps

### 1. Clone the Repository

```bash
git clone https://github.com/C3EQUALZz/cryptographer
cd cryptographer
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the cloned `cryptographer` directory
4. Click **OK**

### 3. Sync Gradle Files

Android Studio will automatically detect the Gradle project and prompt you to sync. If not:

1. Click **File → Sync Project with Gradle Files**
2. Wait for the sync to complete

### 4. Install Dependencies

Gradle will automatically download all required dependencies during the sync process. This includes:

- Kotlin standard library
- Jetpack Compose libraries
- Material Design 3 components
- Hilt dependency injection
- Testing libraries

### 5. Run the Application

1. Connect an Android device or start an emulator
2. Click the **Run** button (▶️) or press `Shift + F10`
3. Select your target device
4. Wait for the app to build and install

## Verifying Installation

After installation, you should be able to:

- ✅ Build the project without errors
- ✅ Run the app on an emulator or device
- ✅ See the main screen with navigation drawer
- ✅ Access all three main features: Key Generation, Encryption, and Encoding

## Troubleshooting

### Gradle Sync Issues

If you encounter Gradle sync errors:

1. **Invalidate Caches**: File → Invalidate Caches → Invalidate and Restart
2. **Clean Build**: Build → Clean Project, then Build → Rebuild Project
3. **Check JDK**: File → Project Structure → SDK Location → Ensure JDK 17+ is selected

### Build Errors

Common build errors and solutions:

- **SDK not found**: Install required SDK versions via SDK Manager
- **Kotlin version mismatch**: Ensure Kotlin version matches `gradle/libs.versions.toml`
- **Dependency conflicts**: Run `./gradlew --refresh-dependencies`

### Emulator Issues

If the emulator doesn't start:

1. Ensure **Android Emulator** is installed via SDK Manager
2. Create a new AVD (Android Virtual Device) with API 33+
3. Enable **Hardware Acceleration** in AVD settings

## Next Steps

- Learn about [Building the Project](building.md)
- Configure the project settings in [Configuration](configuration.md)
- Explore the [Architecture](../architecture/overview.md)
