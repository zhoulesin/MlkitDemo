# Day 4：ML Kit 人脸检测（纯 Kotlin，离线，零 NDK）

## 4.1 依赖配置

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // 人脸检测
    implementation("com.google.mlkit:face-detection:16.1.5")
}
```

## 4.2 布局更新

添加人脸检测按钮：

```xml
<Button
    android:id="@+id/btn_detect_face"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:text="检测人脸"/>
```

## 4.3 核心代码实现

**MainActivity.kt - 人脸检测部分：**

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
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class MainActivity : AppCompatActivity() {

    private val requestCodeTextRecognition = 100
    private val requestCodeImageClassification = 101
    private val requestCodeFaceDetection = 102
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRecognizeText = findViewById<Button>(R.id.btn_recognize_text)
        val btnClassifyImage = findViewById<Button>(R.id.btn_classify_image)
        val btnDetectFace = findViewById<Button>(R.id.btn_detect_face)
        tvResult = findViewById(R.id.tv_result)

        btnRecognizeText.setOnClickListener {
            openGallery(requestCodeTextRecognition)
        }

        btnClassifyImage.setOnClickListener {
            openGallery(requestCodeImageClassification)
        }

        btnDetectFace.setOnClickListener {
            openGallery(requestCodeFaceDetection)
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
                    requestCodeFaceDetection -> detectFace(bitmap)
                }
            }
        }
    }

    private fun detectFace(bitmap: Bitmap) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)
        val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)

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
                tvResult.text = if (faces.isEmpty()) {
                    "未检测到人脸"
                } else {
                    sb.toString()
                }
            }
            .addOnFailureListener { e ->
                tvResult.text = "检测失败：${e.message}"
            }
    }

    private fun classifyImage(bitmap: Bitmap) {
        // 同 Day 3 实现
    }

    private fun recognizeText(bitmap: Bitmap) {
        // 同 Day 2 实现
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

## 4.5 完整依赖配置

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
}
```

## 4.6 Claude Code 快速实现提示词

将以下提示词复制给 Claude Code 即可自动实现人脸检测功能：

```
在这个 Android 项目中实现 ML Kit 人脸检测功能：

1. 在 app/build.gradle.kts 中添加依赖：
   implementation("com.google.mlkit:face-detection:16.1.7")

2. 在 activity_main.xml 中添加人脸检测按钮，id 为 btn_detect_face

3. 在 MainActivity.kt 中：
   - 添加 import：com.google.mlkit.vision.face.*
   - 添加请求码 requestCodeFaceDetection = 102
   - 在 onCreate 中绑定按钮并设置点击事件打开相册
   - 在 onActivityResult 中添加人脸检测分支
   - 实现 detectFace() 方法，使用精确模式、检测所有地标和分类，输出人脸数量、边界框、头部角度、微笑概率、眼睛状态
```
