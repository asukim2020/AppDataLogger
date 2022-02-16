package kr.co.greentech.dataloggerapp.util.objects

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.channel.enums.SensorType
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import java.util.ArrayList
import kotlin.math.roundToInt

object CalculatorUtil {

    var firstValueList = ArrayList<Float>()
    private var maxChannelCount = 64
    private lateinit var context: DataLogApplication

    private fun saveFirstValueData() {
        var str = ""
        for (item in firstValueList) {
            val positionString = ",${item}"
            str += positionString
        }

        PreferenceManager.setString(PreferenceKey.ZERO_POINT_LIST, str)
    }

    fun setFirstValueData(dataList: ArrayList<Float>) {
        val size = if (dataList.size > firstValueList.size) firstValueList.size else dataList.size

        for (i in 0 until size) {
            firstValueList[i] = dataList[i]
        }
    }

    fun setContext(context: DataLogApplication) {
        this.context = context
    }

    fun pxToDp(px: Float): Int {
        val dpi: Float = context.resources.displayMetrics.densityDpi.toFloat()
        val density: Float = DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return (px / (dpi / density)).toInt()
    }

    fun dpToPx(dp: Float): Int {
        val dpi: Float = context.resources.displayMetrics.densityDpi.toFloat()
        val density: Float = DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return (dp * (dpi / density)).toInt()
    }

    fun setMaxChannelCount(maxChannelCount: Int) {
        this.maxChannelCount = maxChannelCount
    }

    fun getChannelDataList(
            lastMsg: String?,
            ampGainList: List<Float>,
            supplyVList: List<Float>,
            copyChannelList: List<CopyChannel>,
            zeroAdjustList: ArrayList<Boolean>,
            zeroClickFlag: Boolean
    ): ArrayList<Float>? {
        if (lastMsg === "") return null
//        Log.d("Asu", lastMsg)

        var msg: String = lastMsg!!
        msg = msg.replace("+", " , ")
        msg = msg.replace("-", " , -")
        msg = msg.replace("*", "")
        msg = msg.replace("$", "")
        val msgList = msg.split(",").toTypedArray()
        var index = 0
        val list: ArrayList<Float> = ArrayList()
        loop@ for (idx in msgList.indices) {
            when {
                msgList[idx] == " " -> {
                    // empty
                }
                else -> {
                    if (copyChannelList.size <= index) break@loop

                    var item = getSettingData(
                            msgList[idx].toFloat(),
                            ampGainList[index],
                            supplyVList[index],
                            copyChannelList[index]
                    )

                    if (firstValueList.size < zeroAdjustList.size) {
                        val diffSize = zeroAdjustList.size - firstValueList.size

                        for (i in 0 until diffSize) {
                            firstValueList.add(0.0F)
                        }
                    }

                    if (zeroAdjustList[index]) {
                        if (firstValueList[index] == 0.0F)
                            firstValueList[index] = item

                        item -= firstValueList[index]
                    } else {
                        if (firstValueList[index] != 0.0F)
                            firstValueList[index] = 0.0F
                    }

                    list.add(item)
                    if (maxChannelCount <= list.size) break@loop
                    index++
                }
            }
        }

        for (i in 0 until 2) {
            val item = copyChannelList.getOrNull(list.size)
            if (item != null) {
                if (item.sensorType == SensorType.SUM.value || item.sensorType == SensorType.AVERAGE.value) {
                    list.add(0.0F)
                }
            }
        }

        for (idx in list.indices) {
            sumOrAvgCalculate(
                    idx,
                    list,
                    copyChannelList
            )
        }

        if (zeroClickFlag) {
            saveFirstValueData()
        }

        return list
    }

