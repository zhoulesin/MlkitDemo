# Day 7：火山引擎多轮对话上下文管理

## 7.1 上下文管理概述

多轮对话需要维护对话历史，让 AI 理解上下文。需要考虑：
- 历史消息的存储
- 字符限制和历史裁剪（简化实现）
- 系统提示词（System Prompt）
- 对话重置功能

## 7.2 依赖配置

确保已有以下依赖：

```kotlin
dependencies {
    // OkHttp - 用于网络请求
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

## 7.3 项目结构

```
app/src/main/java/com/cozyla/mlkitdemo/
├── network/              # 通用网络层
│   ├── HttpClient.kt      # OkHttp 配置和请求构建
│   └── RetryHelper.kt    # 通用重试逻辑
└── chat/                 # 聊天功能
    ├── ChatManager.kt     # 对话历史管理
    └── VolcanoApiHelper.kt # 火山引擎 API 封装
```

## 7.4 核心代码实现

**ChatManager.kt（已在 chat/ 包下）：**

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
            systemPrompt = "你是豆包，是由字节跳动开发的 AI 助手。请用简洁、友好的语言回答问题。"
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

## 7.5 Claude Code 快速实现提示词

```
在这个 Android 项目中实现火山引擎多轮对话上下文管理：

1. ChatManager.kt 已存在于 chat/ 包下，提供：
   - 管理对话历史消息列表
   - 支持系统提示词
   - 实现基于字符数的历史消息裁剪
   - 支持清空对话
   - 提供 getDisplayText() 格式化输出

2. MainActivity 中：
   - 初始化 ChatManager，设置系统提示词
   - 实现清空按钮功能
   - 调用 getApiMessages() 获取裁剪后的消息列表
```
