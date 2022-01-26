package kr.co.greentech.dataloggerapp.fragment.channel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.*
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.Companion.INPUT_TEXT
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.Companion.COLOR_PICKER
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.Companion.SPINNER
import kr.co.greentech.dataloggerapp.fragment.channel.enums.SensorType
import kr.co.greentech.dataloggerapp.fragment.channel.enums.UnitType
import kr.co.greentech.dataloggerapp.fragment.channel.item.ChannelSettingItem
import kr.co.greentech.dataloggerapp.fragment.channel.viewholder.ChannelViewHolder
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.util.extension.addSeparator
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.checkbox.CheckBoxItem

class ChannelSettingAdapter(val fragment: Fragment, val item: RealmChannel) : RecyclerView.Adapter<ChannelViewHolder>() {

    var list: ArrayList<ChannelSettingItem> = ArrayList()
    var sensorType: Int = item.sensorType

    override fun getItemViewType(position: Int): Int {
        return list[position].item.getResource()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        lateinit var view: View
        when (viewType) {
            INPUT_TEXT -> view = inflater.inflate(R.layout.list_item_edit_text, parent, false)
            SPINNER -> view = inflater.inflate(R.layout.list_item_spinner, parent, false)
            COLOR_PICKER -> view = inflater.inflate(R.layout.list_item_channel_color, parent, false)
        }

        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
        return ChannelViewHolder(fragment, view, item, this)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(position)
    }

    fun notifyDataSetChanged(sensorType: Int) {
        if (this.sensorType != sensorType) {
            this.sensorType = sensorType
            notifyDataSetChanged()
        }
    }

    fun getSenorType(): Int {
        return sensorType
    }

    fun isUnitInput(): Boolean {
        return item.unit == UnitType.DIRECT_INPUT.value
    }

    fun addUnitInput() {
        var flag = true
        for (item in list) {
            if (item.item == UNIT_INPUT) {
                flag = false
            }
        }

        if (flag) {
            list.add(UNIT.value + 1, ChannelSettingItem(UNIT_INPUT, editFlag = true, spinnerFlag = false))
            notifyItemInserted(UNIT.value + 1)
        }
    }

    fun deleteUnitInput() {
        for (idx in list.indices) {
            if (list[idx].item == UNIT_INPUT) {
                list.removeAt(idx)
                notifyItemRemoved(idx)
                break
            }
        }
    }

    fun saveRealm(channelAllList: ArrayList<CheckBoxItem>? = null) {
        var name: String = ""
        var type: Int = -1
        var decPoint: Int = -1
        var unit: Int = -1
        var unitInput: String = ""
        var capacity: Float = 0.0F
        var ro: Float = 0.0F
        var gf: Float = 0.0F
        var graphAxis: Int = -1
        var filter: Int = -1
        var adjustA: Float = 0.0F
        var adjustB: Float = 0.0F
        var lineColor: String? = null

        for (item in list) {
            when (item.item) {
                NAME -> name = item.editText
                SENSOR_TYPE -> type = item.selectItemPosition
                DEC_POINT -> decPoint = item.selectItemPosition
                UNIT -> unit = item.selectItemPosition
                UNIT_INPUT -> unitInput = item.editText
                CAPACITY -> capacity =
                        if (item.editText.toFloatOrNull() != null) {
                            val capacity = item.editText.toFloat()
                            when(SensorType.fromInt(type)) {
                                SensorType.GAGE4_SENSOR,
                                SensorType.LVDT_POT,
                                SensorType.VOLT -> {

                                    decPoint = when {
                                        1000 <= capacity -> 0
                                        100 <= capacity && capacity < 1000 -> 1
                                        10 <= capacity && capacity < 100 -> 2
                                        else -> 3
                                    }
                                }
                            }
                            capacity
                        }
                        else 0.0F
                RO -> ro =
                        if (item.editText.toFloatOrNull() != null) item.editText.toFloat() else 0.0F
                GF -> gf =
                        if (item.editText.toFloatOrNull() != null) item.editText.toFloat() else 0.0F
                GRAPH_AXIS -> graphAxis = item.selectItemPosition
                FILTER -> filter = item.selectItemPosition
                ADJUST_A -> adjustA =
                        if (item.editText.toFloatOrNull() != null) item.editText.toFloat() else 0.0F
                ADJUST_B -> adjustB =
                        if (item.editText.toFloatOrNull() != null) item.editText.toFloat() else 0.0F
                LINE_COLOR -> lineColor = item.color
            }
        }

        if (unit == UnitType.values().size - 1) {
            unit = UnitType.DIRECT_INPUT.value
        }

        item.update(
                name,
                type,
                decPoint,
                unit,
                unitInput,
                capacity,
                ro,
                gf,
                graphAxis,
                filter,
                adjustA,
                adjustB,
                lineColor
        )

        if (channelAllList != null) {

            val channelList = RealmChannel.select()
            for (idx in channelAllList.indices) {
                if (channelAllList[idx].isOn) {
                    channelList[idx].update(
                            channelList[idx].name,
                            type,
                            decPoint,
                            unit,
                            unitInput,
                            capacity,
                            ro,
                            gf,
                            graphAxis,
                            filter,
                            adjustA,
                            adjustB,
                            channelList[idx].lineColor
                    )
                }
            }
        }
    }
}