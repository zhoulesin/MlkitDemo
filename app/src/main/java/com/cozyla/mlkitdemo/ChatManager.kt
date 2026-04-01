package com.cozyla.mlkitdemo

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
