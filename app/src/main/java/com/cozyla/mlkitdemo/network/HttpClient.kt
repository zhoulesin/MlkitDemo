package com.cozyla.mlkitdemo.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

/**
 * 通用 OkHttp 客户端配置
 */
object HttpClient {

    /**
     * 创建默认配置的 OkHttpClient
     */
    fun createDefaultClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * 创建带自定义超时的 OkHttpClient
     */
    fun createClient(
        connectTimeoutSec: Long = 60,
        readTimeoutSec: Long = 120,
        writeTimeoutSec: Long = 60,
        retryOnConnectionFailure: Boolean = true
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
            .retryOnConnectionFailure(retryOnConnectionFailure)
            .build()
    }

    /**
     * 创建 POST 请求
     */
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
