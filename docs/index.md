# Cryptographer Documentation

Welcome to the documentation for **Cryptographer**, a modern Android application for text encryption, decryption, and encoding conversion.

## Project Overview

Cryptographer is an Android application that provides secure text encryption and encoding conversion capabilities. Built using Clean Architecture principles and Domain-Driven Design (DDD), the application emphasizes modularity, testability, and maintainability.

### Key Features

- **🔑 Key Generation**: Generate encryption keys for AES-128, AES-192, AES-256, and ChaCha20-256 algorithms
- **🔐 Text Encryption**: Encrypt plain text using AES or ChaCha20 with secure random IV generation
- **🔓 Text Decryption**: Decrypt encrypted text with support for custom IV (Initialization Vector)
- **📝 Encoding Conversion**: Convert text between UTF-8, ASCII, and BASE64 encodings
- **💾 Key Management**: Save, view, and delete encryption keys locally
- **📋 Clipboard Integration**: Copy encrypted/decrypted text and keys to clipboard
- **🌍 Multi-language Support**: Internationalization support for multiple languages
- **🎨 Theme Support**: Light and dark theme modes

### Tech Stack

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI framework
- **Material Design 3** - Material You design system
- **Hilt** - Dependency injection framework
- **Coroutines + Flow** - Asynchronous programming and reactive streams
- **Clean Architecture** - Separation of concerns across layers
- **CQRS** - Command Query Responsibility Segregation
- **DDD** - Domain-Driven Design principles

## Getting Started

To start using Cryptographer, follow the [Installation Guide](getting-started/installation.md) to set up the development environment and build the project.

## Architecture

The application follows **Clean Architecture** principles with clear separation between:

- **Domain Layer**: Pure business logic with no Android dependencies
- **Application Layer**: Use cases and orchestration (CQRS pattern)
- **Infrastructure Layer**: External adapters (storage, ID generation)
- **Presentation Layer**: UI components and state management

Learn more in the [Architecture Overview](architecture/overview.md).

## Repository

- **Source**: [GitHub](https://github.com/C3EQUALZz/cryptographer)
- **License**: [MIT](license.md)
- **Author**: Danil Kovalev ([GitHub](https://github.com/C3EQUALZz))

---

Explore the documentation to learn about the [Architecture](architecture/overview.md), [Features](features/key-generation.md), and [Development](development/setup.md) processes.
