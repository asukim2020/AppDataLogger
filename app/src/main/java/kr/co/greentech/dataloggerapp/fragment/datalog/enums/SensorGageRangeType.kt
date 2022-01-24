package kr.co.greentech.dataloggerapp.fragment.datalog.enums

enum class SensorGageRangeType(val value: Int)  {
    V0_2(0),
    V0_5(1),
    V0_20(2),
    V0_50(3),
    V0_200(4),
    AUTO(5);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(): String {
        return when(this) {
            V0_2 -> "0~2 mv/v"
            V0_5 -> "0~5 mv/v"
            V0_20 -> "0~20 mv/v"
            V0_50 -> "0~50 mv/v"
            V0_200 -> "0~200 mv/v"
            AUTO -> "Auto"
        }
    }
}