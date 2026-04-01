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

### 选项一：火山引擎（豆包）
1. 访问：https://www.volcengine.com/
2. 注册/登录账号
3. 进入「控制台」→「AI 中台」→「智能对话」
4. 创建应用，获取 API Key
5. 开通豆包大模型服务

### 选项二：其他 OpenAI 兼容 API
代码支持任何 OpenAI 格式的兼容 API，只需修改 baseUrl 和 model 参数。

## 6.4 项目结构

```
app/src/main/java/com/cozyla/mlkitdemo/
├── network/              # 通用网络层
│   ├── HttpClient.kt      # OkHttp 配置和请求构建
│   └── RetryHelper.kt    # 通用重试逻辑
└── chat/                 # 聊天功能
    ├── ChatManager.kt     # 对话历史管理
    └── VolcanoApiHelper.kt # 火山引擎 API 封装
```

## 6.5 核心代码实现

**创建 network/HttpClient.kt：**

```kotlin
package com.cozyla.mlkitdemo.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

object HttpClient {

    fun createDefaultClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun createPostRequest(
        url: String,
        body: RequestBody,
        apiKey: String? = null,
        contentType: String = "application/json"
    ): Request {
        val builder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", contentType)
            .post(body)

        apiKey?.let {
            builder.addHeader("Authorization", "Bearer $it")
        }

        return builder.build()
    }
}
```

**创建 network/RetryHelper.kt：**

```kotlin
package com.cozyla.mlkitdemo.network

import android.util.Log
import java.io.IOException
import java.net.SocketTimeoutException

object RetryHelper {
    private const val TAG = "RetryHelper"

    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        retryDelayMs: Long = 1000,
        isRetryable: (Exception) -> Boolean = { isNetworkError(it) },
        onRetry: (Int, Exception) -> Unit = { attempt, e ->
            Log.w(TAG, "重试 $attempt/$maxRetries - ${e.javaClass.simpleName}")
        },
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                onRetry(attempt, e)

                if (attempt >= maxRetries) {
                    Log.e(TAG, "所有重试均失败 (共 $maxRetries 次)")
                    break
                }

                if (!isRetryable(e)) {
                    Log.e(TAG, "非重试异常，停止重试", e)
                    throw e
                }

                Log.d(TAG, "等待 ${retryDelayMs}ms 后重试...")
                kotlinx.coroutines.delay(retryDelayMs)
            }
        }

        throw lastException ?: IOException("请求失败")
    }

    fun isNetworkError(e: Exception): Boolean {
        return e is SocketTimeoutException ||
               e is IOException && e.message?.let {
                   it.contains("timeout", ignoreCase = true) ||
                   it.contains("connection", ignoreCase = true)
               } == true
    }
}
```

**创建 chat/ChatManager.kt：**

```kotlin
package com.cozyla.mlkitdemo.chat

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatManager(
    private val maxHistoryChars: Int = 64000,
    private val systemPrompt: String = "你是豆包，是由字节跳动开发的 AI 助手。"
) {
    private val _messages = mutableListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    fun addMessage(role: String, content: String) {
        _messages.add(ChatMessage(role = role, content = content))
    }

    fun updateLastMessage(content: String) {
        if (_messages.isNotEmpty() && _messages.last().role == "assistant") {
            _messages[_messages.size - 1] = _messages.last().copy(content = content)
        }
    }

    fun getApiMessages(): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()

        if (systemPrompt.isNotEmpty()) {
            result.add(ChatMessage(role = "system", content = systemPrompt))
        }

        var charCount = systemPrompt.length
        val historyToAdd = mutableListOf<ChatMessage>()

        for (msg in _messages.reversed()) {
            if (charCount + msg.content.length > maxHistoryChars) {
                break
            }
            historyToAdd.add(0, msg)
            charCount += msg.content.length
        }

        result.addAll(historyToAdd)
        return result
    }

    fun clear() {
        _messages.clear()
    }

    fun getDisplayText(): String {
        val sb = StringBuilder()
        for (msg in _messages) {
            val role = when (msg.role) {
                "user" -> "我"
                "assistant" -> "AI"
                else -> msg.role
            }
            if (msg.role != "system") {
                sb.append("$role：${msg.content}\n\n")
            }
        }
        return sb.toString()
    }
}
```

**创建 chat/VolcanoApiHelper.kt：**

