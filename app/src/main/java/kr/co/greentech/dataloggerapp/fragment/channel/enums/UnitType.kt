package kr.co.greentech.dataloggerapp.fragment.channel.enums

import android.content.Context
import kr.co.greentech.dataloggerapp.R

enum class UnitType(val value: Int)  {
    UST(0),
    MM(1),
    KGF(2),
    N(3),
    KN(4),
    TON(5),
    V(6),
    G(7),
    POINT(8),
    C(9),
    DIRECT_INPUT(1000);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(context: Context): String {
        return when(this) {
            UST -> "uSt"
            MM -> "mm"
            KGF -> "kgf"
            N -> "N"
            KN -> "kN"
            TON -> "ton"
            V -> "V"
            G -> "g"
            POINT -> "'"
            C -> "℃"
            DIRECT_INPUT -> context.getString(R.string.direct_input)
        }
    }
}