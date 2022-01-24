package kr.co.greentech.dataloggerapp.fragment.datalog.enums

enum class StrainGageRangeType(val value: Int) {
    UST10000(0),
    UST100000(1),
    UST1000000(2);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(): String {
        return when(this) {
            UST10000 -> "±10000 (ust)"
            UST100000 -> "±100000 (ust)"
            UST1000000 -> "±1000000 (ust)"
        }
    }
}