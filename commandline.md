# Advanced Command Line System - Implementation Plan

Think of it as a hybrid of a classic Linux shell, Android's ADB, and a modern AI-powered copilot.

## 🎯 **Features Overview**

### 1. Advanced System & Hardware Interaction

Go beyond just displaying information and allow for direct control.

* **Network Management**:
    * `net scan wifi`: Scan for and list available Wi-Fi networks with their SSID, BSSID, and signal strength.
    * `net connect <ssid> --password <pass>`: Connect to a specific Wi-Fi network.
    * `net disconnect`: Disconnect from the current Wi-Fi network.
    * `bt scan`: Discover nearby Bluetooth devices.
    * `bt pair <device_address>`: Pair with a Bluetooth device.
    * `bt info`: Display detailed information about the Bluetooth adapter and connected devices.

* **Power Management**:
    * `power lock`: Lock the device screen (you already have a `SystemPowerManager` for this).
    * `power reboot --reason <reason>`: (Requires root/system permissions) Reboot the device.
    * `power battery`: Show detailed battery health, temperature, voltage, and charging status.

### 2. Deep Application & Process Management

Leverage your `AppRepository` to build powerful app management tools.

* **Application Control**:
    * `app list [--system]`: List all user-installed apps. The optional flag includes system apps.
    * `app info <package_name>`: Display comprehensive information about an app, including permissions, version, installation date, and signature.
    * `app launch <package_name>`: Launch an application.
    * `app kill <package_name>`: (Requires permission) Force-stop an application.
    * `app clear-cache <package_name>`: (Requires permission) Clear the cache for a specific app.

* **Permission Management**:
    * `perm list <package_name>`: List all permissions requested by an app and their current status (granted/denied).
    * `perm grant <package_name> <permission>`: (Requires special access) Grant a specific permission to an app.
    * `perm revoke <package_name> <permission>`: (Requires special access) Revoke a permission.

### 3. AI Copilot & Automation

This is where your CLI can truly shine by integrating the Gemma model as an intelligent assistant.

* **Natural Language to Command (AI Execution)**:
    * `ai "turn off wifi"`: The AI interprets this and executes the `net disconnect` command.
    * `ai "find all my photos from last week and move them to a new folder called 'Vacation'"`: A complex command where the AI generates and executes a sequence of file system commands.
    * `ai "create a rule to save all notifications from Slack that contain the word 'urgent' into the Work folder"`: The AI directly interacts with your Notes Hub rule engine.

* **Intelligent System Analysis**:
    * `ai analyze-battery`: Provides a natural language summary of battery usage, identifying apps that are draining the battery and suggesting actions.
    * `ai what's slowing down my device?`: The AI analyzes running processes and memory usage to provide a summary and recommendations.
    * `ai summarize my notifications for the last hour`: The AI processes recent notifications from your database and provides a concise summary.

* **Script Generation**:
    * `ai create-script --name "MorningRoutine" --actions "turn off dnd, set volume to 70%, launch news app"`: The AI generates a script file that can be executed later.

### 4. Full-Fledged File System Operations

A staple for any technical command line.

* **Standard Commands**:
    * `ls [-l] <path>`: List files and directories. The `-l` flag provides a detailed view (permissions, size, date).
    * `cd <path>`: Change the current directory.
    * `pwd`: Print the current working directory.
    * `mkdir <name>`: Create a new directory.
    * `touch <filename>`: Create a new empty file.
    * `cat <filename>`: Display the contents of a file.
    * `rm [-r] <path>`: Remove a file or directory (`-r` for recursive).
    * `cp <source> <destination>`: Copy a file or directory.
    * `mv <source> <destination>`: Move or rename a file or directory.

* **Advanced Search**:
    * `find <path> -name "*.jpg"`: Find all files matching a pattern.
    * `grep <pattern> <file>`: Search for a specific pattern within a file.

### 5. Development & Debugging Tools

Since you are a developer, these tools can be invaluable.

* **Logcat Viewer**:
    * `logcat [--grep="MyTag"]`: Stream the device logs (logcat) directly in the command prompt, with optional filtering.

* **Build & App Info**:
    * `build-info`: Display detailed information from your app's `build.gradle.kts`, such as `versionName`, `versionCode`, `compileSdk`, etc.
    * `dumpsys`: Provide a simplified, readable version of common `dumpsys` commands (e.g., `dumpsys battery`, `dumpsys meminfo`).

* **Database Inspector**:
    * `db query "SELECT * FROM notes WHERE folder_id = 'work'"`: Directly query the app's Room database.
    * `db schema <table_name>`: Display the schema for a specific database table.

---

## 🗃️ **Database Architecture**

