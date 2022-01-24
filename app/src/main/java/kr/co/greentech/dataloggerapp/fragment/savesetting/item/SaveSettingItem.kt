package kr.co.greentech.dataloggerapp.fragment.savesetting.item

import android.text.Editable
import android.text.TextWatcher
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.EqualIntervalSaveSettingType

class SaveSettingItem(val item: EqualIntervalSaveSettingType) {

    var editText = ""
    var textWatcher: TextWatcher? = null

    init {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editText = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        }
    }
}