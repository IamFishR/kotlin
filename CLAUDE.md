# Bash Commands
## Working Gradle Commands (Use --no-daemon in WSL)
 - `./gradlew compileDebugKotlin --no-daemon` - Compile Kotlin code in debug mode
 - `./gradlew assembleDebug --no-daemon` - Build debug APK
 - `./gradlew clean --no-daemon` - Clean build artifacts
 - `./gradlew check --no-daemon` - Run all checks including lint
 - `./gradlew lint --no-daemon` - Run lint analysis
 - `./gradlew app:dependencies --configuration debugCompileClasspath` - Check dependencies
 - `./gradlew installDebug --no-daemon` - Build and install APK to connected device
 - `adb devices` - Check connected Android devices
 - `adb install app/build/outputs/apk/debug/app-debug.apk` - Manual APK install

## Android Device Connection in WSL
 - Enable USB Debugging on your Android device (Settings → Developer Options)
 - Install Android SDK Platform Tools on Windows
 - Connect device to Windows PC and authorize debugging
 - WSL can use Windows ADB daemon for device connection
 - Use `./gradlew installDebug --no-daemon` to build and install directly

## Build Issues in WSL
 - Gradle daemon hangs in WSL environment - always use `--no-daemon` flag
 - Regular `./gradlew build` will hang - avoid using it
 - Use `--no-daemon` for all Gradle commands to prevent hanging

# Project Guide
 - check for deprecated APIs before using them
 - Build system uses Kotlin 2.1.0, Android Gradle Plugin 8.11.0
 - Target SDK 34, Compile SDK 36, Min SDK 26