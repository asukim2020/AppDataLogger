package kr.co.greentech.dataloggerapp.util.objects

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


object ImageUtil {

    fun getBitmapFromFile(file: File): Bitmap? {
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    fun getCompressBitmapFromFile2(file: File): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        return compressBitmap(bitmap)
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream)
        val byteArray: ByteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun getCompressBitmapFromFile(
            file: File,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap? {
        try {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)

        var inSampleSize = 1
        val halfWidth = options.outWidth / 2
        val halfHeight = options.outHeight / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }

        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: IOException) {
            return null
        }
    }
}