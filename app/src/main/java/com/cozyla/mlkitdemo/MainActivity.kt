package com.cozyla.mlkitdemo

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cozyla.mlkitdemo.chat.ChatManager
import com.cozyla.mlkitdemo.chat.VolcanoApiHelper
import com.cozyla.mlkitdemo.mlkit.FaceDetectionHelper
import com.cozyla.mlkitdemo.mlkit.GalleryHelper
import com.cozyla.mlkitdemo.mlkit.ImageLabelingHelper
import com.cozyla.mlkitdemo.mlkit.TextRecognitionHelper
import kotlinx.coroutines.launch

/**
 * 首页 - 图片文本识别、图像分类、人脸检测和火山引擎（豆包）聊天演示
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_TEXT = 100
        private const val REQUEST_CODE_IMAGE = 101
        private const val REQUEST_CODE_FACE = 102
        private const val TAG = "MainActivity"
    }

    private lateinit var tvResult: TextView
    private lateinit var tvChat: TextView
    private lateinit var scrollChat: ScrollView
    private lateinit var volcanoApi: VolcanoApiHelper
    private lateinit var chatManager: ChatManager

    private val galleryHelper by lazy { GalleryHelper(this) }
    private val textRecognitionHelper by lazy { TextRecognitionHelper() }
    private val imageLabelingHelper by lazy { ImageLabelingHelper() }
    private val faceDetectionHelper by lazy { FaceDetectionHelper() }

    private var currentRequestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initHelpers()
        initViews()
    }

    private fun initHelpers() {
        chatManager = ChatManager(
            maxHistoryChars = 64000,
            systemPrompt = "你是豆包，是由字节跳动开发的 AI 助手。请用简洁、友好的语言回答问题。"
        )

        volcanoApi = VolcanoApiHelper(
            apiKey = "1be8d665-1cc2-4eca-a0b5-e94a1a3b84f1",
            model = "doubao-seed-2-0-lite-260215",
            baseUrl = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
        )
    }

    private fun initViews() {
        tvResult = findViewById(R.id.tv_result)
        tvChat = findViewById(R.id.tv_chat)
        scrollChat = findViewById(R.id.scroll_chat)
        val etMessage = findViewById<EditText>(R.id.et_message)

        findViewById<Button>(R.id.btn_recognize_text).setOnClickListener {
            galleryHelper.openGallery(REQUEST_CODE_TEXT)
        }

        findViewById<Button>(R.id.btn_classify_image).setOnClickListener {
            galleryHelper.openGallery(REQUEST_CODE_IMAGE)
        }

        findViewById<Button>(R.id.btn_detect_face).setOnClickListener {
            galleryHelper.openGallery(REQUEST_CODE_FACE)
        }

        findViewById<Button>(R.id.btn_send).setOnClickListener {
            val message = etMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                etMessage.text.clear()
            }
        }

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            chatManager.clear()
            updateChatDisplay()
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

    private fun sendMessage(message: String) {
        chatManager.addMessage(role = "user", content = message)
        updateChatDisplay()

        lifecycleScope.launch {
            try {
                val apiMessages = chatManager.getApiMessages()
                val aiResponse = volcanoApi.chatNonStream(apiMessages)
                chatManager.addMessage(role = "assistant", content = aiResponse)
                updateChatDisplay()

            } catch (e: Exception) {
                Log.e(TAG, "聊天请求异常", e)
                val errorMsg = "出错了：${e.javaClass.simpleName}\n${e.message}\n\n请查看 Logcat 获取详细信息"
                chatManager.addMessage(role = "assistant", content = errorMsg)
                updateChatDisplay()
            }
        }
    }

    private fun updateChatDisplay() {
        tvChat.text = chatManager.getDisplayText()
        scrollChat.post { scrollChat.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
