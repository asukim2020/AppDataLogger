package kr.co.greentech.dataloggerapp.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import kr.co.greentech.dataloggerapp.BuildConfig
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType.*
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.objects.FileManager.copyFileList
import kr.co.greentech.dataloggerapp.util.objects.FileManager.duplicateAlert
import kr.co.greentech.dataloggerapp.util.objects.FileManager.getChannelString
import kr.co.greentech.dataloggerapp.util.objects.FileManager.getNowTime
import kr.co.greentech.dataloggerapp.util.objects.FileManager.isExistDir
import kr.co.greentech.dataloggerapp.util.objects.FileManager.writeFile
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.objects.FileManager
import java.io.*
import kotlin.collections.ArrayList


class FileUtil {

    private var fileName = "GreenTech" + getNowTime() + ".csv"
    private var fileInitFlag = true
    private var writeCount: Int = 0
    private var stepAppendFlag = false
    private val imageExtensions = arrayOf(
            "jpg",
            "png",
            "gif",
            "jpeg"
    )
    private var path = getFilePath()

    companion object {
        fun getElapsedTime(startInterval: Long, endInterval: Long, measureSpeed: Float): String {
            val measureSpeed1000 = (measureSpeed * 1000.0F).toLong()
            var sec = (((endInterval - startInterval).toFloat() / 1000000.0F) * measureSpeed1000).toInt()
            var min = sec / 60
            val hour = min / 60
            sec %= 60
            min %= 60
            return "${String.format("%02d", hour)}:${String.format("%02d", min)}:${String.format("%02d", sec)}"
        }
    }

    fun isImage(fileName: String): Boolean {
        val name = fileName.toLowerCase()

        for (extension in imageExtensions) {
            if (name.endsWith(extension)) {
                return true
            }
        }
        return false
    }

    fun setFileName(name: String, path: String = FileManager.getFilePath()) {
        fileName = "$name.csv"
        this.path = path
        writeFile(fileName, "", false, path)
        fileInitFlag = true
    }

    fun getFilePath(): String {
        return FileManager.getFilePath()
    }

    fun getRootFilePath(): String {
        return FileManager.getRootFilePath()
    }

    fun getFileList(directoryPath: String? = null): ArrayList<File> {
        val path = directoryPath ?: getFilePath()

        val directory = File(path)
        val list = directory.listFiles()

        val fileList = ArrayList<File>()
        if (list != null) {
            if (list.isNotEmpty()) {
                fileList.addAll(list)
            }
        }
        fileList.sortByDescending { it.lastModified() }
        return fileList
    }

    fun editName(context: Context, file: File, newFileName: String) {
        val path = file.absolutePath
        val lastIdx = path.lastIndexOf("/")
        val directory = path.substring(0, lastIdx)
        val newFile = File("$directory/$newFileName")
        if (file.renameTo(newFile)) {
            Toast.makeText(context, context.getString(R.string.change_success_msg), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, context.getString(R.string.change_fail_msg), Toast.LENGTH_SHORT).show()
        }
    }

    fun setStepAppend(flag: Boolean) {
        stepAppendFlag = flag
    }

    fun saveFile(msg: String) {
        writeCount = 0
        writeFile(fileName, msg, false, path)
    }

    fun Float.format(digits: Int) = "%.${digits}f".format(this)

    fun saveFile(
            list: ArrayList<Float>,
            mode: SaveType,
            interval: Long,
            channelList: List<CopyChannel>,
            stepp: String = ""
    ) {
        if (list.isEmpty()) return

        val msg: StringBuilder = java.lang.StringBuilder("")

        for (idx in list.indices) {
            if (idx < channelList.size) {
                msg.append(", ${TextUtil.floatToString(list[idx], channelList[idx].decPoint)} ")
            }
        }

        val totalTime = when (mode) {
            INTERVAL -> String.format("%.1f", (interval.toDouble() / 1000) * writeCount)
            STEP -> interval
        }
        val count = list.size
        if (fileInitFlag) {
            fileInitFlag = false
            val headerMsg = StringBuilder("")
            when(mode) {
                INTERVAL -> headerMsg.append("Datafile Ver 1.0, static ,${getNowTime(false)}\n")
                STEP -> headerMsg.append("Datafile Ver 1.0, Step ,${getNowTime(false)}\n")
            }

            headerMsg.append("Date , " + getNowTime(false) + "\n")
            headerMsg.append("Filename , " + getFilePath() + "/" + fileName + "\n")

            if (mode == INTERVAL) {
                headerMsg.append("Measure Interval , ${(interval.toDouble() / 1000)}\n")
            }

            headerMsg.append("Channel Number , ${count}\n")

            if (mode == STEP) {
                headerMsg.append("horizontal pressure force : , $stepp\n")
            }

            headerMsg.append("DateTime , Elasped_Time(sec)" + getChannelString(channelList, count) + "\n")

            writeFile(fileName, headerMsg.toString(), false, path)
        }

        if(mode == STEP && stepAppendFlag) {
            stepAppendFlag = false
            val headerMsg = StringBuilder("")
            headerMsg.append("horizontal pressure force : , $stepp\n")
            headerMsg.append("DateTime , Elasped_Time(sec)" + getChannelString(channelList, count) + "\n")
            writeFile(fileName, headerMsg.toString(), true, path)
        }

        val receivedMsg = getNowTime(true) + " , " + totalTime + msg + "\n"
        writeFile(fileName, receivedMsg, true, path)
        writeCount++
    }

    fun deleteFileList(context: Context, fileList: ArrayList<File>, isToast: Boolean = true) {
        FileManager.deleteFileList(context, fileList, isToast)
    }

    fun copyDir(context: Context, fileList: ArrayList<File>, destPath: String) {
        if(isExistDir(fileList, destPath)) {
            duplicateAlert(context, fileList, destPath)
        } else {
            if (copyFileList(fileList, destPath)) {
                Toast.makeText(context, context.getString(R.string.copy_success_msg), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.copy_fail_msg), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveDir(context: Context, fileList: ArrayList<File>, destPath: String) {
        if(isExistDir(fileList, destPath)) {
            duplicateAlert(context, fileList, destPath, true)
        } else {
            if (copyFileList(fileList, destPath)) {
                deleteFileList(context, fileList, false)
                Toast.makeText(context, context.getString(R.string.move_success_msg), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.move_fail_msg), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createDir(context: Context, path: String) {
        val file = File(path)

        var flag = false

        if (!file.exists()) {
            flag = file.mkdir()
        }

        if (!flag) {
            Toast.makeText(context, context.getString(R.string.create_dir_fail_msg), Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFileList(context: Context, fileList: ArrayList<File>) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)

        intent.type = "application/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        var directoryFlag = false
        val uriList = ArrayList<Uri>()
        for (file in fileList) {
            if(file.isDirectory) {
                directoryFlag = true
                break
            }
            val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
            uriList.add(uri)
        }

        if(directoryFlag) {
            AlertUtil.alert(context, context.getString(R.string.not_shared_folder))
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, uriList)

            val shareIntent = Intent.createChooser(intent, null)
            context.startActivity(shareIntent)
        }
    }

    fun openFile(context: Context, url: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, url)
            val intent = Intent(Intent.ACTION_VIEW)
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword")
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf")
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel")
            } else if (url.toString().contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip")
            } else if (url.toString().contains(".rar")) {
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed")
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf")
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav")
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif")
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg")
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain")
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*")
            } else {
                intent.setDataAndType(uri, "*/*")
            }
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION + Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.file_shared_fail), Toast.LENGTH_SHORT).show()
        }
    }
}