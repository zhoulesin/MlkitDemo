package com.cozyla.mlkitdemo.mlkit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

class GalleryHelper(private val activity: Activity) {

    fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, requestCode)
    }

    fun getBitmap(data: Intent?): Bitmap? {
        val uri: Uri? = data?.data
        return uri?.let {
            MediaStore.Images.Media.getBitmap(activity.contentResolver, it)
        }
    }
}
