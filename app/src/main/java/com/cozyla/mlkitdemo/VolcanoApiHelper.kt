package com.cozyla.mlkitdemo

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class VolcanoApiHelper(
    private val apiKey: String,
    private val model: String,
    private val baseUrl: String
) {
    private val client = OkHttpClient()
    private val mediaType = "application/json".toMediaType()
    private val TAG = "VolcanoApiHelper"

    /**
     * 非流式聊天
     */
    suspend fun chatNonStream(messages: List<Map<String, String>>): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "=== 开始请求 ===")
        Log.d(TAG, "URL: $baseUrl")
        Log.d(TAG, "Model: $model")
        Log.d(TAG, "API Key: ${apiKey.take(10)}...")

        val jsonRequest = JSONObject().apply {
            put("model", model)
            put("stream", false)
            put("messages", JSONArray(messages.map { msg ->
                JSONObject().apply {
                    put("role", msg["role"])
                    put("content", msg["content"])
                }
            }))
        }

        Log.d(TAG, "Request: $jsonRequest")

        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonRequest.toString().toRequestBody(mediaType))
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response message: ${response.message}")

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "无错误信息"
                    Log.e(TAG, "Error body: $errorBody")
                    throw IOException("请求失败：${response.code} - ${response.message}\n$errorBody")
                }

                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Response body: $responseBody")

                val json = JSONObject(responseBody)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
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
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred", e)
            throw e
        }
    }

    /**
     * 流式聊天
     */
    fun chatStream(messages: List<Map<String, String>>): Flow<String> = flow {
        Log.d(TAG, "=== 开始流式请求 ===")
        Log.d(TAG, "URL: $baseUrl")
        Log.d(TAG, "Model: $model")

        val jsonRequest = JSONObject().apply {
            put("model", model)
            put("stream", true)
            put("messages", JSONArray(messages.map { msg ->
                JSONObject().apply {
                    put("role", msg["role"])
                    put("content", msg["content"])
                }
            }))
        }

        Log.d(TAG, "Request: $jsonRequest")

        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonRequest.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            Log.d(TAG, "Response code: ${response.code}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "Error: $errorBody")
                throw IOException("请求失败：${response.code} - ${response.message}\n$errorBody")
            }

            val responseBody = response.body?.string() ?: ""
            Log.d(TAG, "Full response: $responseBody")

            val lines = responseBody.split("\n")

            for (line in lines) {
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    if (data.isNotEmpty()) {
                        try {
                            val json = JSONObject(data)
                            val choices = json.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val choice = choices.getJSONObject(0)
                                val delta = choice.optJSONObject("delta")
                                val content = delta?.optString("content")
                                if (content != null && content.isNotEmpty()) {
                                    emit(content)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Parse error", e)
                        }
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
