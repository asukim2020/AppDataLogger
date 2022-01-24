package kr.co.greentech.dataloggerapp.fragment.channel.item

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType


class ChannelSettingItem(val item: ChannelSettingType, editFlag: Boolean, spinnerFlag: Boolean) {
    var editText = ""
    var selectItemPosition: Int = -1
    var textWatcher: TextWatcher? = null
    var onItemSelectedListener: AdapterView.OnItemSelectedListener? = null
    var color: String = "-1"

    init {

        if (editFlag) {
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    editText = s.toString()
                }

                override fun afterTextChanged(s: Editable) {}
            }
        }

        if (spinnerFlag) {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectItemPosition = position
                }
            }
        }
    }
}