# Day 6：火山引擎（豆包）API 接入 + 流式输出（打字机效果）

## 6.1 火山引擎简介

火山引擎（Volcano Engine）是字节跳动推出的云服务平台，提供豆包（Doubao）大模型 API，国内访问稳定快速。

## 6.2 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // OkHttp - 用于网络请求
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

## 6.3 获取 API Key

### 选项一：硅基流动（推荐，国内稳定）
1. 访问：https://siliconflow.cn/
2. 注册/登录账号
3. 进入「API Key 管理」创建 API Key
4. 可使用模型：Qwen/Qwen2/Qwen3 系列、Llama 系列等

### 选项二：火山引擎（豆包）
1. 访问：https://www.volcengine.com/
2. 注册/登录账号
3. 进入「控制台」→「AI 中台」→「智能对话」
4. 创建应用，获取 API Key
5. 开通豆包大模型服务

### 选项三：其他 OpenAI 兼容 API
代码支持任何 OpenAI 格式的兼容 API，只需修改 baseUrl 和 model 参数。

## 6.4 布局更新

将聊天界面修改为支持流式输出：

```xml
<ScrollView
    android:id="@+id/scroll_chat"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1"
    android:layout_marginTop="12dp">

    <TextView
        android:id="@+id/tv_chat"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="16sp"/>
</ScrollView>
```

## 6.5 核心代码实现

**创建 VolcanoApiHelper.kt：**

```kotlin
package com.cozyla.mlkitdemo

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.IOException

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>? = null
)

@Serializable
data class Choice(
    val message: Message? = null,
    val delta: Delta? = null
)

@Serializable
data class Message(
    val content: String? = null
)

@Serializable
data class Delta(
    val content: String? = null
)

class VolcanoApiHelper(
    private val apiKey: String,
    private val apiSecret: String,
    private val model: String = "doubao-pro-32k"
) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    private val baseUrl = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"

    fun chatStream(messages: List<ChatMessage>): Flow<String> = flow {
        val requestBody = ChatRequest(
            model = model,
            messages = messages,
            stream = true
        )

        val jsonBody = json.encodeToJsonElement(requestBody).toString()
        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("请求失败：${response.code}")
            }

            val responseBody = response.body?.string() ?: ""
            val lines = responseBody.split("\n")

            for (line in lines) {
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    if (data.isNotEmpty()) {
                        try {
                            val chatResponse = json.decodeFromString<ChatResponse>(data)
                            val content = chatResponse.choices?.firstOrNull()?.delta?.content
                            if (content != null) {
                                emit(content)
                            }
                        } catch (e: Exception) {
                            // 忽略解析错误
                        }
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
```

**MainActivity.kt 中的使用：**

```kotlin
package com.cozyla.mlkitdemo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ... 之前的代码 ...

    /** 火山引擎 API 助手 */
    private lateinit var volcanoApi: VolcanoApiHelper

    /** 对话历史 */
    private val messageHistory = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化火山引擎 API（替换为你的 API Key）
        volcanoApi = VolcanoApiHelper(
            apiKey = "YOUR_API_KEY_HERE",
            apiSecret = "YOUR_SECRET_HERE",
            model = "doubao-pro-32k"
        )

        // ... 之前的按钮绑定 ...

        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<Button>(R.id.btn_send)
        val tvChat = findViewById<TextView>(R.id.tv_chat)
        val scrollChat = findViewById<ScrollView>(R.id.scroll_chat)

        btnSend.setOnClickListener {
            val message = etMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message, tvChat, scrollChat)
                etMessage.text.clear()
            }
        }
    }

    private fun sendMessage(message: String, tvChat: TextView, scrollChat: ScrollView) {
        messageHistory.add(ChatMessage(role = "user", content = message))
        updateChatDisplay(tvChat, scrollChat)

        lifecycleScope.launch {
            var aiResponse = ""
            messageHistory.add(ChatMessage(role = "assistant", content = ""))

            try {
                volcanoApi.chatStream(messageHistory.dropLast(1)).collectLatest { chunk ->
                    aiResponse += chunk
                    messageHistory[messageHistory.size - 1] = 
                        ChatMessage(role = "assistant", content = aiResponse)
                    updateChatDisplay(tvChat, scrollChat)
                }
            } catch (e: Exception) {
                aiResponse = "出错了：${e.message}"
                messageHistory[messageHistory.size - 1] = 
                    ChatMessage(role = "assistant", content = aiResponse)
                updateChatDisplay(tvChat, scrollChat)
            }
        }
    }

    private fun updateChatDisplay(tvChat: TextView, scrollChat: ScrollView) {
        val sb = StringBuilder()
        for (msg in messageHistory) {
            val role = if (msg.role == "user") "我" else "AI"
            sb.append("$role：${msg.content}\n\n")
        }
        tvChat.text = sb.toString()
        scrollChat.post { scrollChat.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    // ... 之前的识别方法 ...
}
```

## 6.6 添加序列化插件

在 `app/build.gradle.kts` 中添加：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.0"
}
```

在项目根目录的 `build.gradle.kts` 中添加：

```kotlin
plugins {
    kotlin("jvm") version "1.9.0" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false
}
```

## 6.7 Claude Code 快速实现提示词

```
在这个 Android 项目中实现火山引擎（豆包）API 流式聊天功能：

1. 在 app/build.gradle.kts 中添加依赖：
   - OkHttp 4.12.0
   - Kotlin Coroutines Android 1.7.3
   - Kotlinx Serialization

2. 创建 VolcanoApiHelper.kt 单例类：
   - 使用 SSE 流式调用火山引擎 API
   - 支持 emit 逐个 token
   - 使用 doubao-pro-32k 模型

3. 在 activity_main.xml 中：
   - 给 ScrollView 添加 id
   - 保持聊天界面布局

4. 在 MainActivity.kt 中：
   - 初始化 VolcanoApiHelper
   - 实现流式输出，打字机效果
   - 自动滚动到底部
```
