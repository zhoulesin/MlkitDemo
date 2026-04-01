---
name: lessons-learned
description: 记录在本项目中犯过的错误，避免重蹈覆辙
---

# 项目经验教训记录

## 已犯错误列表

### 1. NetworkOnMainThreadException - 网络请求在主线程执行

**错误时间**: 2026-04-01

**错误描述**:
在 `VolcanoApiHelper.kt` 的 `chatNonStream()` 方法中，OkHttp 的 `execute()` 方法直接在主线程调用，导致 `NetworkOnMainThreadException`。

**错误代码**:
```kotlin
// 错误写法 - 没有指定线程
suspend fun chatNonStream(messages: List<Map<String, String>>): String {
    // ...
    client.newCall(request).execute()  // 主线程执行网络请求
}
```

**修正方案**:
```kotlin
// 正确写法 - 使用 withContext 切换到 IO 线程
suspend fun chatNonStream(messages: List<Map<String, String>>): String = withContext(Dispatchers.IO) {
    // ...
    client.newCall(request).execute()  // IO 线程执行网络请求
}
```

**经验教训**:
- ✅ 所有 suspend 函数中的网络/IO 操作必须用 `withContext(Dispatchers.IO)` 包裹
- ✅ 或者确保函数本身就在 IO 调度器上调用
- ✅ Android 禁止在主线程执行网络请求

---

### 2. API 模型名称不匹配

**错误时间**: 2026-04-01

**错误描述**:
使用火山引擎 API 时，模型名称写错：
- 错误: `Doubao-seed-2-0-lite`
- 正确: `doubao-seed-2-0-lite-260215`

**经验教训**:
- ✅ 严格按照官方文档的模型名称，注意大小写和完整版本号
- ✅ 参考官方 curl 示例确认参数格式

---

### 3. SocketTimeoutException - 网络超时无重试

**错误时间**: 2026-04-01

**错误描述**:
OkHttp 使用默认超时时间（太短），且没有重试机制，网络波动时容易失败。

**错误代码**:
```kotlin
// 错误写法 - 默认超时 + 无重试
private val client = OkHttpClient()
```

**修正方案**:
```kotlin
// 正确写法 - 延长超时 + 重试机制
private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

// 配合通用重试帮助类
RetryHelper.executeWithRetry(maxRetries = 3) {
    // 请求代码
}
```

**经验教训**:
- ✅ 大模型 API 需要更长的读取超时（建议 120s+）
- ✅ 网络请求必须有重试机制
- ✅ 将网络配置和重试逻辑抽成通用工具类

---

### 4. 代码重构 - 职责分离

**时间**: 2026-04-01

**问题描述**:
MainActivity 和 VolcanoApiHelper 包含太多职责，难以维护。

**重构方案**:
- `mlkit/` 包 - ML Kit 各功能独立 Helpers
- `network/` 包 - HttpClient、RetryHelper 通用网络工具
- VolcanoApiHelper - 仅负责 API 调用和响应解析

**经验教训**:
- ✅ 每个类只做一件事（单一职责原则）
- ✅ 通用功能抽成工具类，便于复用
- ✅ 复杂逻辑拆分成小方法
- ✅ 每完成约 10 个功能点后，强制进行一次代码回顾

---

## 5. 缺少定期代码回顾机制

**问题描述**:
代码随着功能增加逐渐臃肿，没有定期回顾和重构的机制。

**解决方案**:
- 创建 `code-refresh` skill
- 每完成约 10 个功能点后，强制回顾一次
- 检查：单一职责、代码重复、可复用性

---

## Android 开发检查清单

写代码前检查：
- [ ] 网络请求是否在 IO 线程？
- [ ] 敏感操作是否有 try-catch？
- [ ] 第三方 API 的参数是否完全符合文档？
- [ ] 是否添加了必要的日志？

定期回顾检查（约 10 个功能点后）：
- [ ] 是否有超过 200 行的类？
- [ ] 是否有重复代码可以抽取？
- [ ] 包结构是否合理？
- [ ] 是否可以进一步拆分？

---

## Android 开发检查清单

写代码前检查：
- [ ] 网络请求是否在 IO 线程？
- [ ] 敏感操作是否有 try-catch？
- [ ] 第三方 API 的参数是否完全符合文档？
- [ ] 是否添加了必要的日志？

## 快速参考

### 协程线程切换
```kotlin
// IO 操作（网络、文件、数据库）
withContext(Dispatchers.IO) { }

// 计算密集操作
withContext(Dispatchers.Default) { }

// 更新 UI
withContext(Dispatchers.Main) { }
```

### 常用日志
```kotlin
import android.util.Log

private val TAG = "ClassName"
Log.d(TAG, "调试信息")
Log.e(TAG, "错误信息", exception)  // 必须传异常对象
```
