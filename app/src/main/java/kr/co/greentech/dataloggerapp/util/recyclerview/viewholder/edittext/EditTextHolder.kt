package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.edittext

import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentStepSetting
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter


class EditTextHolder(
    val fragment: FragmentStepSetting,
    val view: View,
    val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val data = adapter.list[position] as EditTextItem
        val title = view.findViewById<TextView>(R.id.name)
        val str = fragment.requireContext().getString(R.string.step_count)
        val text = String.format(str, "${position + 1}")
        title.text = text

        val tvEdit = view.findViewById<EditText>(R.id.tv_edit)
        tvEdit.hint = fragment.requireContext().getString(R.string.input_minute)
        for (item in adapter.list as ArrayList<EditTextItem>) {
            tvEdit.removeTextChangedListener(item.textWatcher)
        }

        tvEdit.setText(data.editText)

        tvEdit.addTextChangedListener(data.textWatcher)
        tvEdit.inputType = InputType.TYPE_CLASS_NUMBER
        tvEdit.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.action == KeyEvent.ACTION_UP) {
                fragment.checkData()
                true
            } else false
        }
    }
}