# Day 2：纯 Kotlin 实现 离线 OCR 文字识别

## 2.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 中文文本识别
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
}
```

## 2.2 布局文件

`activity_main.xml`：

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:id="@+id/ll_main"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <Button
        android:id="@+id/btn_recognize_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="识别图片文本"/>

    <TextView
        android:id="@+id/tv_result"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        android:textSize="16sp"/>

</LinearLayout>
```

## 2.3 核心代码实现

**MainActivity.kt - 文本识别部分：**

```kotlin
package com.cozyla.mlkitdemo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private val requestCodeTextRecognition = 100
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRecognizeText = findViewById<Button>(R.id.btn_recognize_text)
        tvResult = findViewById(R.id.tv_result)

        btnRecognizeText.setOnClickListener {
            openGallery(requestCodeTextRecognition)
        }
    }

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                if (requestCode == requestCodeTextRecognition) {
                    recognizeText(bitmap)
                }
            }
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(
            ChineseTextRecognizerOptions.Builder().build()
        )

        val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                val resultText = text.text
                tvResult.text = if (resultText.isEmpty()) {
                    "未识别到文本"
                } else {
                    resultText
                }
            }
            .addOnFailureListener { e ->
                tvResult.text = "识别失败：${e.message}"
            }
    }
}
```

## 2.4 实现要点

1. **创建识别器**：使用 `ChineseTextRecognizerOptions` 配置中文识别
2. **构建 InputImage**：将 Bitmap 转换为 ML Kit 的 InputImage
3. **异步处理**：`process()` 方法是异步的，通过回调获取结果
4. **零 NDK/C++**：纯 Kotlin 实现，无需 JNI

## 2.5 Claude Code 快速实现提示词

将以下提示词复制给 Claude Code 即可自动实现中文文本识别功能：

```
在这个 Android 项目中实现 ML Kit 中文文本识别功能：

1. 在 app/build.gradle.kts 中添加依赖：
   implementation("com.google.mlkit:text-recognition-chinese:16.0.1")

2. 在 activity_main.xml 中添加文本识别按钮，id 为 btn_recognize_text，以及结果显示 TextView，id 为 tv_result

3. 在 MainActivity.kt 中：
   - 添加 import：com.google.mlkit.vision.text.*
   - 添加请求码 requestCodeTextRecognition = 100
   - 实现打开相册的方法 openGallery()
   - 在 onActivityResult 中获取图片并调用识别方法
   - 实现 recognizeText() 方法，使用 ChineseTextRecognizerOptions 进行中文文本识别，输出识别结果或失败信息
```
