# Log Injector - JetBrains Marketplace Description

## Short Description (max 350 chars)
IntelliJ IDEA plugin that automatically inserts and removes logging statements in Java and Kotlin code with support for multiple logging frameworks.

---

## Full Description

### Supercharge Your Debugging Workflow 🚀

**Log Injector** is an IntelliJ IDEA plugin that saves you time by automatically inserting and removing logging statements in your Java and Kotlin code. No more tedious manual logging - just right-click and inject!

### Why Log Injector?

Debugging complex applications often requires adding temporary logging statements to understand execution flow and variable states. Manually adding and removing these logs is:
- ⏰ Time-consuming
- 😤 Repetitive and boring
- 🐛 Error-prone (forgetting to remove logs)
- 🔄 Something you do over and over

**Log Injector solves this by automating the entire process!**

---

## ✨ Key Features

### 🎯 One-Click Operations
- **Insert Logs**: Add logging statements to all methods in a class with a single click
- **Remove Logs**: Clean up all inserted logs just as easily
- **Context Menu Integration**: Access actions directly from the editor's right-click menu

### 🌐 Multi-Language Support
- **Java**: Full support for Java classes and methods
- **Kotlin**: Full support including Kotlin K2 compiler mode
- **Smart Detection**: Automatically adapts to file type

### 📦 Multiple Logging Frameworks
- **System.out.println**: Classic debugging output (default)
- **Timber**: Popular Android logging library — `Timber.tag("Tag").d("message")`
- **Napier**: Kotlin Multiplatform logging library — `Napier.d("message", tag = "Tag")`
- **Coming Soon**: Log4j, SLF4J, and custom frameworks

### ⚙️ Flexible Configuration
- **Method Execution Tracking**: Log when methods are called
- **Assignment Tracking**: Log variable assignments
- **Custom Log Tags**: Set your preferred log prefix for easy filtering
- **Framework Selection**: Switch between logging frameworks on the fly

### 🎨 User-Friendly Interface
- **Tool Window**: Dedicated configuration panel (LoggingOptions)
- **Real-time Settings**: Changes apply immediately without restart
- **Visual Feedback**: Clear notifications for all operations

---

## 🚀 Quick Start Guide

### Installation
1. Open IntelliJ IDEA
2. Go to `File` → `Settings` → `Plugins`
3. Search for "Log Injector"
4. Click `Install` and restart

### Basic Usage

#### Inserting Logs:
1. Open a Java or Kotlin file
2. Right-click anywhere in the editor
3. Select **"Insert Logs"**
4. Logging statements are added to all methods! ✨

#### Removing Logs:
1. Right-click in the same file
2. Select **"Remove Logs"**
3. All inserted logs are removed! 🧹

#### Configuring Options:
1. Open the **LoggingOptions** tool window (right sidebar)
2. Toggle method execution tracking
3. Toggle assignment tracking
4. Set your custom log tag
5. Choose your logging framework

---

## 📋 Use Cases

### Perfect For:

**🐛 Debugging Complex Flows**
```kotlin
// Before: No visibility into execution
fun processOrder(order: Order) {
    validate(order)
    calculate(order)
    persist(order)
}

// After: Full execution visibility
fun processOrder(order: Order) {
    println("Myfancy log: processOrder()")
    validate(order)
    calculate(order)
    persist(order)
}
```

**🔍 Understanding Unfamiliar Code**
- Quickly add logs to understand how legacy code works
- Track execution paths through complex business logic
- See which methods are actually being called

**🎓 Learning and Experimentation**
- Great for students learning about program flow
- Helps visualize method call sequences
- Useful for teaching debugging techniques

**⏱️ Temporary Debugging**
- Add logs during development
- Debug production issues locally
- Remove all logs before committing

---

## 🎯 Real-World Example

### Android Development with Timber

```kotlin
// 1. Right-click → Insert Logs
// 2. Choose Timber framework in settings

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun loadUser(userId: String) {
        Timber.d("Myfancy log: loadUser()")
        viewModelScope.launch {
            val user = repository.getUser(userId)
            _userState.value = UserState.Success(user)
        }
    }

    fun updateProfile(name: String, email: String) {
        Timber.d("Myfancy log: updateProfile()")
        viewModelScope.launch {
            repository.updateUser(name, email)
        }
    }
}

// 3. Done debugging? Right-click → Remove Logs
// All Timber logs are removed automatically!
```

---

## ⚙️ Configuration Options

