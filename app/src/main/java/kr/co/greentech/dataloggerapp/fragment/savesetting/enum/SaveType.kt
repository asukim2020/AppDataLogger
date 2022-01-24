package kr.co.greentech.dataloggerapp.fragment.savesetting.enum

import android.content.Context
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.channel.enums.SensorType

enum class SaveType(val value: Int)  {
    INTERVAL(0),
    STEP(1);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(context: Context): String {
        return when(this) {
            INTERVAL -> context.getString(R.string.save_equal_interval_setting)
            STEP -> context.getString(R.string.save_step_setting)
        }
    }

    fun getShortTitle(context: Context): String {
        return when(this) {
            INTERVAL -> context.getString(R.string.save_equal_interval)
            STEP -> context.getString(R.string.save_step)
        }
    }
}