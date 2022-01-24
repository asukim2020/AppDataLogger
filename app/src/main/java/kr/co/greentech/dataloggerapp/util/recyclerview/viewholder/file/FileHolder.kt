package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.file

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.FileUtil
import kr.co.greentech.dataloggerapp.util.objects.FileManager
import kr.co.greentech.dataloggerapp.util.objects.ImageUtil
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter

class FileHolder(
        val context: Context,
        val view: View,
        val adapter: RecyclerViewAdapter
        ): RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val fileUtil = FileUtil()
        val title = view.findViewById<TextView>(R.id.title)
        val subTitle = view.findViewById<TextView>(R.id.subtitle)
        val timeTitle = view.findViewById<TextView>(R.id.tv_time)

        if((adapter.list[position] as FileItem?) == null) return

        val file = (adapter.list[position] as FileItem).file
        val isOn = (adapter.list[position] as FileItem).isOn
        title.text = file.name

        val iv = view.findViewById<ImageView>(R.id.iv)

        if(isOn) {
            view.setBackgroundColor(DataLogApplication.getColor(R.color.editSelect))
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }

        if (file.isDirectory) {
            val tintColor = DataLogApplication.getColor(R.color.folder)
            iv.setImageDrawable(DataLogApplication.getDrawable(R.drawable.ic_folder_white_24))
            iv.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)

            val str = context.getString(R.string.file_list_count_label)
            val text = String.format(str, "${file.listFiles().size}")

            subTitle.text = text
        } else {
            if (fileUtil.isImage(file.name)) {
                GlobalScope.async {
                    val bitmap = ImageUtil.getCompressBitmapFromFile(
                            file,
                            CalculatorUtil.dpToPx(45.0F),
                            CalculatorUtil.dpToPx(45.0F)
                    )
                    if (bitmap != null) {
                        iv.post {
                            iv.clearColorFilter()
                            iv.setImageBitmap(bitmap)
                        }
                    } else {
                        iv.post {
                            val tintColor = DataLogApplication.getColor(R.color.file)
                            iv.setImageDrawable(DataLogApplication.getDrawable(R.drawable.ic_file_white_24))
                            iv.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                        }
                    }
                }
            } else {
                val tintColor = DataLogApplication.getColor(R.color.file)
                iv.setImageDrawable(DataLogApplication.getDrawable(R.drawable.ic_file_white_24))
                iv.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            }

            subTitle.text = FileManager.getFileSizeString(file)
        }

        timeTitle.text = FileManager.getFileTimeString(file)
    }
}