### Database Tables (Dynamic Data Storage)

#### 1. Command History & Execution
```kotlin
@Entity(tableName = "command_history")
data class CommandHistory(
    @PrimaryKey val id: String,
    val command: String,                    // Full command text
    val commandType: String,                // SYSTEM, AI, FILE, NET, APP, DEV, etc.
    val subCommand: String?,                // e.g., "scan", "list", "info"
    val arguments: String?,                 // JSON array of arguments
    val timestamp: Long,
    val executionTimeMs: Long,
    val success: Boolean,
    val outputPreview: String,              // First 200 chars of output
    val fullOutputId: String?,              // Reference to command_outputs
    val sessionId: String                   // Group commands by session
)
```

#### 2. Command Outputs (For Large Results)
```kotlin
@Entity(tableName = "command_outputs")
data class CommandOutput(
    @PrimaryKey val id: String,
    val commandId: String,                  // FK to command_history
    val fullOutput: String,                 // Complete command output
    val outputType: String,                 // TEXT, JSON, ERROR, etc.
    val compressed: Boolean = false,        // For large outputs
    val timestamp: Long
)
```

#### 3. AI Conversations & Context
```kotlin
@Entity(tableName = "ai_conversations")
data class AIConversation(
    @PrimaryKey val id: String,
    val sessionId: String,
    val prompt: String,
    val response: String,
    val modelUsed: String,
    val tokensUsed: Int?,
    val processingTimeMs: Long,
    val timestamp: Long,
    val conversationType: String,           // CHAT, COMMAND_GENERATION, ANALYSIS
    val contextData: String?,               // JSON context for AI
    val generatedCommands: String?,         // JSON array of commands AI generated
    val userRating: Int?                    // User feedback 1-5
)
```

#### 4. User Scripts & Automation
```kotlin
@Entity(tableName = "user_scripts")
data class UserScript(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val commands: String,                   // JSON array of commands
    val createdAt: Long,
    val lastModified: Long,
    val lastExecuted: Long?,
    val executionCount: Int = 0,
    val isFavorite: Boolean = false,
    val tags: String?,                      // JSON array of tags
    val scheduleType: String?,              // MANUAL, HOURLY, DAILY, etc.
    val scheduleData: String?               // JSON schedule configuration
)
```

#### 5. System Monitoring & Analytics
```kotlin
@Entity(tableName = "system_snapshots")
data class SystemSnapshot(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val batteryLevel: Int,
    val batteryHealth: String?,
    val batteryTemp: Float?,
    val memoryUsage: Long,
    val cpuUsage: Float?,
    val networkState: String,               // WIFI, MOBILE, NONE
    val wifiSSID: String?,
    val runningApps: String,                // JSON array of running apps
    val triggerCommand: String?,            // Command that triggered this snapshot
    val customData: String?                 // JSON for extensibility
)
```

#### 6. Network & Bluetooth History
```kotlin
@Entity(tableName = "network_history")
data class NetworkHistory(
    @PrimaryKey val id: String,
    val type: String,                       // WIFI, BLUETOOTH
    val action: String,                     // SCAN, CONNECT, DISCONNECT, PAIR
    val targetIdentifier: String,           // SSID, MAC address, etc.
    val success: Boolean,
    val timestamp: Long,
    val additionalData: String?,            // JSON for signal strength, etc.
    val commandId: String?                  // Reference to command_history
)
```

#### 7. App Management History
```kotlin
@Entity(tableName = "app_actions")
data class AppAction(
    @PrimaryKey val id: String,
    val packageName: String,
    val action: String,                     // LAUNCH, KILL, CLEAR_CACHE, etc.
    val timestamp: Long,
    val success: Boolean,
    val commandId: String?,                 // Reference to command_history
    val additionalData: String?             // JSON for extra info
)
```

#### 8. File System Operations
```kotlin
@Entity(tableName = "file_operations")
data class FileOperation(
    @PrimaryKey val id: String,
    val operation: String,                  // LS, CD, CP, MV, RM, MKDIR, etc.
    val sourcePath: String,
    val destinationPath: String?,
    val timestamp: Long,
    val success: Boolean,
    val filesAffected: Int,
    val bytesTransferred: Long?,
    val commandId: String?                  // Reference to command_history
)
```

#### 9. Database Query History
```kotlin
@Entity(tableName = "db_queries")
data class DatabaseQuery(
    @PrimaryKey val id: String,
    val query: String,
    val tableName: String?,
    val queryType: String,                  // SELECT, INSERT, UPDATE, DELETE, SCHEMA
    val resultCount: Int,
    val executionTimeMs: Long,
    val timestamp: Long,
    val success: Boolean,
    val errorMessage: String?,
    val commandId: String?                  // Reference to command_history
)
```

