package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.graphaxis

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter

class GraphAxisHolder(
        val context: Context,
        val view: View,
        val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {

        val list = adapter.list as ArrayList<GraphAxisItem>
        val data = list[position]


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