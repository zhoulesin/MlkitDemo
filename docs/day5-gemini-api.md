# Day 5：Google Gemini API 接入（云端大模型、聊天）

## 5.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
}
```

## 5.2 布局更新

添加聊天界面：

```xml
<EditText
    android:id="@+id/et_message"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:hint="输入消息..."
    android:inputType="textMultiLine"
    android:minHeight="80dp"/>

<Button
    android:id="@+id/btn_send"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:text="发送"/>

<ScrollView
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

## 5.3 核心代码实现

**MainActivity.kt - Gemini API 部分：**

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

    // ... 之前的代码 ...

    /** Gemini 对话历史 */
    private val chatHistory = StringBuilder()

    /** 协程作用域 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Gemini API Key - 需要在 https://aistudio.google.com/app/apikey 获取 */
    private val apiKey = "YOUR_API_KEY_HERE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ... 之前的按钮绑定 ...

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

    // ... 之前的识别方法 ...
}
```

## 5.4 获取 API Key

1. 访问 https://aistudio.google.com/app/apikey
2. 使用 Google 账号登录
3. 点击"Create API Key"
4. 创建或选择一个 Google Cloud 项目
5. 复制生成的 API Key，替换代码中的 `YOUR_API_KEY_HERE`

## 5.5 添加网络权限

在 `AndroidManifest.xml` 中添加：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

## 5.6 完整依赖配置

最终 `app/build.gradle.kts` 中的 dependencies：

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ML Kit - 中文文本识别
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
    // ML Kit - 图像分类
    implementation("com.google.mlkit:image-labeling:17.0.9")
    // ML Kit - 人脸检测
    implementation("com.google.mlkit:face-detection:16.1.7")
    // Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
}
```

## 5.7 Claude Code 快速实现提示词

将以下提示词复制给 Claude Code 即可自动实现 Gemini API 聊天功能：

```
在这个 Android 项目中实现 Google Gemini API 聊天功能：

1. 在 app/build.gradle.kts 中添加依赖：
   implementation("com.google.ai.client.generativeai:generativeai:0.7.0")

2. 在 activity_main.xml 中添加：
   - EditText (id: et_message) 用于输入消息
   - Button (id: btn_send) 发送按钮
   - ScrollView + TextView (id: tv_chat) 显示聊天历史

3. 在 AndroidManifest.xml 中添加 INTERNET 权限

4. 在 MainActivity.kt 中：
   - 添加 import：com.google.ai.client.generativeai.*
   - 添加协程相关代码
   - 使用 Gemini 2.0 Flash 模型
   - 实现 sendMessage() 方法，将用户消息和 AI 回复显示在聊天历史中
   - 提示用户需要在 https://aistudio.google.com/app/apikey 获取 API Key
```
