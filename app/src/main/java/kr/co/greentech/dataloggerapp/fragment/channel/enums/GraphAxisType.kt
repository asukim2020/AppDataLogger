package kr.co.greentech.dataloggerapp.fragment.channel.enums

import kr.co.greentech.dataloggerapp.realm.RealmGraphScale

enum class GraphAxisType(val value: Int) {
    STRAIN(0),
    LOAD(1),
    DISPLACEMENT(2),
    PRESSURE(3),
    ACCE(4),
    VOLT(5),
    TEMPERATURE(6);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(): String {
        return when(this) {
            STRAIN -> "Strain"
            LOAD -> "Load"
            DISPLACEMENT -> "Displacement"
            PRESSURE -> "Pressure"
            ACCE -> "Acce."
            VOLT -> "Volt"
            TEMPERATURE -> "Temperature"
        }
    }

    fun getGraphScale(): RealmGraphScale {
        val scale = RealmGraphScale()

        when(this) {
            STRAIN -> scale.update(true, -500F, 500F)
            LOAD -> scale.update(true, -100F, 100F)
            DISPLACEMENT -> scale.update(true, -50F, 50F)
            PRESSURE -> scale.update(true, -5F, 5F)
            ACCE -> scale.update(true, -20F, 20F)
            VOLT -> scale.update(true, 0F, 10F)
            TEMPERATURE -> scale.update(true, 0F, 100F)
        }

        return scale
    }
}