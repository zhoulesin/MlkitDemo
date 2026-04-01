package com.cozyla.mlkitdemo.network

import android.util.Log
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 通用重试帮助类
 */
object RetryHelper {

    private const val TAG = "RetryHelper"

    /**
     * 带重试的 suspend 函数执行
     */
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

    /**
     * 判断是否为网络相关的可重试异常
     */
    fun isNetworkError(e: Exception): Boolean {
        return e is SocketTimeoutException ||
               e is IOException && e.message?.let {
                   it.contains("timeout", ignoreCase = true) ||
                   it.contains("connection", ignoreCase = true) ||
                   it.contains("reset", ignoreCase = true)
               } == true
    }
}
