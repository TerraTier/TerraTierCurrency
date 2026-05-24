# Contributing

Thanks for helping improve TerraTierCurrency.

## Development

- Use Java 21.
- Build with `.\gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS.
- Keep generated files out of commits (`build/`, `.gradle/`, `bin/`, and jars).
- Keep public API changes backward-compatible when possible.

## Pull Requests

- Describe the behavior change clearly.
- Include any new commands, permissions, placeholders, or config options in `README.md`.
- Run a local build before opening a PR.

## Server Testing

For features touching WorldGuard, PlaceholderAPI, or SimpleScore, test with those plugins installed on a Paper server before release.
