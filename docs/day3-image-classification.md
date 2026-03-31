# Day 3：纯 Kotlin 离线图像分类

## 3.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 图像分类
    implementation("com.google.mlkit:image-labeling:17.0.9")
}
```

## 3.2 布局更新

添加图像分类按钮：

```xml
<Button
    android:id="@+id/btn_classify_image"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:text="识别图像内容"/>
```

## 3.3 核心代码实现

**MainActivity.kt - 图像分类部分：**

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
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    private val requestCodeTextRecognition = 100
    private val requestCodeImageClassification = 101
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRecognizeText = findViewById<Button>(R.id.btn_recognize_text)
        val btnClassifyImage = findViewById<Button>(R.id.btn_classify_image)
        tvResult = findViewById(R.id.tv_result)

        btnRecognizeText.setOnClickListener {
            openGallery(requestCodeTextRecognition)
        }

        btnClassifyImage.setOnClickListener {
            openGallery(requestCodeImageClassification)
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
                when (requestCode) {
                    requestCodeTextRecognition -> recognizeText(bitmap)
                    requestCodeImageClassification -> classifyImage(bitmap)
                }
            }
        }
    }

    private fun classifyImage(bitmap: Bitmap) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val sb = StringBuilder()
                for (label in labels) {
                    val name = label.text
                    val confidence = label.confidence
                    sb.append("类别：$name，置信度：${"%.2f".format(confidence)}\n")
                }
                tvResult.text = if (sb.isEmpty()) {
                    "未识别到内容"
                } else {
                    sb.toString()
                }
            }
            .addOnFailureListener { e ->
                tvResult.text = "识别失败：${e.message}"
            }
    }

    private fun recognizeText(bitmap: Bitmap) {
        // 同 Day 2 实现
    }
}
```

## 3.4 图像分类能力

- **识别类别**：1000+ 种（人、猫、狗、车、食物、家具等）
- **置信度**：0.0 - 1.0，越高越准确
- **本地模型**：首次运行时自动下载，后续离线使用

## 3.5 完整依赖配置

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
}
```
