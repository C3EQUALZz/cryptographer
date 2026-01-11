# Contributing

Contributions to the Cryptographer project are welcome! This guide outlines how to contribute.

## Getting Started

1. **Fork the Repository**:
   Fork [C3EQUALZz/cryptographer](https://github.com/C3EQUALZz/cryptographer).

2. **Clone Your Fork**:
   ```bash
   git clone https://github.com/<your_username>/cryptographer
   cd cryptographer
   ```

3. **Set Up Development Environment**:
   - Open project in Android Studio
   - Sync Gradle files
   - Install git hooks: `./gradlew installGitHooks`

## Making Changes

### Create a Branch

```bash
git checkout -b feature/your-feature
```

### Follow Coding Standards

- Use Spotless for formatting (`./gradlew spotlessApply`)
- Run Detekt for static analysis (`./gradlew detekt`)
- Write tests for new functionality (`./gradlew test`)
- Follow Clean Architecture principles

### Commit Messages

Use conventional commits:

- **Example**: `feat: add new encryption algorithm`
- **Types**: 
  - `feat`: New feature
  - `fix`: Bug fix
  - `docs`: Documentation changes
  - `chore`: Maintenance tasks
  - `style`: Code style changes
  - `refactor`: Code refactoring
  - `test`: Test additions/changes
  - `build`: Build system changes

### Run Pre-Commit Checks

If git hooks are installed, they run automatically. To run manually:

```bash
./gradlew spotlessCheck detekt
```

## Code Quality

Before submitting:

1. **Format Code**:
   ```bash
   ./gradlew spotlessApply
   ```

2. **Run Static Analysis**:
   ```bash
   ./gradlew detekt
   ```

3. **Run Tests**:
   ```bash
   ./gradlew test
   ```

4. **Run All Checks**:
   ```bash
   ./gradlew check
   ```

## Architecture Guidelines

### Follow Clean Architecture

- **Domain Layer**: Pure business logic, no Android dependencies
- **Application Layer**: Use cases and orchestration (CQRS)
- **Infrastructure Layer**: Adapters for external systems
- **Presentation Layer**: UI components and ViewModels

### Use CQRS Pattern

- **Commands**: For write operations
- **Queries**: For read operations
- **Views**: DTOs for presentation

### Dependency Injection

- Use Hilt for dependency injection
- Annotate ViewModels with `@HiltViewModel`
- Provide services as singletons when appropriate

## Submitting Changes

### Push Changes

```bash
git push origin feature/your-feature
```

### Create a Merge Request

- Target the `main` or `develop` branch
- Describe the changes clearly
- Reference any related issues
- Ensure all CI checks pass

### CI Checks

The merge request will trigger GitLab CI to run:

- Code formatting check (Spotless)
- Static analysis (Detekt)
- Unit tests
- Build verification

Ensure all checks pass before requesting review.

## Code Review

- Respond to feedback promptly
- Make necessary changes and push updates to the same branch
- Keep commits focused and atomic
- Squash commits if requested

## Reporting Issues

Report bugs or suggest features via:

- **GitLab Issues**: [Create an issue](https://github.com/C3EQUALZz/cryptographer/issues)
- Include:
  - Clear description
  - Steps to reproduce (for bugs)
  - Expected vs actual behavior
  - Environment details (Android version, device, etc.)

## Feature Requests

When proposing new features:

1. Check if the feature aligns with project goals
2. Describe the use case
3. Consider architecture impact
4. Propose implementation approach

## Questions?

- **Author**: Danil Kovalev ([GitHub](https://github.com/C3EQUALZz))
- Check [Documentation](../index.md) for more information

Thank you for contributing! 🎉
