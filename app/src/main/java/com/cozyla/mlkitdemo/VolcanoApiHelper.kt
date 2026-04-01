package com.cozyla.mlkitdemo

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

/**
 * 火山引擎（豆包）API 帮助类
 * 职责单一：仅负责 API 调用和响应解析
 */
class VolcanoApiHelper(
    private val apiKey: String,
    private val model: String,
    private val baseUrl: String,
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 1000
) {
    private val mediaType = "application/json".toMediaType()
    private val TAG = "VolcanoApiHelper"

    // 使用通用的 OkHttp 客户端
    private val client = HttpClient.createDefaultClient()

    /**
     * 非流式聊天
     */
    suspend fun chatNonStream(messages: List<Map<String, String>>): String = withContext(Dispatchers.IO) {
        RetryHelper.executeWithRetry(
            maxRetries = maxRetries,
            retryDelayMs = retryDelayMs
        ) {
            doChatNonStreamRequest(messages)
        }
    }

    /**
     * 执行实际的非流式请求
     */
    private fun doChatNonStreamRequest(messages: List<Map<String, String>>): String {
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

    /**
     * 流式聊天
     */
    fun chatStream(messages: List<Map<String, String>>): Flow<String> = flow {
        RetryHelper.executeWithRetry(
            maxRetries = maxRetries,
            retryDelayMs = retryDelayMs
        ) {
            doChatStreamRequest(messages, this@flow)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 执行实际的流式请求
     */
    private suspend fun doChatStreamRequest(
        messages: List<Map<String, String>>,
        flowCollector: kotlinx.coroutines.flow.FlowCollector<String>
    ) {
        Log.d(TAG, "=== 开始流式请求 ===")

        val jsonRequest = buildChatRequest(messages, stream = true)
        val request = HttpClient.createPostRequest(
            url = baseUrl,
            body = jsonRequest.toString().toRequestBody(mediaType),
            apiKey = apiKey
        )

        client.newCall(request).execute().use { response ->
            Log.d(TAG, "Response code: ${response.code}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "Error: $errorBody")
                throw IOException("请求失败：${response.code} - ${response.message}\n$errorBody")
            }

            val responseBody = response.body?.string() ?: ""
            val lines = responseBody.split("\n")

            for (line in lines) {
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    if (data.isNotEmpty()) {
                        parseStreamChunk(data)?.let { content ->
                            flowCollector.emit(content)
                        }
                    }
                }
            }
        }
    }

    /**
     * 构建聊天请求 JSON
     */
    private fun buildChatRequest(messages: List<Map<String, String>>, stream: Boolean): JSONObject {
        return JSONObject().apply {
            put("model", model)
            put("stream", stream)
            put("messages", JSONArray(messages.map { msg ->
                JSONObject().apply {
                    put("role", msg["role"])
                    put("content", msg["content"])
                }
            }))
        }
    }

    /**
     * 解析非流式响应
     */
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

    /**
     * 解析流式响应块
     */
    private fun parseStreamChunk(data: String): String? {
        return try {
            val json = JSONObject(data)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val delta = choice.optJSONObject("delta")
                delta?.optString("content")?.takeIf { it.isNotEmpty() }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error", e)
            null
        }
    }
}
