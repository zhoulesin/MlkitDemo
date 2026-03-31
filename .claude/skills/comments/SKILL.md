---
name: android-comments
description: Android 代码注释规范，核心要点
---

# Android 注释规范

## 类注释
每个类都要有，说明职责

```kotlin
/**
 * 用户登录页面
 * 处理用户名密码验证和登录请求
 */
class LoginActivity : AppCompatActivity()
```

## 方法注释
公共方法必须有注释
```kotlin
/**
 * 验证用户输入
 * @param username 用户名
 * @param password 密码
 * @return 是否验证通过
 */
fun validate(username: String, password: String): Boolean
```

## 字段注释
需要说明用途的字段

```kotlin
/** 最大重试次数 */
private const val MAX_RETRY = 3

/** ViewModel 实例 */
private lateinit var viewModel: LoginViewModel
```

## TODO 格式

```kotlin
// TODO(作者): 待办事项
// TODO(张三): 添加网络重试机制
// FIXME(李四): 修复 Android 12 崩溃问题
```

# 注释原则
- 好代码自解释，注释说明"为什么"而非"是什么"
- 无意义注释不要写（❌ // i++ 让i加1）
- TODO 及时处理，不留到生产环境
- 修改代码时同步更新注释