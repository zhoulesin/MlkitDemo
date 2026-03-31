# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MlkitDemo is a lightweight Android application demonstrating Google ML Kit's machine learning capabilities for mobile devices. It provides image classification and Chinese text recognition features through a simple, user-friendly interface.

**Key Details:**
- **Project Type**: Android Application (Native Kotlin)
- **Package Name**: com.cozyla.mlkitdemo
- **Language**: 100% Kotlin
- **Architecture**: Single-Activity Application
- **Build System**: Gradle 9.2.1
- **Android SDK**: Min: 24 (Android 7.0), Target: 36

## Architecture and Structure

The project follows a simple single-Activity architecture:

```
app/
├── src/main/java/com/cozyla/mlkitdemo/
│   └── MainActivity.kt          # Core activity with ML Kit integration
├── src/main/res/
│   ├── layout/activity_main.xml # Main UI layout
│   └── values/                   # Strings, colors, themes
├── src/test/java/                # Local unit tests
└── src/androidTest/java/         # Instrumented tests
```

**Core Components:**
- **MainActivity.kt**: Handles UI, gallery integration, and ML Kit API calls
- **activity_main.xml**: Simple layout with a button and results text view

## ML Kit Features

1. **Image Classification** (Default - active)
   - API: `com.google.mlkit:image-labeling:17.0.0`
   - Recognizes 1000+ object categories with confidence scores
   - Runs on-device

2. **Chinese Text Recognition** (Commented out in code)
   - API: `com.google.mlkit:text-recognition-chinese:16.0.0`
   - Chinese language text extraction from images

## Common Commands

```bash
# Build
./gradlew build
./gradlew :app:build
./gradlew clean :app:assembleDebug

# Test
./gradlew test
./gradlew androidTest

# Install and run
./gradlew :app:installDebug
adb shell am start -n com.cozyla.mlkitdemo/.MainActivity
```
