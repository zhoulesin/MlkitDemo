# Day 2：纯 Kotlin 实现 离线 OCR 文字识别

## 2.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 中文文本识别
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
}
```

## 2.2 项目结构

```
app/src/main/java/com/cozyla/mlkitdemo/
└── mlkit/
    ├── GalleryHelper.kt        # 相册选择
    └── TextRecognitionHelper.kt # 文本识别
```

## 2.3 核心代码实现

**创建 mlkit/GalleryHelper.kt：**

```kotlin
package com.cozyla.mlkitdemo.mlkit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

class GalleryHelper(private val activity: Activity) {

    fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, requestCode)
    }

    fun getBitmap(data: Intent?): Bitmap? {
        val uri: Uri? = data?.data
        return uri?.let {
            MediaStore.Images.Media.getBitmap(activity.contentResolver, it)
        }
    }
}
```

**创建 mlkit/TextRecognitionHelper.kt：**

```kotlin
package com.cozyla.mlkitdemo.mlkit

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class TextRecognitionHelper {

    fun recognizeText(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                val resultText = text.text
                onSuccess(if (resultText.isEmpty()) "未识别到文本" else resultText)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
```

**MainActivity.kt 中的使用：**

```kotlin
package com.cozyla.mlkitdemo

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cozyla.mlkitdemo.mlkit.GalleryHelper
import com.cozyla.mlkitdemo.mlkit.TextRecognitionHelper

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_TEXT = 100
    }

    private lateinit var tvResult: TextView
    private val galleryHelper by lazy { GalleryHelper(this) }
    private val textRecognitionHelper by lazy { TextRecognitionHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tv_result)

        findViewById<Button>(R.id.btn_recognize_text).setOnClickListener {
            galleryHelper.openGallery(REQUEST_CODE_TEXT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        val bitmap = galleryHelper.getBitmap(data) ?: return

        when (requestCode) {
            REQUEST_CODE_TEXT -> processText(bitmap)
        }
    }

    private fun processText(bitmap: Bitmap) {
        textRecognitionHelper.recognizeText(
            bitmap = bitmap,
            onSuccess = { tvResult.text = it },
            onFailure = { tvResult.text = "识别失败：${it.message}" }
        )
    }
}
```

## 2.4 实现要点

1. **单一职责**：每个 Helper 类只负责一个功能
2. **回调接口**：使用 `onSuccess` / `onFailure` 回调处理结果
3. **懒初始化**：使用 `by lazy` 延迟加载 Helper 实例
4. **零 NDK/C++**：纯 Kotlin 实现，无需 JNI

## 2.5 Claude Code 快速实现提示词

```
在这个 Android 项目中实现 ML Kit 中文文本识别功能：

1. 创建 mlkit/ 包结构

2. 创建 GalleryHelper.kt：
   - 封装相册选择逻辑
   - 提供 openGallery() 和 getBitmap() 方法

3. 创建 TextRecognitionHelper.kt：
   - 封装中文文本识别逻辑
   - 使用回调 onSuccess/onFailure 返回结果

4. 在 MainActivity.kt 中：
   - 使用 by lazy 初始化 Helpers
   - 调用 Helpers 实现文本识别功能
```
