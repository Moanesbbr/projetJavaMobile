# eventii07 - Local Setup Guide

This guide will help you set up and run the eventii07 Android application using only command-line tools, without Android Studio. This is perfect for minimalist development environments or systems with limited resources.

## Prerequisites

- Windows 10 or 11
- Java (already installed)
- Internet connection for downloading SDK tools
- Terminal access (PowerShell or Command Prompt)
- An Android phone with USB debugging enabled

## 1. Install Required Java Version

It's recommended to use Java 17 for compatibility with the Android build tools:

1. Download OpenJDK 17 from [Adoptium](https://adoptium.net/)
2. Run the installer and follow the installation steps
3. Verify installation by opening PowerShell and typing:

```powershell
java -version
```

## 2. Download and Install Android Command Line Tools

First, let's create the necessary directory structure for Android SDK:

```powershell
# Create directories for Android SDK
mkdir -Force C:\Android\sdk
mkdir -Force C:\Android\sdk\platforms
mkdir -Force C:\Android\sdk\avd
```

Download the command line tools and platform tools:

1. Download command line tools from [Android SDK Command Line Tools](https://developer.android.com/studio#command-tools)
2. Download platform tools from [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools)

Extract the tools:

```powershell
# Extract command line tools (assuming downloaded to Downloads folder)
Expand-Archive -Path $env:USERPROFILE\Downloads\commandlinetools-win-*.zip -DestinationPath C:\Android\sdk\cmdline-tools-tmp

# Extract platform tools
Expand-Archive -Path $env:USERPROFILE\Downloads\platform-tools-latest-windows.zip -DestinationPath C:\Android\sdk\
```

Organize directories correctly (Android SDK Manager expects this specific structure):

```powershell
mkdir -Force C:\Android\sdk\cmdline-tools\latest
Move-Item -Path C:\Android\sdk\cmdline-tools-tmp\cmdline-tools\* -Destination C:\Android\sdk\cmdline-tools\latest
Remove-Item -Path C:\Android\sdk\cmdline-tools-tmp -Recurse
```

## 3. Set Up Environment Variables

Set environment variables using PowerShell:

```powershell
# Set environment variables for current session
$env:ANDROID_SDK_ROOT = "C:\Android\sdk"
$env:ANDROID_CMD_LINE_TOOLS = "$env:ANDROID_SDK_ROOT\cmdline-tools\latest"
$env:ANDROID_PLATFORM_TOOLS = "$env:ANDROID_SDK_ROOT\platform-tools"
$env:ANDROID_AVD_HOME = "$env:ANDROID_SDK_ROOT\avd"
$env:PATH = "$env:PATH;$env:ANDROID_CMD_LINE_TOOLS\bin;$env:ANDROID_SDK_ROOT;$env:ANDROID_PLATFORM_TOOLS"

# To add these permanently to your system (run PowerShell as Administrator):
[Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", "C:\Android\sdk", "User")
[Environment]::SetEnvironmentVariable("ANDROID_CMD_LINE_TOOLS", "C:\Android\sdk\cmdline-tools\latest", "User")
[Environment]::SetEnvironmentVariable("ANDROID_PLATFORM_TOOLS", "C:\Android\sdk\platform-tools", "User")
[Environment]::SetEnvironmentVariable("ANDROID_AVD_HOME", "C:\Android\sdk\avd", "User")
[Environment]::SetEnvironmentVariable("PATH", [Environment]::GetEnvironmentVariable("PATH", "User") + ";C:\Android\sdk\cmdline-tools\latest\bin;C:\Android\sdk;C:\Android\sdk\platform-tools", "User")
```

After setting environment variables, restart your PowerShell session.

## 4. Install Required SDK Components

Install the required Android SDK components:

```powershell
# Install basic SDK components (match these to your app's target SDK version)
sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.0"

# Accept licenses
sdkmanager --licenses
```

## 5. Prepare Project for Build

Navigate to your project directory:

```powershell
cd C:\path\to\java-mobile  # Replace with the actual path to your project
```

Install Gradle:

1. Download Gradle from [gradle.org/releases](https://gradle.org/releases/)
2. Extract the ZIP to a location like `C:\Gradle`
3. Add Gradle to your PATH:

```powershell
# For current session
$env:PATH = "$env:PATH;C:\Gradle\gradle-8.0\bin"

# Permanently (run PowerShell as Administrator)
[Environment]::SetEnvironmentVariable("PATH", [Environment]::GetEnvironmentVariable("PATH", "User") + ";C:\Gradle\gradle-8.0\bin", "User")
```

Create the Gradle wrapper (required for specific version compatibility):

```powershell
gradle wrapper --gradle-version 8.0
```

Create `gradle.properties` file:

```powershell
@"
# Project-wide Gradle settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
"@ | Out-File -FilePath gradle.properties -Encoding utf8
```

Create a build script (`build-with-java17.ps1`) to ensure Java 17 is used:

```powershell
@"
# Set Java 17 as current Java
`$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.x"  # Update with your actual Java 17 path
`$env:PATH = "`$env:JAVA_HOME\bin;`$env:PATH"
java -version
.\gradlew assembleDebug
"@ | Out-File -FilePath build-with-java17.ps1 -Encoding utf8
```

## 6. Building the eventii07 App

Run the build script:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-with-java17.ps1
```

If the build is successful, the APK will be located at `app\build\outputs\apk\debug\app-debug.apk`.

*Note: If the build fails due to missing icons, you might need to temporarily remove `android:icon` and `android:roundIcon` attributes from the `<application>` tag in `app/src/main/AndroidManifest.xml`.*

## 7. Install and Run the App on Your Phone

**Enable USB Debugging on your Phone:**

1.  Go to `Settings` > `About phone`.
2.  Tap `Build number` seven times.
3.  Go back to `Settings`, find `Developer options` (it might be under `System`).
4.  Enable `Developer options` and `USB debugging`.

**Connect Your Phone:**

Connect your phone to your PC via USB.
You might see a prompt on your phone asking to "Allow USB debugging?". Check "Always allow from this computer" and tap "Allow".

**Install USB Drivers (if needed):**

If Windows doesn't recognize your device, you may need to install manufacturer-specific USB drivers from your phone manufacturer's website.

**Install the App:**

Open PowerShell in the project directory and run:

```powershell
# Check if the device is connected
adb devices

# Install the app (replace path if necessary)
adb install app\build\outputs\apk\debug\app-debug.apk
```

If the app is already installed, you can update it using:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

**Launch the App:**

You can usually find the "eventii07" app in your phone's app drawer. Alternatively, you can launch it via ADB:

```powershell
adb shell am start -n com.polyscievent.tracker/.activity.MainActivity
```

## Troubleshooting

1.  **Build fails**: Check Java version (`java -version` should show version 17), clean the project (`.\gradlew clean`), and ensure all SDK components are installed correctly.
2.  **ADB connection issues**: Ensure USB debugging is enabled and authorized. Try reconnecting the USB cable or running `adb kill-server` followed by `adb start-server`.
3.  **PowerShell execution policy**: If you encounter execution policy errors, run PowerShell as administrator and set: `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned`

## Conclusion

You now have a fully functional Android development environment for the eventii07 app using command-line tools on Windows. You can build, test, and run the application on your physical Android device. 