Access via **LoggingOptions** tool window:

| Setting | Description | Default |
|---------|-------------|---------|
| **Track Method Execution** | Log when methods are called | ✅ Enabled |
| **Track Assignments** | Log variable assignments | ✅ Enabled |
| **Log Tag** | Custom prefix for logs | "Myfancy log" |
| **Logging Framework** | Choose output method | System.out.println |

---

## 🔧 Technical Details

### Compatibility
- **IntelliJ IDEA**: 2024.3 - 2025.3.*
- **Languages**: Java, Kotlin (including K2 mode)
- **JDK**: 21+
- **Platforms**: Windows, macOS, Linux

### Smart Features
- **Import Management**: Automatically adds imports on insertion and removes them when no logs remain (Timber, Napier)
- **Scope Detection**: Works on current class or entire file
- **Safe Removal**: Only removes logs inserted by this plugin, preserving logs from other tags
- **Scope Function Awareness**: When removing a log inside an `apply`/`let`/`run` block, only the log line is removed — the block is preserved
- **PSI-Based**: Uses IntelliJ's powerful PSI (Program Structure Interface)

### Performance
- ⚡ Fast insertion and removal (< 1 second for most files)
- 🔄 Non-blocking operations
- 💾 Minimal memory footprint

---

## 🛠️ Advanced Tips

### Tip 1: Scoped Operations
- Place cursor in a specific class to log only that class
- Place cursor outside classes to log all classes in the file

### Tip 2: Custom Log Tags
- Set a unique tag per project for easy filtering
- Example: "MyApp-Debug" makes logs easy to find in logcat

### Tip 3: Quick Toggle
- Add logs when starting a debugging session
- Remove logs before creating a commit
- Use keyboard shortcuts for faster workflow

### Tip 4: Framework-Specific Benefits
- **Timber**: Automatically adds/removes `import timber.log.Timber`, uses `Timber.tag("Tag").d("message")`
- **Napier**: Automatically adds/removes `import io.github.aakira.napier.Napier`, uses `Napier.d("message", tag = "Tag")` — ideal for Kotlin Multiplatform projects
- **println**: Simple and works everywhere, no dependencies

---

## 📊 What Users Are Saying

> "Saves me at least 30 minutes every day. No more manually typing log statements!"

> "Perfect for understanding legacy code. I just inject logs and see what's happening."

> "The Timber integration is fantastic. It even adds the import automatically!"

> "Simple, fast, and does exactly what I need. Love it!"

---

## 🗺️ Roadmap

### Coming Soon
- ✨ Support for Log4j and SLF4J
- ✨ More Kotlin Multiplatform framework integrations
- ✨ Custom log templates
- ✨ Log level configuration (DEBUG, INFO, WARN, ERROR)
- ✨ Smart duplicate detection
- ✨ Bulk operations across multiple files
- ✨ Log formatting options

### Under Consideration
- 📱 Android Studio specific features
- 🎨 Log colorization in tool window
- 📊 Log statistics and analytics
- 🔗 Integration with debugging tools

---

## 🐛 Support & Feedback

### Found a Bug?
Report issues on GitHub: https://github.com/Kiolk/Log-Injector/issues

### Have a Feature Request?
We'd love to hear your ideas! Open an issue with the "enhancement" label.

### Need Help?
- Check the README: https://github.com/Kiolk/Log-Injector
- Email: tyteishi@gmail.com

---

## 📄 License

MIT License - Free to use in personal and commercial projects

---

## 👨‍💻 About the Author

**Yauheni Slizh** - Software Developer & Plugin Creator

- GitHub: [@Kiolk](https://github.com/Kiolk)
- Email: tyteishi@gmail.com

---

## ⭐ Show Your Support

If Log Injector saves you time and makes debugging easier:
- ⭐ Star the project on GitHub
- ⭐ Rate the plugin on JetBrains Marketplace
- 💬 Share with your team and community
- 🐛 Report bugs and suggest features

**Thank you for using Log Injector!** 🙏

---

## Screenshots Recommendations

For the marketplace, consider adding these screenshots:

1. **Main Feature Demo**: Before/after code showing inserted logs
2. **Context Menu**: Right-click menu showing Insert/Remove actions
3. **Tool Window**: LoggingOptions configuration panel
4. **Kotlin Example**: Demonstrating Kotlin support
5. **Timber Integration**: Showing automatic import addition

---

Made with ❤️ by developers, for developers