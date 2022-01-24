package kr.co.greentech.dataloggerapp.fragment.channel.viewholder

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.channel.adapter.ChannelSettingAdapter
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.*
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.Companion.INPUT_TEXT
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.Companion.COLOR_PICKER
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.Companion.SPINNER
import kr.co.greentech.dataloggerapp.fragment.channel.enums.SensorType
import kr.co.greentech.dataloggerapp.fragment.channel.enums.SensorType.*
import kr.co.greentech.dataloggerapp.fragment.channel.enums.UnitType
import kr.co.greentech.dataloggerapp.fragment.channel.fragment.FragmentChannelSetting
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.util.AdapterUtil

class ChannelViewHolder(
        val fragment: Fragment,
        val view: View,
        val item: RealmChannel,
        val adapter: ChannelSettingAdapter
        ): RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val data = adapter.list[position]
        val title = view.findViewById<TextView>(R.id.name)
        title.text = data.item.getTitle(fragment.requireContext())

        when(data.item.getResource()) {
            INPUT_TEXT -> setEditText(position)
            SPINNER -> setSpinner(position)
            COLOR_PICKER -> setLineColor(position)

            else -> {
                // empty
            }
        }
    }

    private fun setEditText(position: Int) {
        val data = adapter.list[position]
        val tvEdit = view.findViewById<EditText>(R.id.tv_edit)

        val invisibleTvList = ArrayList<ChannelSettingType>()
        when (SensorType.fromInt(adapter.getSenorType())) {

            GAGE1_120,
            GAGE1_350,
            GAGE2,
            GAGE4_STRAIN -> invisibleTvList.addAll(listOf(CAPACITY, RO))

            GAGE4_SENSOR,
            LVDT_POT -> invisibleTvList.addAll(listOf(GF))

            VOLT -> invisibleTvList.addAll(listOf(RO, GF))

            PT100,
            TC_J,
            TC_K,
            TC_T,
            TC_E,
            TC_R,
            TC_S,
            DIGIMATIC,
            AVERAGE,
            SUM -> invisibleTvList.addAll(listOf(CAPACITY, RO, GF))
        }

        if (invisibleTvList.contains(data.item)) {
            tvEdit.visibility = View.INVISIBLE
        } else {
            tvEdit.visibility = View.VISIBLE
        }

        for (item in adapter.list) {
            tvEdit.removeTextChangedListener(item.textWatcher)
        }
        if (data.editText == "") {
            when (data.item) {
                NAME -> tvEdit.setText(item.name)
                UNIT_INPUT -> tvEdit.setText(item.unitInput)
                CAPACITY -> tvEdit.setText(item.capacity.toString())
                RO -> tvEdit.setText(item.ro.toString())
                GF -> tvEdit.setText(item.gf.toString())
                ADJUST_A -> tvEdit.setText(item.adjustA.toString())
                ADJUST_B -> tvEdit.setText(item.adjustB.toString())
                else -> {}
            }
            data.editText = tvEdit.text.toString()
        }

        tvEdit.setText(data.editText)
        tvEdit.addTextChangedListener(data.textWatcher)

        when (data.item) {
            NAME, UNIT_INPUT -> tvEdit.inputType = InputType.TYPE_CLASS_TEXT
            else -> tvEdit.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    private fun setSpinner(position: Int) {
        val data = adapter.list[position]
        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val spinnerAdapter: ArrayAdapter<String> = AdapterUtil.getSpinnerAdapter(
                fragment.requireContext(),
                spinner,
                data.item.getItems(fragment.requireContext())
        )
        spinner.adapter = spinnerAdapter

        if (data.selectItemPosition == -1) {

            when (data.item) {
                SENSOR_TYPE -> data.selectItemPosition = item.sensorType
                DEC_POINT -> data.selectItemPosition = item.decPoint
                UNIT -> {
                    if (item.unit == UnitType.DIRECT_INPUT.value) {
                        data.selectItemPosition = UnitType.values().size - 1
                    } else {
                        data.selectItemPosition = item.unit
                    }
                }
                GRAPH_AXIS -> data.selectItemPosition = item.graphAxis
                FILTER -> data.selectItemPosition = item.filter
                else -> {}
            }
        }

        when(data.item) {
            SENSOR_TYPE -> {
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, idx: Int, id: Long) {
                        data.selectItemPosition = idx
                        if (SensorType.isGageSensor(idx)) {
                            adapter.list.getOrNull(position + 1)?.selectItemPosition = 0
                        }
                        adapter.notifyDataSetChanged(idx)
                    }
                }
            }

            UNIT -> {
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, idx: Int, id: Long) {
                        data.selectItemPosition = idx
                        if (idx == UnitType.values().size - 1) {
                            adapter.addUnitInput()
                        } else {
                            adapter.deleteUnitInput()
                        }
                    }
                }
            }

            else -> spinner.onItemSelectedListener = data.onItemSelectedListener!!
        }

        spinner.setSelection(data.selectItemPosition)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setLineColor(position: Int) {
        val data = adapter.list[position]
        val vw = view.findViewById<View>(R.id.background_view)
        val color = item.lineColor
        if (data.color == "-1") {
            data.color = color ?: ""

            if (color != null) {
                try {
                    data.color = color
                } catch (e: NumberFormatException) {

                }
            }
        }
        if (data.color != "-1") {
            try {
                val lineColor = Color.parseColor("#${data.color}")
                vw.setBackgroundColor(lineColor)
            } catch (e: IllegalArgumentException) {

            }
        }

        vw.setOnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                v.post {
                    (fragment as FragmentChannelSetting).showColorPicker()
                }
                true
            } else {
                false
            }
        }
    }
}