#### 10. Command Usage Statistics
```kotlin
@Entity(tableName = "command_usage")
data class CommandUsage(
    @PrimaryKey val command: String,        // Command pattern (e.g., "net scan")
    val usageCount: Int = 0,
    val lastUsed: Long?,
    val successRate: Float = 0.0f,
    val averageExecutionTime: Float = 0.0f,
    val totalExecutionTime: Long = 0L,
    val category: String                    // NET, APP, FILE, AI, DEV, etc.
)
```

### Static Command Structure (In Code/Files)

#### Command Registry (Kotlin Code)
```kotlin
// commands/CommandRegistry.kt
object CommandRegistry {
    val SYSTEM_COMMANDS = mapOf(
        "net" to NetworkCommands,
        "bt" to BluetoothCommands,
        "power" to PowerCommands,
        "app" to AppCommands,
        "perm" to PermissionCommands,
        "ai" to AICommands,
        "file" to FileCommands,
        "dev" to DevCommands,
        "db" to DatabaseCommands
    )
}
```

---

## 📅 **Implementation Timeline**

### **Phase 1: Database Foundation (Week 1)**
- [ ] Create database entities for command history, outputs, and AI conversations
- [ ] Set up DAOs and repositories for core tables
- [ ] Implement database migration from v13 to v14
- [ ] Update existing CommandPrompt to persist history to database
- [ ] Test database integration with current commands

### **Phase 2: Command Infrastructure (Week 2)**
- [ ] Create CommandRegistry and static command definitions
- [ ] Implement command parsing and execution framework
- [ ] Add command categories (NET, APP, FILE, AI, DEV, etc.)
- [ ] Create base command interfaces and abstract classes
- [ ] Implement enhanced autocomplete using static commands + usage stats

### **Phase 3: System & Hardware Commands (Week 3)**
- [ ] Implement Network Management commands (net scan, connect, disconnect)
- [ ] Add Bluetooth commands (bt scan, pair, info)
- [ ] Create Power Management commands (power lock, battery)
- [ ] Add system monitoring and snapshot functionality
- [ ] Implement permission checking and request handling

### **Phase 4: Application Management (Week 4)**
- [ ] Create App Control commands (app list, info, launch, kill)
- [ ] Implement Permission Management commands (perm list, grant, revoke)
- [ ] Add app action tracking to database
- [ ] Create comprehensive app information display
- [ ] Implement app cache management

### **Phase 5: File System Operations (Week 5)**
- [ ] Implement basic file commands (ls, cd, pwd, mkdir, touch)
- [ ] Add file manipulation commands (cat, rm, cp, mv)
- [ ] Create advanced search commands (find, grep)
- [ ] Implement file operation tracking
- [ ] Add file permission and metadata display

### **Phase 6: AI Enhancement (Week 6)**
- [ ] Enhance AI command interpretation and execution
- [ ] Implement natural language to command conversion
- [ ] Add intelligent system analysis commands
- [ ] Create AI script generation functionality
- [ ] Implement AI conversation context management

### **Phase 7: Development Tools (Week 7)**
- [ ] Create logcat viewer with filtering
- [ ] Implement build information commands
- [ ] Add dumpsys integration
- [ ] Create database inspector commands
- [ ] Implement debugging and diagnostic tools

### **Phase 8: Advanced Features (Week 8)**
- [ ] Add user script creation and management
- [ ] Implement command scheduling and automation
- [ ] Create command analytics and reporting
- [ ] Add performance optimization and caching
- [ ] Implement data archiving and cleanup

### **Phase 9: Polish & Testing (Week 9)**
- [ ] Comprehensive testing of all command categories
- [ ] Performance optimization and database tuning
- [ ] UI/UX improvements and error handling
- [ ] Documentation and help system completion
- [ ] Security audit and permission validation

### **Phase 10: Future Enhancements (Week 10+)**
- [ ] Plugin system for custom commands
- [ ] Advanced AI integration and learning
- [ ] Cloud synchronization for scripts and history
- [ ] Voice command integration
- [ ] Advanced automation and rule engine

---

## 🎯 **Success Metrics**

- **Command Execution**: 50+ built-in commands across 8 categories
- **Database Performance**: Sub-100ms query response times
- **AI Integration**: Natural language command interpretation
- **User Experience**: Intuitive autocomplete and help system
- **Data Management**: Efficient storage with 1-2GB yearly growth
- **Extensibility**: Plugin-ready architecture for future commands

By implementing these features, your command prompt will evolve from a simple utility into a sophisticated control center that gives technical users unprecedented power and flexibility, all powered by the intelligence of your on-device AI.