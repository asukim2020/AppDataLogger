package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.enum

import kr.co.greentech.dataloggerapp.fragment.channel.enums.GraphAxisType

enum class XYAxisType(val value: Int) {

    X_AXIS(0),
    Y_AXIS(1);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(): String {
        return when(this) {
            X_AXIS -> "X Axis"
            Y_AXIS -> "Y Axis"
        }
    }
}