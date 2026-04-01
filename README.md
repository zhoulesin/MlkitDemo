# Android 端侧 AI 学习路线

MlkitDemo - 基于 Google ML Kit 的移动端机器学习实践项目

## 项目信息

| 属性 | 说明 |
|------|------|
| 项目类型 | Android Application (Native Kotlin) |
| 包名 | com.cozyla.mlkitdemo |
| 语言 | 100% Kotlin |
| 架构 | Single-Activity |
| minSdk | 24 (Android 7.0) |
| targetSdk | 36 |

## 学习路线

### 第一阶段：基础入门
- **Day 1**: [AI 移动端核心概念](docs/day1-core-concepts.md)
  - 移动端 AI 优势
  - Google ML Kit 简介
  - 项目配置基础

### 第二阶段：OCR 文字识别
- **Day 2**: [纯 Kotlin 实现离线 OCR 文字识别](docs/day2-text-recognition.md)
  - 中文文本识别依赖配置
  - 相册选择与图片处理
  - ML Kit  TextRecognition API 使用

### 第三阶段：图像分类
- **Day 3**: [纯 Kotlin 离线图像分类](docs/day3-image-classification.md)
  - 图像标注依赖配置
  - 1000+ 类别识别
  - 置信度输出与展示

### 第四阶段：人脸检测
- **Day 4**: [ML Kit 人脸检测](docs/day4-face-detection.md)
  - 人脸检测依赖配置
  - 人脸位置与边界框
  - 头部姿态、微笑、眼睛状态检测

### 第五阶段：云端大模型
- **Day 5**: [Google Gemini API 接入](docs/day5-gemini-api.md)
  - Gemini API 依赖配置
  - 云端大模型聊天
  - API Key 获取与配置（保留文档，使用火山引擎替代实现）
- **Day 6**: [火山引擎（豆包）API 接入 + 流式输出](docs/day6-volcano-streaming.md)
  - 火山引擎 API 配置
  - 流式输出（打字机效果）
  - 国内稳定访问
- **Day 7**: [火山引擎多轮对话上下文管理](docs/day7-volcano-context.md)
  - 对话历史管理
  - Token 裁剪策略
  - 系统提示词配置

## 功能特性

| 功能 | 依赖库 | 版本 |
|------|--------|------|
| 中文 OCR | text-recognition-chinese | 16.0.1 |
| 图像分类 | image-labeling | 17.0.9 |
| 人脸检测 | face-detection | 16.1.7 |
| Gemini 聊天（文档） | generativeai | 0.7.0 |
| 火山引擎聊天 | OkHttp / Coroutines | 4.12.0 / 1.7.3 |

- ✅ 纯 Kotlin 实现
- ✅ 零 NDK / 零 C++
- ✅ 离线运行
- ✅ 隐私安全

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
