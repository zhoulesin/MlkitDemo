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
