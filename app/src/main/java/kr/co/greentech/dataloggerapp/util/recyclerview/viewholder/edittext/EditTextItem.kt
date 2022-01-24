package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.edittext

import android.text.Editable
import android.text.TextWatcher

class EditTextItem(val text: String, val title: String = "") {
    var editText = text
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