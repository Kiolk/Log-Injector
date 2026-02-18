# Log Injector for IntelliJ IDEA

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/Kiolk/Log-Injector)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ-2024.3-orange.svg)](https://www.jetbrains.com/idea/)

An IntelliJ IDEA plugin that automatically inserts and removes logging statements in your Java and Kotlin code. Save time and improve debugging efficiency by adding comprehensive logging with just a few clicks.

## Features

### 🚀 Core Functionality
- **Automatic Log Insertion**: Insert logging statements into all methods of a class with a single action
- **Automatic Log Removal**: Remove all inserted logs from a class quickly and cleanly
- **Multi-Language Support**: Works with both Java and Kotlin files
- **Multiple Logging Frameworks**:
  - System.out.println (default)
  - Timber (Android logging library)
  - Napier (Kotlin Multiplatform logging library)
- **Automatic Import Management**: Imports are added when inserting logs and removed automatically when all logs for a framework are removed

### 📊 Tracking Options
- **Method Execution Tracking**: Automatically log method entry points
- **Assignment Tracking**: Log variable assignments for detailed debugging
- **Custom Log Tags**: Configure your own log tag/prefix for easy filtering

### 🎨 User Interface
- **Context Menu Actions**: Right-click in the editor to insert or remove logs
- **Tool Window**: Dedicated settings panel for configuring logging preferences
- **Real-time Configuration**: Change settings without restarting the IDE

## Installation

### From JetBrains Marketplace (Coming Soon)
1. Open IntelliJ IDEA
2. Go to `File` → `Settings` → `Plugins`
3. Search for "Log Injector"
4. Click `Install` and restart the IDE

### From Source
1. Clone this repository:
   ```bash
   git clone https://github.com/Kiolk/Log-Injector.git
   cd Log-Injector
   ```

2. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```

3. Install the plugin:
   - Go to `File` → `Settings` → `Plugins`
   - Click the gear icon ⚙️ and select `Install Plugin from Disk...`
   - Select the ZIP file from `build/distributions/`
   - Restart IntelliJ IDEA

## Usage

### Quick Start

1. **Open a Java or Kotlin file** in your project
2. **Right-click** anywhere in the editor
3. Choose one of the following actions:
   - **Insert Logs**: Adds logging statements to methods in the current class
   - **Remove Logs**: Removes all previously inserted logs from the current class

### Configuration

Access the plugin settings through the **LoggingOptions** tool window on the right side of the IDE:

- **Track Method Execution**: Enable/disable method entry logging
- **Track Assignments**: Enable/disable variable assignment logging
- **Log Tag**: Set a custom prefix for your log statements (default: "Myfancy log")
- **Logging Framework**: Choose between System.out.println, Timber, or Napier

### Examples

#### Before (Kotlin):
```kotlin
class UserManager {
    fun createUser(name: String, email: String): User {
        val user = User(name, email)
        saveToDatabase(user)
        return user
    }
}
```

#### After (with method tracking enabled):
```kotlin
class UserManager {
    fun createUser(name: String, email: String): User {
        println("Myfancy log: createUser()")
        val user = User(name, email)
        saveToDatabase(user)
        return user
    }
}
```

## Development

### Prerequisites

- **JDK 21** or higher
- **Gradle 8.x** (wrapper included)
- **IntelliJ IDEA 2024.3** or higher

### Setup Development Environment

1. Clone the repository:
   ```bash
   git clone https://github.com/Kiolk/Log-Injector.git
   cd Log-Injector
   ```

2. Open the project in IntelliJ IDEA:
   - `File` → `Open` → Select the project directory
   - IntelliJ will automatically import the Gradle project

3. Run the plugin in a development instance:
   ```bash
   ./gradlew runIde
   ```

### Project Structure

```
LogInjector/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/github/kiolk/loggingplugin/
│   │   │       ├── actions/          # Plugin actions (Insert/Remove)
│   │   │       ├── services/         # Core logic and strategies
│   │   │       ├── settings/         # Configuration management
│   │   │       └── toolwindow/       # UI components
│   │   └── resources/
│   │       └── META-INF/
│   │           └── plugin.xml        # Plugin configuration
│   └── test/
│       └── kotlin/                   # Unit tests
├── build.gradle.kts                  # Build configuration
└── README.md                         # This file
```

### Key Components

- **InsertLogsAction**: Handles the "Insert Logs" action
- **RemoveLogsAction**: Handles the "Remove Logs" action
- **LogInserterService**: Core service for inserting logs into PSI elements
- **LogStrategy**: Strategy pattern implementation for different logging frameworks
- **LoggingSettings**: Persistent configuration storage

## Contributing

We welcome contributions! Here's how you can help:

### Reporting Issues

1. Check if the issue already exists in the [Issues](https://github.com/Kiolk/Log-Injector/issues) section
2. If not, create a new issue with:
   - Clear description of the problem
   - Steps to reproduce
   - Expected vs actual behavior
   - IntelliJ IDEA version and plugin version
   - Sample code (if applicable)

### Submitting Pull Requests

1. **Fork the repository** and create your branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Follow the existing code style (enforced by ktlint)
   - Add tests for new functionality
   - Update documentation if needed

3. **Run tests** to ensure everything works:
   ```bash
   ./gradlew test
   ```

4. **Run code quality checks**:
   ```bash
   ./gradlew ktlintCheck
   ```

5. **Commit your changes** with a clear commit message:
   ```bash
   git commit -m "Add feature: description of your changes"
   ```

6. **Push to your fork** and create a Pull Request:
   ```bash
   git push origin feature/your-feature-name
   ```

7. In your pull request description:
   - Explain what changes you made and why
   - Reference any related issues
   - Include screenshots for UI changes

### Code Style Guidelines

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused
- Write unit tests for new features
- Ensure ktlint passes: `./gradlew ktlintFormat`

### Development Tips

- Use `./gradlew runIde` to test changes in a live IDE instance
- Check plugin logs: `Help` → `Show Log in Finder/Explorer`
- Use IntelliJ's PSI Viewer: `Tools` → `View PSI Structure`

## Testing

Run all tests:
```bash
./gradlew test
```

Run specific test:
```bash
./gradlew test --tests "LogInserterServiceTest"
```

## Building

Build the plugin distribution:
```bash
./gradlew buildPlugin
```

The plugin ZIP will be created in `build/distributions/`

## Publishing

To publish to JetBrains Marketplace:

1. Set up environment variables:
   ```bash
   export PUBLISH_TOKEN="your-marketplace-token"
   export CERTIFICATE_CHAIN="your-certificate"
   export PRIVATE_KEY="your-private-key"
   export PRIVATE_KEY_PASSWORD="your-password"
   ```

2. Publish:
   ```bash
   ./gradlew publishPlugin
   ```

## Compatibility

- **IntelliJ IDEA**: 2024.3 - 2025.3.*
- **Languages**: Java, Kotlin
- **JDK**: 21+

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Yauheni Slizh**
- GitHub: [@Kiolk](https://github.com/kiolk)
- Email: tyteishi@gmail.com

## Acknowledgments

- Built with [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- Inspired by the need for faster debugging workflows
- Thanks to all contributors!

## Changelog

### Version 1.0.0 (Current)
- Initial release
- Support for Java and Kotlin
- Method execution tracking
- Assignment tracking
- System.out.println and Timber frameworks
- Configurable log tags
- Tool window for settings

## Roadmap

Future features under consideration:
- [x] Napier logging framework support
- [ ] More logging framework support (Log4j, SLF4J, etc.)
- [ ] Custom log templates
- [ ] Smart log placement (avoid duplicates)
- [ ] Log level configuration
- [ ] Bulk operations across multiple files
- [ ] Integration with debugging tools

## Support

- **Issues**: [GitHub Issues](https://github.com/Kiolk/Log-Injector/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Kiolk/Log-Injector/discussions)
- **Documentation**: [Wiki](https://github.com/Kiolk/Log-Injector/wiki)

---

Made with ❤️ for developers who love clean code and efficient debugging