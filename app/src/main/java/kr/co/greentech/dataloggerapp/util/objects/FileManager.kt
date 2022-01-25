package kr.co.greentech.dataloggerapp.util.objects

import android.content.Context
import android.os.Environment
import android.widget.Toast
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.TextUtil
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object FileManager {
    fun writeFile(fileName: String, msg: String, isAppend: Boolean, path: String) {
        // 파일 생성
        val saveFile = File(path) // 저장 경로

        // 폴더 생성
        if (!saveFile.exists()) { // 폴더 없을 경우
            saveFile.mkdir() // 폴더 생성
        }
        try {
            val buf = BufferedWriter(FileWriter("$saveFile/$fileName", isAppend))
            buf.append(msg) // 파일 쓰기
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun editExcelFile(file: File, readLine: List<String>, editLineMap: HashMap<Int, String>) {
        if (editLineMap.isEmpty()) return

        val path = file.absolutePath
        val editFile = File(path + "_copy")

        try {
            val buf = BufferedWriter(FileWriter(editFile.absolutePath, false))

            for (idx in readLine.indices) {
                if (editLineMap[idx] != null) {
                    buf.append(editLineMap[idx])
                } else {
                    buf.append(readLine[idx])
                }
                buf.newLine()
            }

            buf.close()

            editFile.renameTo(file)

            editLineMap.clear()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getRootFilePath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    fun getFilePath(): String {
        return getRootFilePath() + "/GreenTech"
    }

    fun getNowTime(): String? {
        val date = getNowDate()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        return sdf.format(date)
    }

    fun getFileTimeString(file: File): String {
        val time = file.lastModified()
        val date = Date(time)
        val sdf = SimpleDateFormat("yyyy.MM.dd")
        return sdf.format(date)
    }

    fun getFileSizeString(file: File): String {
        var size = file.length().toFloat()
        var count = 0
        while(size > 1024) {
            size /= 1024
            count++
        }

//        바이트 (B) - 1
//        킬로 바이트 (KB)	 - 1,024
//        메가 바이트 (MB) - 1,048,576
//        기가 바이트 (GB) - 1,073,741,824
//        테라 바이트 (TB)	- 1,099,511,627,776
//        페타 바이트 (PB)	- 1,125,899,906,842,624
//        엑사 바이트 (EB)	- 1,152,921,504,606,846,976
//        제타 바이트 (ZB)	- 1,180,591,620,717,411,303,424
//        요타 바이트 (YB)	- 1,208,925,819,614,629,174,706,176

        val unit = when(count) {
            0 -> "Byte"
            1 -> "KB"
            2 -> "MB"
            3 -> "GB"
            4 -> "TB"
            5 -> "PB"
            6 -> "EB"
            7 -> "ZB"
            else -> "YB"
        }

        var sizeString = when {
            size > 100 -> size.toInt().toString()
            else -> CalculatorUtil.editFloatDecimalPosition(size, 2).toString()
        }

        if (sizeString.contains(".")) {
            while (sizeString[sizeString.lastIndex] == '0') {
                sizeString = sizeString.substring(0, sizeString.lastIndex)
            }

            if (sizeString[sizeString.lastIndex] == '.') {
                sizeString = sizeString.substring(0, sizeString.lastIndex)
            }
        }

        return "${sizeString}${unit}"
    }

    fun getNowTime(flag: Boolean): String? {
        val date = getNowDate()
        val sdf: SimpleDateFormat = if (flag) {
            SimpleDateFormat("yyyy-MM-dd HH-mm-ss")
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        }
        return sdf.format(date)
    }

    private fun getNowDate(): Date {
        val now = System.currentTimeMillis() // 현재시간 받아오기
        return Date(now)
    }

    fun getChannelString(
        channelList: List<CopyChannel>,
        count: Int
    ): String {
        val chString = StringBuilder()
        for (i in 0 until count) {
            chString.append(" , ${channelList[i].name}")

        }
        return chString.toString()
    }

    fun duplicateAlert(context: Context, fileList: ArrayList<File>, destPath: String, isDelete: Boolean = false) {
        AlertUtil.alertOkAndCancel(
                context,
                context.getString(R.string.overwrite_exception_msg),
                context.getString(R.string.overwrite)
        ) { _, _ ->
            if (copyFileList(fileList, destPath)) {
                Toast.makeText(context, context.getString(R.string.overwrite_success_msg), Toast.LENGTH_SHORT).show()
                if (isDelete) {
                    deleteFileList(context, fileList)
                }
            } else {
                Toast.makeText(context, context.getString(R.string.overwrite_fail_msg), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteFileList(context: Context, fileList: ArrayList<File>, isToast: Boolean = true) {
        try {
            for (file in fileList) {
                removeDir(file)
            }
            if (isToast) {
                Toast.makeText(context, context.getString(R.string.delete_success_msg), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            if (isToast) {
                Toast.makeText(context, context.getString(R.string.delete_fail_msg), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isExistDir(fileList: ArrayList<File>, destPath: String): Boolean {
        for (file in fileList) {
            val destFile = File(destPath)
            val destFileList = destFile.listFiles()

            for(destFile in destFileList) {
                if(file.name == destFile.name) {
                    return true
                }
            }
        }

        return false
    }


    fun copyFileList(fileList: ArrayList<File>, destPath: String): Boolean {

        for (file in fileList) {
            val path = "$destPath"
            val saveFile = File(path) // 저장 경로

            // 폴더 생성
            if (!saveFile.exists()) { // 폴더 없을 경우
                saveFile.mkdir() // 폴더 생성
            }

            copyDir(file, destPath)
        }

        return true
    }

    private fun copyDir(file: File, destPath: String) {
        if (file.isDirectory) {
            val directory = File("${destPath}/${file.name}")
            val childFileList = file.listFiles()

            if (!directory.exists()) {
                directory.mkdir()
            }

            for (childFile in childFileList) {
                copyDir(childFile, directory.absolutePath)
            }

            directory.setLastModified(file.lastModified())
        } else {
            copyFile(file, destPath)
        }
    }

    private fun copyFile(file: File, destPath: String) {
        try {
            val newFile = File("${destPath}/${file.name}")

            if(!newFile.exists()) {
                newFile.createNewFile()
            }

            val bytes = file.readBytes()
            val stream = FileOutputStream(newFile)
            stream.write(bytes)
            stream.close()

            newFile.setLastModified(file.lastModified())
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun removeDir(file: File) {
        val childFileList = file.listFiles()

        if (childFileList != null) {
            for (childFile in childFileList) {
                if (childFile.isDirectory) {
                    removeDir(childFile)
                } else {
                    childFile.delete()
                }
            }
        }

        file.delete()
    }
}