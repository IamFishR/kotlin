# Win11 Launcher for Android

A modern Android launcher application that mimics the Windows 11 interface, featuring a comprehensive notification system with real device notification integration and proper background activity launch handling.

## 🚀 Features

### Core Launcher Features
- **Windows 11-style UI** - Modern, clean interface inspired by Windows 11
- **Custom Taskbar** - Bottom taskbar with system tray and date/time display
- **Start Menu** - Windows 11-style start menu with app grid and personalized user display
- **All Apps Screen** - Complete application list with search functionality
- **Profile Customization** - Comprehensive user profile management and personalization

### Profile Customization System
- **"My Computer" Settings Tab** - Dedicated profile management interface
- **Custom Username Display** - Personalized username shown in Start Menu
- **Profile Picture Upload** - Upload and manage custom profile pictures with automatic optimization
- **Display Name Management** - Optional full name display alongside username
- **Personal Bio** - Add personal description and information
- **Theme Color Selection** - Choose from multiple accent colors for UI personalization
- **Database-Driven Storage** - All profile data stored securely in local SQLite database
- **Image Management** - Automatic image compression, rotation correction, and cleanup
- **Privacy Controls** - Toggle profile picture and username visibility in Start Menu

### Advanced Notification System
- **Real Device Notifications** - Displays actual notifications from installed apps
- **NotificationListenerService** - Intercepts and processes system notifications
- **Background Launch Support** - Properly handles notification clicks without background activity restrictions
- **Smart Filtering** - Filters out irrelevant system notifications
- **Permission Management** - Automatic permission requests and user guidance

### Technical Highlights
- **Multi-layered Launch Strategy** - BubbleMetadata and ActivityOptions approaches
- **Android 10+ Compatibility** - Handles modern Android background launch restrictions
- **Comprehensive Error Handling** - Multiple fallback mechanisms for notification launches
- **Battery Optimization Aware** - Requests necessary permissions for reliable operation
- **Room Database Integration** - SQLite database with migrations for profile data persistence
- **Advanced Image Processing** - Automatic compression, EXIF rotation, and size optimization
- **Hilt Dependency Injection** - Modern DI framework for clean architecture
- **Reactive State Management** - Flow-based state updates with Jetpack Compose integration

## 📱 Screenshots

*Taskbar with notification panel and system tray functionality*

## 🛠 Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API level 29 or higher
- Kotlin 1.8+
- Gradle 8.0+

### Setup
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd kotlin
   ```

2. Open the project in Android Studio

3. Build and install:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 🔧 Configuration

### Required Permissions
The app requires several permissions for full functionality:

#### Manifest Permissions
- `QUERY_ALL_PACKAGES` - Access to installed applications
- `BIND_NOTIFICATION_LISTENER_SERVICE` - Access to system notifications
- `SYSTEM_ALERT_WINDOW` - Overlay permission for proper launches
- `START_ACTIVITIES_FROM_BACKGROUND` - Background activity launching
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - Battery optimization exemption

#### Runtime Permissions
The app will guide users through granting:
1. **Notification Access** - Required to read device notifications
2. **Battery Optimization Exemption** - Ensures reliable notification handling
3. **System Alert Window** - Enables proper app launching from notifications

### First Launch Setup
1. **Set as Default Launcher** - Android will prompt to set as home app
2. **Grant Notification Access** - Follow the in-app guidance to enable notification listener
3. **Battery Optimization** - Allow the app to run in background for best performance
4. **Customize Profile** - Access Settings → "My Computer" to personalize your experience

### Profile Customization Usage
1. **Access Settings** - Open launcher settings and navigate to "My Computer" tab
2. **Set Username** - Click edit icon next to username to customize display name
3. **Upload Profile Picture** - Tap profile picture area or "Change" button to select image
4. **Add Personal Info** - Set display name and bio for a personalized experience
5. **Choose Theme Color** - Select accent color from the color picker grid
6. **Configure Privacy** - Control what information appears in the Start Menu

## 🏗 Architecture

### Core Components

#### UI Layer
- **LauncherScreen** - Main screen coordinator
- **Taskbar** - Bottom navigation and system tray
- **StartMenu** - Application launcher interface with personalized user display
- **NotificationPanel** - Real-time notification display
- **SettingsScreen** - Comprehensive settings management with profile customization

#### Services
- **Win11NotificationListenerService** - Intercepts system notifications
- **SystemStatusManager** - Monitors system state (battery, network, etc.)

#### Data Layer
- **UserProfileRepository** - Manages user profile data and customization settings
- **SettingsRepository** - Handles app settings and permission management
- **NotesDatabase** - Room database with user profiles, settings, and app data
- **ProfileImageManager** - Advanced image processing and storage management

#### Utilities
- **NotificationManager** - Handles notification clicks and permissions
- **AppRepository** - Manages installed application data
- **AppLauncher** - Application launching utilities

### Notification Click Architecture

```
User Click → NotificationManager.handleNotificationClick()
    ↓
