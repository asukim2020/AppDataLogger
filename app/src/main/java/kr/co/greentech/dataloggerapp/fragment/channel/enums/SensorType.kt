package kr.co.greentech.dataloggerapp.fragment.channel.enums

import kr.co.greentech.dataloggerapp.fragment.datalog.enums.SensorGageRangeType
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.SensorGageRangeType.*
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.StrainGageRangeType
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.StrainGageRangeType.*
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel

enum class SensorType(val value: Int) {
    GAGE1_120(0),
    GAGE1_350(1),
    GAGE2(2),
    GAGE4_STRAIN(3),
    GAGE4_SENSOR(4),
    LVDT_POT(5),
    VOLT(6),
    PT100(7),
    TC_J(8),
    TC_K(9),
    TC_T(10),
    TC_E(11),
    TC_R(12),
    TC_S(13),
    DIGIMATIC(14),
    AVERAGE(15),
    SUM(16);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun isGageSensor(value: Int): Boolean {

            return when(fromInt(value)) {
                GAGE1_120, GAGE1_350, GAGE2, GAGE4_STRAIN -> true
                else -> false
            }
        }
    }

    fun getTitle(): String {
        return when(this) {
            GAGE1_120 -> "1Gage 120"
            GAGE1_350 -> "1Gage 350"
            GAGE2 -> "2Gage"
            GAGE4_STRAIN -> "4Gage (Strain)"
            GAGE4_SENSOR -> "4Gage (Sensor)"
            LVDT_POT -> "Lvdt(Pot.)"
            VOLT -> "Volt"
            PT100 -> "PT 100"
            TC_J -> "TC J"
            TC_K -> "TC K"
            TC_T -> "TC T"
            TC_E -> "TC E"
            TC_R -> "TC R"
            TC_S -> "TC S"
            DIGIMATIC -> "Digimatic"
            AVERAGE -> "Average"
            SUM -> "Sum"
        }
    }

    fun getAmpGain(
            strainGateRange: StrainGageRangeType,
            sensorGageRange: SensorGageRangeType,
            copyChannel: CopyChannel,
    ): String {
        return when(this) {
            GAGE1_120, GAGE1_350, GAGE2, GAGE4_STRAIN -> {
                when(strainGateRange) {
                    UST10000 -> "3"
                    UST100000 -> "2"
                    UST1000000 -> "1"
                }
            }
            GAGE4_SENSOR -> {
                when(sensorGageRange) {
                    V0_2 -> "3"
                    V0_5 -> "3"
                    V0_20 -> "2"
                    V0_50 -> "2"
                    V0_200 -> "1"
                    AUTO -> {
                        val ro = copyChannel.ro
                        val ampGain = when {
                            ro <= 5.1F -> "3"
                            ro <= 50.1F -> "2"
                            else -> "1"
                        }
                        ampGain
                    }
                }
            }
            LVDT_POT -> "1"

            VOLT,
            DIGIMATIC -> "0"

            PT100,
            TC_J,
            TC_K,
            TC_T,
            TC_E,
            TC_R,
            TC_S -> "2"

            AVERAGE -> "0"
            SUM -> "0"
        }
    }

    fun getAppliedVoltage(
            sensorGageRange: SensorGageRangeType,
            copyChannel: CopyChannel,
    ): String {
        return when(this) {
            GAGE1_120 -> "2"
            GAGE1_350 -> "2"
            GAGE2 -> "2"
            GAGE4_STRAIN -> "2"
            GAGE4_SENSOR -> {
                when(sensorGageRange) {
                    V0_2 -> "1"
                    V0_5 -> "2"
                    V0_20 -> "1"
                    V0_50 -> "2"
                    V0_200 -> "1"
                    AUTO -> {
                        val ro = copyChannel.ro
                        val ampGain = when {
                            ro <= 2.1F -> "1"
                            ro <= 5.1F -> "2"
                            ro <= 20.1F -> "1"
                            ro <= 50.1F -> "2"
                            else -> "1"
                        }
                        ampGain
                    }
                }
            }
            LVDT_POT -> "2"

            VOLT,
            DIGIMATIC -> "2"

            PT100,
            TC_J,
            TC_K,
            TC_T,
            TC_E,
            TC_R,
            TC_S  -> "2"

            AVERAGE -> "0"
            SUM -> "0"
        }
    }
}