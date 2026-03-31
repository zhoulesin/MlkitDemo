# MlkitDemo

轻量级 Android 应用，演示 Google ML Kit 的移动端机器学习能力，提供图像分类和中文文本识别功能。

## 项目概览

- **项目类型**: Android Application (Native Kotlin)
- **包名**: com.cozyla.mlkitdemo
- **语言**: 100% Kotlin
- **架构**: Single-Activity Application
- **构建系统**: Gradle 9.2.1
- **Android SDK**: Min: 24 (Android 7.0), Target: 36

## 学习目录

| Day | 标题 | 文档 |
|-----|------|------|
| Day 1 | AI 移动端核心概念 | [docs/day1-core-concepts.md](docs/day1-core-concepts.md) |
| Day 2 | 纯 Kotlin 实现离线 OCR 文字识别 | [docs/day2-text-recognition.md](docs/day2-text-recognition.md) |
| Day 3 | 纯 Kotlin 离线图像分类 | [docs/day3-image-classification.md](docs/day3-image-classification.md) |

## ML Kit 功能

1. **图像分类**
   - API: `com.google.mlkit:image-labeling:17.0.9`
   - 识别 1000+ 种对象类别及置信度
   - 设备端离线运行

2. **中文文本识别**
   - API: `com.google.mlkit:text-recognition-chinese:16.0.1`
   - 从图像中提取中文文本

## 常用命令

```bash
# 构建
./gradlew build
./gradlew :app:build
./gradlew clean :app:assembleDebug

# 测试
./gradlew test
./gradlew androidTest

# 安装并运行
./gradlew :app:installDebug
adb shell am start -n com.cozyla.mlkitdemo/.MainActivity
```

## 总结

| 功能 | 依赖库 | 版本 |
|------|--------|------|
| 中文 OCR | text-recognition-chinese | 16.0.1 |
| 图像分类 | image-labeling | 17.0.9 |

**特点：**
- ✅ 纯 Kotlin 实现
- ✅ 零 NDK / 零 C++
- ✅ 离线运行
- ✅ 隐私安全
