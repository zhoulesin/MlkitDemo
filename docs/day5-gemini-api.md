# Day 5：云端大模型 API 接入（火山引擎替代 Gemini）

> ⚠️ **注意**：由于国内网络环境无法访问 Google Gemini，本项目实际使用**火山引擎（豆包）**API 替代。本文档同时保留 Gemini 作为参考，实际实现请参考 Day 6-7。

## 5.1 方案对比

### 选项 A：火山引擎（豆包）- 推荐 ⭐
- 国内访问稳定快速
- OpenAI 兼容 API 格式
- 提供豆包大模型（Doubao）
- 控制台：https://www.volcengine.com/

### 选项 B：Google Gemini
- 需要访问 Google 服务
- 官方 SDK 集成简单
- 控制台：https://aistudio.google.com/

---

## 火山引擎实现（实际使用）

### 依赖配置

```kotlin
dependencies {
    // OkHttp - 用于网络请求
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### 快速开始

完整实现请参考：
- **Day 6**：火山引擎 API 接入 + 流式输出
- **Day 7**：多轮对话上下文管理

核心文件结构：
```
app/src/main/java/com/cozyla/mlkitdemo/
├── network/              # 通用网络层
│   ├── HttpClient.kt      # OkHttp 配置
│   └── RetryHelper.kt    # 重试机制
└── chat/                 # 聊天功能
    ├── ChatManager.kt     # 对话管理
    └── VolcanoApiHelper.kt # API 封装
```

---

## Gemini API 实现（参考）

### 依赖配置

```kotlin
dependencies {
    // Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
}
```

### 核心代码示例

```kotlin
package com.cozyla.mlkitdemo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    /** Gemini 对话历史 */
    private val chatHistory = StringBuilder()

    /** 协程作用域 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Gemini API Key - 需要在 https://aistudio.google.com/app/apikey 获取 */
    private val apiKey = "YOUR_API_KEY_HERE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<Button>(R.id.btn_send)
        val tvChat = findViewById<TextView>(R.id.tv_chat)

        btnSend.setOnClickListener {
            val message = etMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message, tvChat)
                etMessage.text.clear()
            }
        }
    }

    private fun sendMessage(message: String, tvChat: TextView) {
        chatHistory.append("我：$message\n\n")
        tvChat.text = chatHistory.toString()

        scope.launch {
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = apiKey,
                    generationConfig = generationConfig {
                        temperature = 0.9f
                        topK = 1
                        topP = 1f
                        maxOutputTokens = 2048
                    }
                )

                val response = generativeModel.generateContent(message)
                val aiResponse = response.text ?: "未获取到回复"

                chatHistory.append("AI：$aiResponse\n\n")
                tvChat.text = chatHistory.toString()
            } catch (e: Exception) {
                chatHistory.append("AI：出错了 - ${e.message}\n\n")
                tvChat.text = chatHistory.toString()
            }
        }
    }
}
```

### 获取 API Key（Gemini）

1. 访问 https://aistudio.google.com/app/apikey
2. 使用 Google 账号登录
3. 点击"Create API Key"
4. 创建或选择一个 Google Cloud 项目
5. 复制生成的 API Key

### 添加网络权限

在 `AndroidManifest.xml` 中添加：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```
