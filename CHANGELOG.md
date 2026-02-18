# Changelog

All notable changes to the "Log Injector" plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - 2026-02-19

### Fixed
- Fixed incorrect removal of entire scope function blocks (`apply`, `let`, `run`) when removing a single log line inside them

### Improved
- Added Napier logging framework support for Kotlin Multiplatform projects
- Minor stability improvements

### Planned Features
- Support for additional logging frameworks (Log4j, SLF4J)
- Custom log templates
- Smart log placement (avoid duplicates)
- Log level configuration (DEBUG, INFO, WARN, ERROR)
- Bulk operations across multiple files
- Import optimization for logging frameworks

## [1.0.0] - 2025-02-17

### Added
- 🎉 Initial release of Log Injector
- ✨ Automatic log insertion for Java and Kotlin methods
- ✨ Automatic log removal functionality
- ⚙️ Support for System.out.println logging
- ⚙️ Support for Timber logging framework (Android)
- 📊 Method execution tracking
- 📊 Variable assignment tracking
- 🎨 Configurable log tags/prefixes
- 🎨 Tool window for easy configuration
- 🔧 Context menu integration (right-click actions)
- ✅ Full Kotlin K2 support
- ✅ IntelliJ IDEA 2024.3+ compatibility
- 📝 Comprehensive documentation and README
- 🧪 Unit tests for core functionality

### Technical Details
- Built with Kotlin 2.1.0
- Uses IntelliJ Platform SDK 2024.3
- Code style enforced with ktlint
- Gradle-based build system
- GitHub Actions CI/CD integration

### Compatibility
- IntelliJ IDEA 2024.3 (build 243) to 2025.3 (build 253.*)
- Supports Java and Kotlin files
- Requires JDK 21 or higher

---

## Release Notes Format for Marketplace

### Version 1.0.0 - First Release 🎉

**New Features:**
- One-click log insertion and removal for Java and Kotlin
- Support for System.out.println and Timber frameworks
- Configurable log tags and tracking options
- Tool window for easy configuration
- Context menu integration

**Technical:**
- Full Kotlin K2 compiler support
- Compatible with IntelliJ IDEA 2024.3+
- Built with modern IntelliJ Platform SDK

**Get Started:**
1. Right-click in any Java/Kotlin file
2. Choose "Insert Logs" to add debug statements
3. Use "Remove Logs" when done debugging
4. Configure options in the LoggingOptions tool window

Thank you for trying Log Injector! Report issues at: https://github.com/Kiolk/Log-Injector/issues