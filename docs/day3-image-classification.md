# Day 3：纯 Kotlin 离线图像分类

## 3.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 图像分类
    implementation("com.google.mlkit:image-labeling:17.0.9")
}
```

## 3.2 项目结构

```
app/src/main/java/com/cozyla/mlkitdemo/
└── mlkit/
    ├── GalleryHelper.kt          # 相册选择
    ├── TextRecognitionHelper.kt   # 文本识别
    └── ImageLabelingHelper.kt     # 图像分类
```

## 3.3 核心代码实现

**创建 mlkit/ImageLabelingHelper.kt：**

```kotlin
package com.cozyla.mlkitdemo.mlkit

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ImageLabelingHelper {

    fun classifyImage(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val sb = StringBuilder()
                for (label in labels) {
                    val name = label.text
                    val confidence = label.confidence
                    sb.append("类别：$name，置信度：${"%.2f".format(confidence)}\n")
                }
                onSuccess(if (sb.isEmpty()) "未识别到内容" else sb.toString())
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
import com.cozyla.mlkitdemo.mlkit.ImageLabelingHelper

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_TEXT = 100
        private const val REQUEST_CODE_IMAGE = 101
    }

    private lateinit var tvResult: TextView
    private val galleryHelper by lazy { GalleryHelper(this) }
    private val textRecognitionHelper by lazy { TextRecognitionHelper() }
    private val imageLabelingHelper by lazy { ImageLabelingHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tv_result)

        findViewById<Button>(R.id.btn_recognize_text).setOnClickListener {
            galleryHelper.openGallery(REQUEST_CODE_TEXT)
        }

        findViewById<Button>(R.id.btn_classify_image).setOnClickListener {
            galleryHelper.openGallery(REQUEST_CODE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        val bitmap = galleryHelper.getBitmap(data) ?: return

        when (requestCode) {
            REQUEST_CODE_TEXT -> processText(bitmap)
            REQUEST_CODE_IMAGE -> processImage(bitmap)
        }
    }

    private fun processText(bitmap: Bitmap) {
        textRecognitionHelper.recognizeText(
            bitmap = bitmap,
            onSuccess = { tvResult.text = it },
            onFailure = { tvResult.text = "识别失败：${it.message}" }
        )
    }

    private fun processImage(bitmap: Bitmap) {
        imageLabelingHelper.classifyImage(
            bitmap = bitmap,
            onSuccess = { tvResult.text = it },
            onFailure = { tvResult.text = "识别失败：${it.message}" }
        )
    }
}
```

## 3.4 图像分类能力

- **识别类别**：1000+ 种（人、猫、狗、车、食物、家具等）
- **置信度**：0.0 - 1.0，越高越准确
- **本地模型**：首次运行时自动下载，后续离线使用

## 3.5 Claude Code 快速实现提示词

```
在这个 Android 项目中实现 ML Kit 图像分类功能：

1. 创建 mlkit/ImageLabelingHelper.kt：
   - 封装图像分类逻辑
   - 使用回调 onSuccess/onFailure 返回结果
   - 输出类别名称和置信度

2. 在 MainActivity.kt 中：
   - 添加 ImageLabelingHelper 实例
   - 添加图像分类按钮点击事件
   - 在 onActivityResult 中添加处理分支
   - 实现 processImage() 方法调用 Helper
```
