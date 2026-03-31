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
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

/**
 * 首页 - 图片文本识别和图像分类演示
 * 提供两个按钮分别进行中文文本识别和图像内容分类
 */
class MainActivity : AppCompatActivity() {

    /** 请求码：文本识别 */
    private val requestCodeTextRecognition = 100

    /** 请求码：图像分类 */
    private val requestCodeImageClassification = 101

    /** TAG 用于日志 */
    private val tag = this::class.java.simpleName

    /** 结果显示文本控件 */
    private lateinit var tvResult: TextView

    /** 当前识别模式 */
    private var currentRequestCode = 0

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

    /**
     * 打开相册选择图片
     * @param requestCode 请求码，用于区分识别类型
     */
    private fun openGallery(requestCode: Int) {
        currentRequestCode = requestCode
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

    /**
     * ML Kit 中文文本识别
     * @param bitmap 待识别的图片
     */
    private fun recognizeText(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
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


    /**
     * ML Kit 图像分类
     * @param bitmap 待分类的图片
     */
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
}