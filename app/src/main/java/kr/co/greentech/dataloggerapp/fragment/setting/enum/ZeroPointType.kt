package kr.co.greentech.dataloggerapp.fragment.setting.enum

import android.content.Context
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType

enum class ZeroPointType(val value: Int) {
    SOME(0),
    ALL(1);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(context: Context): String {
        return when(this) {
            SOME -> context.getString(R.string.zero_point_some)
            ALL -> context.getString(R.string.zero_point_all)
        }
    }
}