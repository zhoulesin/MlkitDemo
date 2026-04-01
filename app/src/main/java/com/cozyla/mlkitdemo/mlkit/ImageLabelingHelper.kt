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
