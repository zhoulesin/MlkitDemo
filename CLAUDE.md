# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MlkitDemo is an Android端侧 AI learning project demonstrating Google ML Kit's machine learning capabilities. It serves as both a practical demo and a structured learning route for mobile AI development.

**Key Details:**
- **Project Type**: Android Application (Native Kotlin)
- **Package Name**: com.cozyla.mlkitdemo
- **Language**: 100% Kotlin
- **Architecture**: Single-Activity Application
- **Build System**: Gradle
- **Android SDK**: Min: 24 (Android 7.0), Target: 36

## Learning Route Structure

This project is organized as a step-by-step learning route with daily documentation in the `docs/` directory:

- **Day 1**: Core concepts of mobile AI
- **Day 2**: Chinese OCR text recognition
- **Day 3**: Image classification
- **Day 4**: Face detection

Each daily doc includes a "Claude Code Quick Prompt" section that can be used to automatically implement that day's feature.

## Architecture and Structure

The project follows a simple single-Activity architecture:

```
app/
├── src/main/java/com/cozyla/mlkitdemo/
│   └── MainActivity.kt          # Core activity with all ML Kit integrations
├── src/main/res/layout/
│   └── activity_main.xml         # Main UI with three buttons
└── docs/                          # Learning route documentation
    ├── day1-core-concepts.md
    ├── day2-text-recognition.md
    ├── day3-image-classification.md
    └── day4-face-detection.md
```

**Core Components:**
- **MainActivity.kt**: Handles UI, gallery integration, and three ML Kit features (text recognition, image classification, face detection)
- **activity_main.xml**: Layout with three buttons and a results text view

## ML Kit Features (All Active)

1. **Chinese Text Recognition**
   - API: `com.google.mlkit:text-recognition-chinese:16.0.1`
   - Extracts Chinese text from images

2. **Image Classification**
   - API: `com.google.mlkit:image-labeling:17.0.9`
   - Recognizes 1000+ object categories with confidence scores

3. **Face Detection**
   - API: `com.google.mlkit:face-detection:16.1.7`
   - Detects faces, head angles, smiling probability, eye status

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

## Adding New ML Kit Features

When adding new ML Kit features:
1. Follow the existing pattern in MainActivity.kt (add request code, button, detection method)
2. Update the docs/ directory with a new dayN-*.md file including a Claude Code Quick Prompt
3. Update README.md with the new feature in the learning route and feature table
4. Keep versions in sync between build.gradle.kts, README.md, and documentation
