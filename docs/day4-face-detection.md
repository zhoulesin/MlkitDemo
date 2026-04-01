# Day 4：ML Kit 人脸检测（纯 Kotlin，离线，零 NDK）

## 4.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 人脸检测
    implementation("com.google.mlkit:face-detection:16.1.7")
}
```

## 4.2 项目结构

```
app/src/main/java/com/cozyla/mlkitdemo/
└── mlkit/
    ├── GalleryHelper.kt          # 相册选择
    ├── TextRecognitionHelper.kt   # 文本识别
    ├── ImageLabelingHelper.kt     # 图像分类
    └── FaceDetectionHelper.kt     # 人脸检测
```

## 4.3 核心代码实现

**创建 mlkit/FaceDetectionHelper.kt：**

```kotlin
package com.cozyla.mlkitdemo.mlkit

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectionHelper {

    fun detectFace(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                val sb = StringBuilder()
                sb.append("检测到 ${faces.size} 张人脸\n\n")
                for ((index, face) in faces.withIndex()) {
                    sb.append("人脸 ${index + 1}：\n")
                    sb.append("  边界：${face.boundingBox}\n")

                    face.headEulerAngleY?.let { sb.append("  左右转头角度：${"%.1f".format(it)}°\n") }
                    face.headEulerAngleZ?.let { sb.append("  上下点头角度：${"%.1f".format(it)}°\n") }

                    face.smilingProbability?.let {
                        sb.append("  微笑概率：${"%.2f".format(it)}\n")
                    }
                    face.leftEyeOpenProbability?.let {
                        sb.append("  左眼睁开概率：${"%.2f".format(it)}\n")
                    }
                    face.rightEyeOpenProbability?.let {
                        sb.append("  右眼睁开概率：${"%.2f".format(it)}\n")
                    }
                    sb.append("\n")
                }
                onSuccess(if (faces.isEmpty()) "未检测到人脸" else sb.toString())
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
import com.cozyla.mlkitdemo.mlkit.FaceDetectionHelper

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_TEXT = 100
        private const val REQUEST_CODE_IMAGE = 101
        private const val REQUEST_CODE_FACE = 102
    }

    private lateinit var tvResult: TextView
    private val galleryHelper by lazy { GalleryHelper(this) }
    private val textRecognitionHelper by lazy { TextRecognitionHelper() }
    private val imageLabelingHelper by lazy { ImageLabelingHelper() }
    private val faceDetectionHelper by lazy { FaceDetectionHelper() }

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

        findViewById<Button>(R.id.btn_detect_face).setOnClickListener {
            galleryHelper.openGallery(REQUEST_CODE_FACE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        val bitmap = galleryHelper.getBitmap(data) ?: return

        when (requestCode) {
            REQUEST_CODE_TEXT -> processText(bitmap)
            REQUEST_CODE_IMAGE -> processImage(bitmap)
            REQUEST_CODE_FACE -> processFace(bitmap)
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

    private fun processFace(bitmap: Bitmap) {
        faceDetectionHelper.detectFace(
            bitmap = bitmap,
            onSuccess = { tvResult.text = it },
            onFailure = { tvResult.text = "检测失败：${it.message}" }
        )
    }
}
```

## 4.4 人脸检测能力

- **检测功能**：人脸位置、头部姿态、微笑概率、眼睛睁开状态
- **性能模式**：
  - `PERFORMANCE_MODE_FAST` - 快速模式
  - `PERFORMANCE_MODE_ACCURATE` - 精确模式
- **地标模式**：
  - `LANDMARK_MODE_NONE` - 不检测面部特征点
  - `LANDMARK_MODE_ALL` - 检测眼睛、耳朵、鼻子、嘴巴等
- **分类模式**：
  - `CLASSIFICATION_MODE_NONE` - 不分类
  - `CLASSIFICATION_MODE_ALL` - 微笑、眼睛状态分类

## 4.5 Claude Code 快速实现提示词

```
在这个 Android 项目中实现 ML Kit 人脸检测功能：

1. 创建 mlkit/FaceDetectionHelper.kt：
   - 封装人脸检测逻辑
   - 使用精确模式、检测所有地标和分类
   - 使用回调 onSuccess/onFailure 返回结果
   - 输出人脸数量、边界框、头部角度、微笑概率、眼睛状态

2. 在 MainActivity.kt 中：
   - 添加 FaceDetectionHelper 实例
   - 添加人脸检测按钮点击事件
   - 在 onActivityResult 中添加处理分支
   - 实现 processFace() 方法调用 Helper
```
