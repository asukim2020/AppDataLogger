package kr.co.greentech.dataloggerapp.dialog

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.checkbox.CheckBoxItem

class DialogFragmentChannelAllSetting: DialogFragment() {

    lateinit var adapter: RecyclerViewAdapter

    companion object {
        fun newInstance(): DialogFragmentChannelAllSetting {
            return DialogFragmentChannelAllSetting()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_channel_all_setting, container, false)
        isCancelable = false

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val checkBoxLayout = view.findViewById<ConstraintLayout>(R.id.bottom_left_layout)
        val checkBox = view.findViewById<CheckBox>(R.id.checkbox)
        val cancelBtn = view.findViewById<TextView>(R.id.cancel_button)
        val confirmBtn = view.findViewById<TextView>(R.id.confirm_button)

        val list = ArrayList<CheckBoxItem>()
        val maxChannelCount = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)

        for (i in 0 until maxChannelCount) {
            list.add(CheckBoxItem(true))
        }

        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        checkBoxLayout.setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
            val list = adapter.list as ArrayList<CheckBoxItem>
            for (item in list) {
                item.isOn = checkBox.isChecked
            }
            adapter.notifyDataSetChanged()
        }

        checkBox.setOnClickListener {
            val list = adapter.list as ArrayList<CheckBoxItem>
            for (item in list) {
                item.isOn = checkBox.isChecked
            }
            adapter.notifyDataSetChanged()
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
        
        confirmBtn.setOnClickListener {

            val event = MapEvent(HashMap())
            event.map[DialogFragmentChannelAllSetting.toString()] = DialogFragmentChannelAllSetting.toString()
            val checkBoxItemList = ArrayList<CheckBoxItem>()
            checkBoxItemList.addAll(adapter.list as ArrayList<CheckBoxItem>)
            event.map["CheckBoxItemList"] = checkBoxItemList

            GlobalBus.getBus().post(event)

            dismiss()
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val maxChannelCount = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)
        val checkboxHeight = 45.0F

        val height: Float = checkboxHeight.toFloat() * maxChannelCount + 32.0F + 40.0F

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()

        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = CalculatorUtil.dpToPx(300.0F)

        val maxHeight = CalculatorUtil.dpToPx(height)

        if((size.y * 0.9) > maxHeight) {
            params?.height = maxHeight
        } else {
            params?.height = (size.y * 0.9).toInt()
        }
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}