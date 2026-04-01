# Day 7：火山引擎多轮对话上下文管理

## 7.1 上下文管理概述

多轮对话需要维护对话历史，让 AI 理解上下文。需要考虑：
- 历史消息的存储
- Token 限制和历史裁剪
- 系统提示词（System Prompt）
- 对话重置功能

## 7.2 依赖配置

确保已有以下依赖：

```kotlin
dependencies {
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    // DataStore（可选，用于持久化对话历史）
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

## 7.3 布局更新

添加清空对话按钮：

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <Button
        android:id="@+id/btn_send"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="发送"/>

    <Button
        android:id="@+id/btn_clear"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:text="清空"/>
</LinearLayout>
```

## 7.4 核心代码实现

**创建 ChatManager.kt：**

```kotlin
package com.cozyla.mlkitdemo

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatManager(
    private val maxHistoryTokens: Int = 32000,
    private val systemPrompt: String = "你是豆包，是由字节跳动开发的 AI 助手。"
) {
    private val _messages = mutableListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    /**
     * 添加消息
     */
    fun addMessage(role: String, content: String) {
        _messages.add(ChatMessage(role = role, content = content))
    }

    /**
     * 更新最后一条消息（用于流式输出）
     */
    fun updateLastMessage(content: String) {
        if (_messages.isNotEmpty() && _messages.last().role == "assistant") {
            _messages[_messages.size - 1] = _messages.last().copy(content = content)
        }
    }

    /**
     * 获取用于 API 调用的消息列表（包含系统提示词和裁剪后的历史）
     */
    fun getApiMessages(): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()

        // 添加系统提示词
        if (systemPrompt.isNotEmpty()) {
            result.add(ChatMessage(role = "system", content = systemPrompt))
        }

        // 添加历史消息，从新到旧裁剪
        val historyToAdd = mutableListOf<ChatMessage>()
        var tokenCount = estimateTokens(systemPrompt)

        for (msg in _messages.reversed()) {
            val msgTokens = estimateTokens(msg.content)
            if (tokenCount + msgTokens > maxHistoryTokens) {
                break
            }
            historyToAdd.add(0, msg)
            tokenCount += msgTokens
        }

        result.addAll(historyToAdd)
        return result
    }

    /**
     * 估算 token 数量（简单估算：1 个汉字 ≈ 1 token，1 个英文单词 ≈ 1 token）
     */
    private fun estimateTokens(text: String): Int {
        if (text.isEmpty()) return 0
        var count = 0
        var i = 0
        while (i < text.length) {
            val char = text[i]
            if (char.isLetter() && char.code < 128) {
                // 英文单词
                while (i < text.length && text[i].isLetterOrDigit()) {
                    i++
                }
                count++
            } else if (char.isIdeograph()) {
                // 汉字
                count++
                i++
            } else {
                i++
            }
        }
        return count.coerceAtLeast(1)
    }

    private fun Char.isIdeograph(): Boolean {
        return this.code in 0x4E00..0x9FFF ||
               this.code in 0x3400..0x4DBF ||
               this.code in 0x20000..0x2A6DF
    }

    /**
     * 清空对话
     */
    fun clear() {
        _messages.clear()
    }

    /**
     * 序列化保存
     */
    fun serialize(): String {
        return Json.encodeToString(_messages)
    }

    /**
     * 从序列化恢复
     */
    fun deserialize(data: String) {
        _messages.clear()
        _messages.addAll(Json.decodeFromString<List<ChatMessage>>(data))
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ... 之前的代码 ...

    /** 对话管理器 */
    private lateinit var chatManager: ChatManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化对话管理器
        chatManager = ChatManager(
            maxHistoryTokens = 32000,
            systemPrompt = "你是豆包，是由字节跳动开发的 AI 助手。请用简洁、友好的语言回答问题。"
        )

        // 初始化火山引擎 API
        volcanoApi = VolcanoApiHelper(
            apiKey = "YOUR_API_KEY_HERE",
            apiSecret = "YOUR_SECRET_HERE",
            model = "doubao-pro-32k"
        )

        // ... 之前的按钮绑定 ...

        val btnClear = findViewById<Button>(R.id.btn_clear)

        btnClear.setOnClickListener {
            chatManager.clear()
            updateChatDisplay(tvChat, scrollChat)
        }
    }

    private fun sendMessage(message: String, tvChat: TextView, scrollChat: ScrollView) {
        chatManager.addMessage(role = "user", content = message)
        updateChatDisplay(tvChat, scrollChat)

        lifecycleScope.launch {
            var aiResponse = ""
            chatManager.addMessage(role = "assistant", content = "")

            try {
                val apiMessages = chatManager.getApiMessages().map {
                    com.cozyla.mlkitdemo.ChatMessage(role = it.role, content = it.content)
                }

                volcanoApi.chatStream(apiMessages).collectLatest { chunk ->
                    aiResponse += chunk
                    chatManager.updateLastMessage(aiResponse)
                    updateChatDisplay(tvChat, scrollChat)
                }
            } catch (e: Exception) {
                aiResponse = "出错了：${e.message}"
                chatManager.updateLastMessage(aiResponse)
                updateChatDisplay(tvChat, scrollChat)
            }
        }
    }

    private fun updateChatDisplay(tvChat: TextView, scrollChat: ScrollView) {
        val sb = StringBuilder()
        for (msg in chatManager.messages) {
            val role = when (msg.role) {
                "user" -> "我"
                "assistant" -> "AI"
                "system" -> "系统"
                else -> msg.role
            }
            if (msg.role != "system") {
                sb.append("$role：${msg.content}\n\n")
            }
        }
        tvChat.text = sb.toString()
        scrollChat.post { scrollChat.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    // ... 之前的识别方法 ...
}
```

## 7.5 更新 build.gradle.kts

确保添加了序列化插件：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.0"
}
```

## 7.6 Claude Code 快速实现提示词

```
在这个 Android 项目中实现火山引擎多轮对话上下文管理：

1. 创建 ChatManager.kt 类：
   - 管理对话历史消息列表
   - 支持系统提示词
   - 实现基于 token 的历史消息裁剪（简单估算）
   - 支持清空对话
   - 支持序列化和反序列化

2. 在 activity_main.xml 中添加清空按钮

3. 在 MainActivity.kt 中：
   - 初始化 ChatManager，设置系统提示词
   - 使用 ChatManager 替代简单的 List
   - 实现清空按钮功能
   - 调用 getApiMessages() 获取裁剪后的消息列表
```
