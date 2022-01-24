package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.enum

import android.content.Context
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.channel.enums.*

enum class YAxisType(val value: Int) {

    LEFT_AXIS(0),
    RIGHT_AXIS(1);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun getItems(): ArrayList<String> {
            val items = ArrayList<String>()

            for (item in GraphAxisType.values()) {
                items.add(item.getTitle())
            }

            return items
        }
    }

    fun getTitle(): String {
        return when(this) {
            LEFT_AXIS -> "Left Y Axis"
            RIGHT_AXIS -> "Right Y Axis"
        }
    }
}