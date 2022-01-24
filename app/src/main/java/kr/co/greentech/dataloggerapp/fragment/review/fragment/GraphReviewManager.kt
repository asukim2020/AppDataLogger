package kr.co.greentech.dataloggerapp.fragment.review.fragment

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object GraphReviewManager {

    private fun getBitmap(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        view.draw(canvas)
        return returnedBitmap
    }

    fun captureLayout(layout: ViewGroup, file: File, isOverwrite: Boolean = false): Boolean {
        val index = file.name.indexOf(".")
        var fileName = file.name.substring(0, index)
        val format = Bitmap.CompressFormat.JPEG
        val quality = 100
        val rootFilePath = file.absolutePath
        val lastIdx = rootFilePath.lastIndexOf("/")
        val path = rootFilePath.substring(0, lastIdx)
        val folder = File(path)
        if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) fileName += ".jpg"

        val filePath = folder.absolutePath + "/" + fileName
        val file = File(filePath)

        if (!isOverwrite && file.exists()) return false

        try {
            val out = FileOutputStream(filePath)
            val b: Bitmap = getBitmap(layout)
            b.compress(format, quality, out)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    private const val menuSpace: Float = 40.7F

    fun updateOrientationUI(
        context: Context,
        orientation: Int,
        chartLayout: ConstraintLayout,
        menuPortrait: ConstraintLayout,
        menuLandscape: ConstraintLayout
    ) {
        val chartParam = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                chartParam.marginEnd = CalculatorUtil.dpToPx(0.0F)
                chartParam.bottomMargin = CalculatorUtil.dpToPx(menuSpace)
                chartLayout.layoutParams = chartParam
                menuPortrait.visibility = View.VISIBLE
                menuLandscape.visibility = View.INVISIBLE
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                chartParam.marginEnd = CalculatorUtil.dpToPx(menuSpace)
                chartParam.bottomMargin = CalculatorUtil.dpToPx(0.0F)
                chartLayout.layoutParams = chartParam
                menuPortrait.visibility = View.INVISIBLE
                menuLandscape.visibility = View.VISIBLE
            }
        }
    }
}