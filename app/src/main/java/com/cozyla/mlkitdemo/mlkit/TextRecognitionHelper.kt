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