1. BubbleMetadata Approach (Primary)
    - Creates Notification.BubbleMetadata with original PendingIntent
    - Uses ActivityOptions with proper Integer flags
    - Signals legitimate user interaction to Android system
    ↓
2. Enhanced ActivityOptions (Fallback)
    - Custom Bundle with background launch permissions
    - PendingIntent.OnFinished callback for error handling
    ↓
3. Simple PendingIntent Send (Fallback)
    - Basic PendingIntent.send() without options
    ↓
4. Direct App Launch (Last Resort)
    - PackageManager.getLaunchIntentForPackage()
    - Component-based launching if needed
```

## 🐛 Troubleshooting

### Common Issues

#### Notifications Not Appearing
1. Check notification access permission in Settings
2. Ensure the service is running: `adb shell dumpsys notification`
3. Verify app is not being killed by battery optimization

#### Apps Not Opening from Notifications
1. Grant battery optimization exemption
2. Enable "Display over other apps" permission
3. Check logs for background activity launch errors

#### Performance Issues
1. Reduce notification filtering in `shouldSkipNotification()`
2. Optimize notification update frequency
3. Check battery optimization settings

### Debug Commands
```bash
# Check notification listener status
adb shell dumpsys notification

# Monitor logs for notification events
adb logcat | grep "NotificationManager\|NotificationListener"

# Check battery optimization status
adb shell dumpsys deviceidle
```

## 📝 Development

### Key Files

#### Core Application
- `MainActivity.kt` - Entry point and launcher setup with Hilt integration
- `LauncherScreen.kt` - Main launcher interface coordination
- `StartMenu.kt` - Application launcher with personalized user display
- `Taskbar.kt` - Main taskbar implementation with system tray

#### Notification System
- `NotificationManager.kt` - Core notification handling logic
- `Win11NotificationListenerService.kt` - System notification interception
- `NotificationPanel.kt` - UI for notification display

#### Profile Customization
- `SettingsScreen.kt` - Profile customization UI with "My Computer" tab
- `UserProfile.kt` - Database entities for user data
- `UserProfileRepository.kt` - Profile data management and business logic
- `ProfileImageManager.kt` - Image processing and storage utilities
- `SettingsViewModel.kt` - Profile state management with reactive updates

### Adding New Features
1. Follow existing architecture patterns
2. Use proper error handling and logging
3. Test on multiple Android versions (API 29+)
4. Consider battery optimization impact

### Testing Notification Functionality
```kotlin
// Test notification click handling
notificationManager.handleNotificationClick(testNotification)

// Verify permissions
assert(notificationManager.isNotificationAccessEnabled())
assert(notificationManager.isBackgroundLaunchAllowed())
```

## 🔒 Security Considerations

- **Permission Scope** - Only requests necessary permissions
- **Data Privacy** - Notification content stays on device
- **Background Activity** - Properly handles Android security restrictions
- **Intent Validation** - Validates PendingIntents before launching

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📊 Technical Specifications

- **Minimum SDK**: API 29 (Android 10)
- **Target SDK**: API 34 (Android 14)
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Compose State Management
- **Database**: Room SQLite with migrations
- **Dependency Injection**: Hilt
- **Image Loading**: Coil Compose
- **State Management**: Kotlin Flows + Compose State

## 🙏 Acknowledgments

- Android Notification System Documentation
- Jetpack Compose UI Guidelines
- Windows 11 Design System Reference
- Android Background Activity Launch Best Practices

---

*Built with ❤️ for Android users who love the Windows 11 aesthetic*