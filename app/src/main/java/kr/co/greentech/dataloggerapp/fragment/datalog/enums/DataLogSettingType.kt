package kr.co.greentech.dataloggerapp.fragment.datalog.enums

import kr.co.greentech.dataloggerapp.fragment.channel.enums.*

enum class DataLogSettingType(val value: Int) {
    STRAIN_GAGE_RANGE(0),
    SENSOR_GAGE_RANGE(1);

    fun getTitle(): String {
        return when(this) {
            STRAIN_GAGE_RANGE -> "Strain Gage Range"
            SENSOR_GAGE_RANGE -> "4Gage (Sensor) Range"
        }
    }

    fun getItems(): List<String> {
        val items = ArrayList<String>()
        when (this) {
            STRAIN_GAGE_RANGE -> {
                for (item in StrainGageRangeType.values()) {
                    items.add(item.getTitle())
                }
                return items
            }

            SENSOR_GAGE_RANGE -> {
                for (item in SensorGageRangeType.values()) {
                    items.add(item.getTitle())
                }
                return items
            }

            else -> return listOf()
        }
    }
}