    private fun getSettingData(
            data: Float,
            ampGain: Float,
            supplyV: Float,
            copyChannel: CopyChannel,
    ): Float {

        when (SensorType.fromInt(copyChannel.sensorType)) {
            SensorType.DIGIMATIC -> {
                val value = when (copyChannel.decPoint) {
                    1 -> data / 10F
                    2 -> data / 100F
                    3 -> data / 1000F
                    else -> data
                }

                return editFloatDecimalPosition((copyChannel.adjustA * value) + copyChannel.adjustB, copyChannel.decPoint)
            }
            else -> {}
        }

        when (SensorType.fromInt(copyChannel.sensorType)) {
            SensorType.AVERAGE -> return 0.0F
            SensorType.SUM -> return 0.0F
            else -> {
            }
        }

        var item = data * 10.0f / 32767.0f

        var gageFactor = copyChannel.gf
        if (gageFactor == 0.0f) {
            gageFactor = 2.0f
        }

        var supplyVoltage = supplyV
        if (supplyVoltage.toInt() == 1) {
            supplyVoltage = 5.0f
        }

        when (ampGain.toInt()) {
            1 -> item /= 10
            2 -> item /= 100
            3 -> item /= 1000
        }

        item = when (SensorType.fromInt(copyChannel.sensorType)) {
            SensorType.GAGE1_120, SensorType.GAGE1_350 -> (item * 4.0f) / (gageFactor * supplyVoltage) * -1000000.0f
            SensorType.GAGE2, SensorType.GAGE4_STRAIN -> (item * 4.0f) / (gageFactor * supplyVoltage) * 1000000.0f
            SensorType.GAGE4_SENSOR -> item * (copyChannel.capacity / (supplyVoltage * copyChannel.ro * 0.001f))
            SensorType.LVDT_POT -> (item * (copyChannel.capacity / supplyVoltage)) / copyChannel.ro
            SensorType.VOLT -> item * (copyChannel.capacity / 10.0f)

            SensorType.PT100,
            SensorType.TC_J,
            SensorType.TC_K,
            SensorType.TC_T,
            SensorType.TC_E,
            SensorType.TC_R,
            SensorType.TC_S -> data * 0.1f

            SensorType.AVERAGE -> 0.0F
            SensorType.SUM -> 0.0F
            else -> 0.0F
        }

        return editFloatDecimalPosition((copyChannel.adjustA * item) + copyChannel.adjustB, copyChannel.decPoint)
    }


    private fun sumOrAvgCalculate(
            idx: Int,
            list: ArrayList<Float>,
            copyChannelList: List<CopyChannel>
    ) {
        val copyChannel = copyChannelList[idx]

        if (copyChannel.sensorType == SensorType.SUM.value
                || copyChannel.sensorType == SensorType.AVERAGE.value) {

            val sumAvgFlag = copyChannel.sensorType == SensorType.SUM.value

            val str = if (sumAvgFlag) {
                PreferenceManager.getString(PreferenceKey.SUM_SET)
            } else {
                PreferenceManager.getString(PreferenceKey.AVERAGE_SET)
            }

            val dataList = str.split(",")

            if (dataList.isEmpty() || str == PreferenceManager.DEFAULT_VALUE_STRING) {

                var data = 0.0F

                for (index in list.indices) {
                    if (copyChannelList[index].sensorType != SensorType.SUM.value
                            && copyChannelList[index].sensorType != SensorType.AVERAGE.value) {
                        data += list[index]
                    }
                }

                list[idx] = data

                if (copyChannel.sensorType == SensorType.AVERAGE.value) {

                    var count = 0

                    for (index in list.indices) {
                        if (copyChannelList[index].sensorType != SensorType.SUM.value
                                && copyChannelList[index].sensorType != SensorType.AVERAGE.value) {
                            count++
                        }
                    }

                    list[idx] /= count.toFloat()
                }

            } else {

                var count = 0

                for (item in dataList) {
                    if (item.toIntOrNull() != null) {
                        val position = item.toInt()
                        if (copyChannelList[position].sensorType != SensorType.SUM.value
                                && copyChannelList[position].sensorType != SensorType.AVERAGE.value) {

                            if (list.getOrNull(position) != null) {
                                list[idx] += list[position]
                                count++
                            }

                        }
                    }
                }

                if (copyChannel.sensorType == SensorType.AVERAGE.value) {
                    list[idx] /= count.toFloat()
                }

            }
            list[idx] = editFloatDecimalPosition((copyChannel.adjustA * list[idx]) + copyChannel.adjustB, copyChannel.decPoint)
        }
    }

    fun editFloatDecimalPosition(data: Float, position: Int): Float {
        var weightInt = 1

        for (i in 0 until position) {
            weightInt *= 10
        }

        val weight = weightInt.toFloat()

        return (data * weight).roundToInt() / weight
    }
}