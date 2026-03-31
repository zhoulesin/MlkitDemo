---
name: android-logging
description: Android 日志规范，核心要点
---

# Android 日志规范

## 日志级别

| 级别 | 方法 | 用途 |
|------|------|------|
| Debug | `Log.d()` | 调试信息，变量值 |
| Info | `Log.i()` | 关键流程（登录成功） |
| Warning | `Log.w()` | 潜在问题 |
| Error | `Log.e()` | 异常错误，必须记录堆栈 |

## TAG 命名

```kotlin
// 使用类名
private const val TAG = "LoginViewModel"
private val TAG = this::class.java.simpleName
```

## 日志格式
```kotlin
// 格式：方法名: 描述, 参数
Log.d(TAG, "loadUser: started, userId=$userId")
Log.d(TAG, "loadUser: completed, user=$user")
Log.e(TAG, "loadUser: failed, userId=$userId", exception)
```

## 必须打印的场景
- 网络请求（URL、状态码、耗时）
- 异常捕获（完整堆栈）
- 关键生命周期（onCreate、onResume）
- 用户操作（按钮点击）

## 敏感信息脱敏
```kotlin
// 手机号：138****8000
fun maskPhone(phone: String) = phone.replace(Regex("(\\d{3})\\d{4}(\\d{4})"), "$1****$2")

// Token：只显示前4后4
fun maskToken(token: String) = "${token.take(4)}...${token.takeLast(4)}"

// 使用
Log.d(TAG, "login: phone=${maskPhone(phone)}")
```

## 禁止场景
- ❌ 循环中打印日志
- ❌ 高频回调中打印（onScroll）
- ❌ 打印密码、完整 Token