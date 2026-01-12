# CI/CD

The Cryptographer project uses GitLab CI for continuous integration and continuous deployment.

## Configuration

**File**: `.gitlab-ci.yml`

## Pipeline Stages

### 1. Validate Stage

Runs code quality checks:

- **spotless_check**: Code formatting check
- **detekt_check**: Static code analysis
- **check_all**: Combined validation

### 2. Test Stage

Runs automated tests:

- **unit_tests**: Unit test execution

### 3. Build Stage

Builds the application:

- **build_debug**: Debug APK generation
- **build_release**: Release APK generation

## Pipeline Flow

```
┌─────────────┐
│   Validate  │ → Code formatting, static analysis
└──────┬──────┘
       │
┌──────▼──────┐
│    Test     │ → Unit tests
└──────┬──────┘
       │
┌──────▼──────┐
│    Build    │ → APK generation
└─────────────┘
```

## Jobs

### spotless_check

```yaml
spotless_check:
  stage: validate
  script:
    - ./gradlew spotlessCheck --no-daemon
  only:
    - merge_requests
    - main
    - develop
```

### detekt_check

```yaml
detekt_check:
  stage: validate
  script:
    - ./gradlew detekt --no-daemon
  only:
    - merge_requests
    - main
    - develop
```

### unit_tests

```yaml
unit_tests:
  stage: test
  script:
    - ./gradlew test --no-daemon
  artifacts:
    reports:
      junit: app/build/test-results/test/TEST-*.xml
    paths:
      - app/build/reports/tests/
    expire_in: 1 week
```

### build_debug

```yaml
build_debug:
  stage: build
  script:
    - ./gradlew assembleDebug --no-daemon
  artifacts:
    paths:
      - app/build/outputs/apk/debug/*.apk
    expire_in: 1 week
```

### build_release

```yaml
build_release:
  stage: build
  script:
    - ./gradlew assembleRelease --no-daemon
  artifacts:
    paths:
      - app/build/outputs/apk/release/*.apk
    expire_in: 1 month
  only:
    - main
    - tags
```

## Docker Image

Uses `jangrewe/gitlab-ci-android:latest` which includes:

- Android SDK
- Gradle
- Required build tools

## Caching

Gradle dependencies are cached:

```yaml
cache:
  key:
    files:
      - gradle/wrapper/gradle-wrapper.properties
      - gradle/libs.versions.toml
  paths:
    - .gradle
    - build
    - app/build
  policy: pull-push
```

## Artifacts

### Test Reports

- JUnit XML reports
- HTML test reports
- Expire after 1 week

### APKs

- Debug APK: Available for all branches
- Release APK: Only for main and tags
- Expire after 1 week (debug) or 1 month (release)

## Triggers

### Automatic Triggers

- Push to `main` branch
- Push to `develop` branch
- Merge requests
- Tags

### Manual Triggers

Can be triggered manually from GitLab UI.

## Viewing Results

### Pipeline Status

View in GitLab: **CI/CD → Pipelines**

### Job Logs

Click on any job to view logs.

### Artifacts

Download artifacts from job pages.

## Troubleshooting

### Build Failures

1. Check job logs for errors
2. Verify Android SDK version
3. Check dependency resolution

### Test Failures

1. View test reports
2. Check for flaky tests
3. Verify test environment

### Cache Issues

Clear cache if builds fail:

```yaml
# Add to job
cache: {}
```

## Learn More

- [Development Setup](setup.md) - Local development
- [Code Quality](code-quality.md) - Quality checks
- [Testing](testing.md) - Test strategies



