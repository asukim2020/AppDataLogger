package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.util.AdapterUtil
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.graphaxis.GraphAxisItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem

class SpinnerAndGraphAxisHolder(
        val context: Context,
        val view: View,
        val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {

        if(adapter.list[position] is SpinnerItem) {
            val item = adapter.list[position] as SpinnerItem

            val title = view.findViewById<TextView>(R.id.name)
            title.text = item.title

            val spinner = view.findViewById<Spinner>(R.id.spinner)
            val spinnerAdapter: ArrayAdapter<String> = AdapterUtil.getSpinnerAdapter(
                    context,
                    spinner,
                    item.itemList
            )
            spinner.adapter = spinnerAdapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (item.selectItemPosition != position) {
                        item.selectItemPosition = position
                        adapter.changeAxisData()
                    }
                }
            }
            spinner.setSelection(item.selectItemPosition)
        } else if (adapter.list[position] is GraphAxisItem) {
            val list = ArrayList<GraphAxisItem>()

            for (item in adapter.list) {
                if(item is GraphAxisItem) {
                    list.add(item)
                }
            }

            val data = adapter.list[position] as GraphAxisItem


            val title = view.findViewById<TextView>(R.id.title)
            val tvEditMin = view.findViewById<EditText>(R.id.tv_edit_min)
            val tvEditMax = view.findViewById<EditText>(R.id.tv_edit_max)
            val switchBtn = view.findViewById<SwitchCompat>(R.id.button_switch)
            val bottomLayout = view.findViewById<LinearLayout>(R.id.bottom_layout)

            title.text = data.title

            tvEditMin.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
            tvEditMax.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL

            for(item in list) {
                tvEditMin.removeTextChangedListener(item.minTextWatcher)
                tvEditMin.removeTextChangedListener(item.maxTextWatcher)

                tvEditMax.removeTextChangedListener(item.minTextWatcher)
                tvEditMax.removeTextChangedListener(item.maxTextWatcher)
            }

            tvEditMin.setText(data.minEditText)
            tvEditMax.setText(data.maxEditText)

            tvEditMin.addTextChangedListener(data.minTextWatcher)
            tvEditMax.addTextChangedListener(data.maxTextWatcher)

            switchBtn.setOnCheckedChangeListener { _, isChecked ->
                data.isOn = isChecked

                if (!isChecked) {
                    bottomLayout.foreground = ColorDrawable(Color.TRANSPARENT)
                    tvEditMin.isEnabled = true
                    tvEditMax.isEnabled = true
                } else {
                    bottomLayout.foreground = ColorDrawable(DataLogApplication.getColor(R.color.separator))
                    tvEditMin.isEnabled = false
                    tvEditMax.isEnabled = false
                }
            }

            switchBtn.isChecked = data.isOn
            if (!data.isOn) {
                bottomLayout.foreground = ColorDrawable(Color.TRANSPARENT)
                tvEditMin.isEnabled = true
                tvEditMax.isEnabled = true
            } else {
                bottomLayout.foreground = ColorDrawable(DataLogApplication.getColor(R.color.separator))
                tvEditMin.isEnabled = false
                tvEditMax.isEnabled = false
            }
        }
    }
}