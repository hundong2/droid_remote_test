# droid_remote_test

android remote test tools with LLM

## Kotlin Web Service

This repository includes a Kotlin-based web service built with Ktor framework.

### Building

```bash
./gradlew build
```

### Running

```bash
./gradlew run
```

The service will start on port 8080 with the following endpoints:
- `GET /` - Root endpoint returning service status
- `GET /health` - Health check endpoint
- `GET /api/status` - JSON status endpoint

### Testing

```bash
./gradlew test
```

## CI/CD

The project includes a GitHub Actions workflow that automatically builds and tests the Kotlin web service on every push or pull request to main/master/develop branches.

See [.github/workflows/build.yml](.github/workflows/build.yml) for the workflow configuration.
