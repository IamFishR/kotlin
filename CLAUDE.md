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

## Build Issues in WSL
 - Gradle daemon hangs in WSL environment - always use `--no-daemon` flag
 - Regular `./gradlew build` will hang - avoid using it
 - Use `--no-daemon` for all Gradle commands to prevent hanging

# Project Guide
 - check for deprecated APIs before using them
 - Build system uses Kotlin 2.1.0, Android Gradle Plugin 8.11.0
 - Target SDK 34, Compile SDK 36, Min SDK 26

# Explore, plan, code, commit
 - Explore the project structure and files regarding requested features (use multiple subagents strongly)
 - make a plan for how to approach a specific problem
 - implement its solution in code
 - Commit changes with clear messages
 - Push to remote repository when ready