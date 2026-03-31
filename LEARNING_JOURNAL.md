# ML Kit 移动端 AI 学习记录

## Day 1：AI 移动端核心概念

### 1.1 移动端 AI 优势

| 特性 | 说明 |
|------|------|
| **离线运行** | 不需要网络请求，保护用户隐私 |
| **低延迟** | 本地处理，响应速度快 |
| **零费用** | 不需要调用云端 API |
| **高可用性** | 无网络环境也能使用 |

### 1.2 Google ML Kit 简介

ML Kit 是 Google 提供的移动端机器学习 SDK，提供：
- 文本识别（OCR）
- 图像标注
- 人脸检测
- 条码扫描
- 翻译等功能

### 1.3 项目配置基础

**最低版本要求：**
- minSdk: 24 (Android 7.0)
- targetSdk: 36 (Android 14)
- 语言: 100% Kotlin

---

## Day 2：纯 Kotlin 实现 离线 OCR 文字识别

### 2.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 中文文本识别
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
}
```

### 2.2 布局文件

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

### 2.3 核心代码实现

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

### 2.4 实现要点

1. **创建识别器**：使用 `ChineseTextRecognizerOptions` 配置中文识别
2. **构建 InputImage**：将 Bitmap 转换为 ML Kit 的 InputImage
3. **异步处理**：`process()` 方法是异步的，通过回调获取结果
4. **零 NDK/C++**：纯 Kotlin 实现，无需 JNI

---

## Day 3：纯 Kotlin 离线图像分类

### 3.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 图像分类
    implementation("com.google.mlkit:image-labeling:17.0.9")
}
```

### 3.2 布局更新

添加图像分类按钮：

```xml
<Button
    android:id="@+id/btn_classify_image"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:text="识别图像内容"/>
```

### 3.3 核心代码实现

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

### 3.4 图像分类能力

- **识别类别**：1000+ 种（人、猫、狗、车、食物、家具等）
- **置信度**：0.0 - 1.0，越高越准确
- **本地模型**：首次运行时自动下载，后续离线使用

### 3.5 完整依赖配置

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

---

## 总结

| 功能 | 依赖库 | 版本 |
|------|--------|------|
| 中文 OCR | text-recognition-chinese | 16.0.1 |
| 图像分类 | image-labeling | 17.0.9 |

**特点：**
- ✅ 纯 Kotlin 实现
- ✅ 零 NDK / 零 C++
- ✅ 离线运行
- ✅ 隐私安全
