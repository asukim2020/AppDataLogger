package kr.co.greentech.dataloggerapp.fragment.channel.enums

import android.content.Context
import kr.co.greentech.dataloggerapp.R

enum class DecimalPointType(val value: Int) {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3);

    fun getTitle(context: Context): String {
        return when(this) {
            ZERO -> context.getString(R.string.not_decimal_point)
            ONE -> context.getString(R.string.decimal_point_1)
            TWO -> context.getString(R.string.decimal_point_2)
            THREE -> context.getString(R.string.decimal_point_3)
        }
    }
}