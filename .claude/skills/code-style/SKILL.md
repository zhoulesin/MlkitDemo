---
name: android-code-style
description: Android Kotlin 代码风格规范，核心要点
---

# Android 代码风格规范

## 核心原则

### 1. 变量
- 优先用 `val`，少用 `var`
- 类型能推断就不写

```kotlin
val userName = "John"  // ✅
var count = 0          // 可变时才用 var
```

### 2. 空安全
- 用`?.`安全调用
- 用`?:`提供默认值
- 禁止用`!!`

```kotlin
textView.text = user?.name ?: "未知"  // ✅
textView.text = user!!.name           // ❌
```

### 3. 作用域函数

- let：处理可空值
- apply：初始化对象 

```kotlin
user?.let { save(it) }                    // ✅ 处理可空
Intent().apply { putExtra("id", userId) } // ✅ 初始化
```

### 4. 条件语句
- 多分支用 when，不用 if-else if
```kotlin
when (status) {
    200 -> "成功"
    404 -> "未找到"
    else -> "错误"
}
```

### 5. 数据类

- 数据持有用 data class
- 状态用 sealed class

```kotlin
    data class User(val id: Long, val name: String)
    sealed class Result {
        object Loading : Result()
        data class Success(val data: User) : Result()
        data class Error(val msg: String) : Result()
    }
```

### 6. 协程

- ViewModel 用 viewModelScope
- IO 操作切到 Dispatchers.IO
```kotlin
viewModelScope.launch {
    val data = withContext(Dispatchers.IO) {
        repository.getData()
    }
    updateUi(data)
}
```

## 资源文件规范

### 布局

- 根布局用 ConstraintLayout
- ID 格式：控件类型_含义（btn_login, tv_title）

### 字符串/颜色/尺寸

- 全部写在 XML 文件，禁止硬编码
```xml
    <!-- ✅ -->
    <TextView>
    android:text="@string/login"
    android:textColor="@color/primary"
    android:textSize="@dimen/text_normal"
    </TextView>

    <!-- ❌ -->
    <TextView>
    android:text="登录"
    android:textColor="#FF0000"
    android:textSize="16sp"
    </TextView>
```

## 禁止事项

- ❌ 使用 !! 强制解包
- ❌ 硬编码字符串、颜色、尺寸
- ❌ 布局层级超过 5 层
- ❌ 在循环中拼接字符串（用 StringBuilder）
