---
name: android-error-handling
description: Android 错误处理规范，核心要点
---

# Android 错误处理规范

## 异常捕获原则

### ❌ 禁止空捕获

```kotlin
try {
    doSomething()
} catch (e: Exception) {
    // 什么都不做 - 禁止！
}
```

## 捕获具体异常

```kotlin
try {
    file.readText()
} catch (e: FileNotFoundException) {
    showError("文件不存在")
} catch (e: IOException) {
    showError("读取失败")
}
```

## 协程错误处理

```kotlin
fun loadData() {
    viewModelScope.launch {
        try {
            _state.value = UiState.Loading
            val data = withContext(Dispatchers.IO) {
                repository.getData()
            }
            _state.value = UiState.Success(data)
        } catch (e: IOException) {
            _state.value = UiState.Error("网络错误")
        } catch (e: Exception) {
            _state.value = UiState.Error("未知错误")
        }
    }
}
```

## 统一状态管理

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## 用户提示

| 错误类型 | 提示方式 |
|------|------|------|
| 网络错误 | Snackbar + 重试按钮 |
| 表单验证 | Toast 短提示|
| 加载失败 | 页面内错误视图| 

```kotlin
// 网络错误用 Snackbar
Snackbar.make(root, "网络错误", Snackbar.LENGTH_INDEFINITE)
    .setAction("重试") { retry() }
    .show()

// 简单提示用 Toast
Toast.makeText(context, "请输入用户名", Toast.LENGTH_SHORT).show()
```

## 日志记录
```kotlin
catch (e: Exception) {
    Log.e(TAG, "loadData failed", e)  // 必须记录异常
    _state.value = UiState.Error("加载失败")
}
```