```kotlin
package com.cozyla.mlkitdemo.chat

import android.util.Log
import com.cozyla.mlkitdemo.network.HttpClient
import com.cozyla.mlkitdemo.network.RetryHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class VolcanoApiHelper(
    private val apiKey: String,
    private val model: String,
    private val baseUrl: String,
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 1000
) {
    private val mediaType = "application/json".toMediaType()
    private val TAG = "VolcanoApiHelper"

    private val client = HttpClient.createDefaultClient()

    suspend fun chatNonStream(messages: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        RetryHelper.executeWithRetry(
            maxRetries = maxRetries,
            retryDelayMs = retryDelayMs
        ) {
            doChatNonStreamRequest(messages)
        }
    }

    private fun doChatNonStreamRequest(messages: List<ChatMessage>): String {
        Log.d(TAG, "=== 开始请求 ===")
        Log.d(TAG, "URL: $baseUrl")
        Log.d(TAG, "Model: $model")

        val jsonRequest = buildChatRequest(messages, stream = false)
        Log.d(TAG, "Request: $jsonRequest")

        val request = HttpClient.createPostRequest(
            url = baseUrl,
            body = jsonRequest.toString().toRequestBody(mediaType),
            apiKey = apiKey
        )

        return client.newCall(request).execute().use { response ->
            Log.d(TAG, "Response code: ${response.code}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "无错误信息"
                Log.e(TAG, "Error body: $errorBody")
                throw IOException("请求失败：${response.code} - ${response.message}\n$errorBody")
            }

            val responseBody = response.body?.string() ?: ""
            Log.d(TAG, "Response body: $responseBody")

            parseChatResponse(responseBody)
        }
    }

    private fun buildChatRequest(messages: List<ChatMessage>, stream: Boolean): JSONObject {
        return JSONObject().apply {
            put("model", model)
            put("stream", stream)
            put("messages", JSONArray(messages.map { msg ->
                JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                }
            }))
        }
    }

    private fun parseChatResponse(responseBody: String): String {
        val json = JSONObject(responseBody)
        val choices = json.optJSONArray("choices")
        return if (choices != null && choices.length() > 0) {
            val choice = choices.getJSONObject(0)
            val message = choice.optJSONObject("message")
            val content = message?.optString("content") ?: "未获取到回复"
            Log.d(TAG, "Extracted content: $content")
            content
        } else {
            Log.e(TAG, "No choices in response")
            "未获取到回复"
        }
    }
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
import com.cozyla.mlkitdemo.chat.ChatManager
import com.cozyla.mlkitdemo.chat.VolcanoApiHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvChat: TextView
    private lateinit var scrollChat: ScrollView
    private lateinit var volcanoApi: VolcanoApiHelper
    private lateinit var chatManager: ChatManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initHelpers()
        initViews()
    }

    private fun initHelpers() {
        chatManager = ChatManager(
            maxHistoryChars = 64000,
            systemPrompt = "你是豆包，是由字节跳动开发的 AI 助手。"
        )

        volcanoApi = VolcanoApiHelper(
            apiKey = "YOUR_API_KEY_HERE",
            model = "doubao-seed-2-0-lite-260215",
            baseUrl = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
        )
    }

    private fun initViews() {
        tvChat = findViewById(R.id.tv_chat)
        scrollChat = findViewById(R.id.scroll_chat)
        val etMessage = findViewById<EditText>(R.id.et_message)

        findViewById<Button>(R.id.btn_send).setOnClickListener {
            val message = etMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                etMessage.text.clear()
            }
        }

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            chatManager.clear()
            updateChatDisplay()
        }
    }

    private fun sendMessage(message: String) {
        chatManager.addMessage(role = "user", content = message)
        updateChatDisplay()

        lifecycleScope.launch {
            try {
                val apiMessages = chatManager.getApiMessages()
                val aiResponse = volcanoApi.chatNonStream(apiMessages)
                chatManager.addMessage(role = "assistant", content = aiResponse)
                updateChatDisplay()
            } catch (e: Exception) {
                val errorMsg = "出错了：${e.message}"
                chatManager.addMessage(role = "assistant", content = errorMsg)
                updateChatDisplay()
            }
        }
    }

    private fun updateChatDisplay() {
        tvChat.text = chatManager.getDisplayText()
        scrollChat.post { scrollChat.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
```

## 6.6 Claude Code 快速实现提示词

```
在这个 Android 项目中实现火山引擎（豆包）API 聊天功能：

1. 在 app/build.gradle.kts 中添加依赖：
   - OkHttp 4.12.0
   - Kotlin Coroutines Android 1.7.3

2. 创建 network/ 包下的通用类：
   - HttpClient.kt - OkHttp 配置和请求构建
   - RetryHelper.kt - 通用重试逻辑

3. 创建 chat/ 包下的聊天类：
   - ChatManager.kt - 对话历史管理
   - VolcanoApiHelper.kt - 火山引擎 API 封装

4. 在 MainActivity.kt 中：
   - 初始化 ChatManager 和 VolcanoApiHelper
   - 实现聊天发送和显示
```
