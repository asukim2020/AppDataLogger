package kr.co.greentech.dataloggerapp.fragment.channel.enums

import android.content.Context
import kr.co.greentech.dataloggerapp.R

enum class ChannelSettingType(val value: Int) {
    NAME(0),
    SENSOR_TYPE(1),
    DEC_POINT(2),
    UNIT(3),
    UNIT_INPUT(4),
    CAPACITY(5),
    RO(6),
    GF(7),
    GRAPH_AXIS(8),
    FILTER(9),
    ADJUST_A(10),
    ADJUST_B(12),
    LINE_COLOR(12);

    companion object {
        const val INPUT_TEXT = 0
        const val SPINNER = 2
        const val COLOR_PICKER = 3
    }

    fun getTitle(context: Context): String {
        return when(this) {
            NAME -> context.getString(R.string.name)
            SENSOR_TYPE -> context.getString(R.string.sensor_type)
            DEC_POINT -> context.getString(R.string.decimal_point)
            UNIT -> context.getString(R.string.select_unit)
            UNIT_INPUT -> context.getString(R.string.input_unit)
            CAPACITY -> "Capacity"
            RO -> "R.O(mV/V)"
            GF -> "G.F"
            GRAPH_AXIS -> "Graph Axis"
            FILTER -> context.getString(R.string.filter)
            ADJUST_A -> "Adj. A"
            ADJUST_B -> "Adj. B"
            LINE_COLOR -> context.getString(R.string.line_color)
        }
    }

    fun getResource(): Int {
        return when(this) {
            NAME, UNIT_INPUT, CAPACITY, RO, GF, ADJUST_A, ADJUST_B -> INPUT_TEXT
            SENSOR_TYPE, DEC_POINT, UNIT, GRAPH_AXIS, FILTER -> SPINNER
            LINE_COLOR -> COLOR_PICKER
        }
    }

    fun getItems(context: Context): List<String> {
        val items = ArrayList<String>()
        when (this) {
            SENSOR_TYPE -> {
                for (item in SensorType.values()) {
                    items.add(item.getTitle())
                }
                return items
            }

            DEC_POINT -> {
                for (item in DecimalPointType.values()) {
                    items.add(item.getTitle(context))
                }
                return items
            }

            UNIT -> {
                for (item in UnitType.values()) {
                    items.add(item.getTitle(context))
                }
                return items
            }

            GRAPH_AXIS -> {
                for (item in GraphAxisType.values()) {
                    items.add(item.getTitle())
                }
                return items
            }

            FILTER -> {
                for (item in FilterType.values()) {
                    items.add(item.getTitle())
                }
                return items
            }

            else -> return listOf()
        }
    }
}