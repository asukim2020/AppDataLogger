package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.graphaxis

import android.text.Editable
import android.text.TextWatcher

class GraphAxisItem(val title: String, var minEditText: String, var maxEditText: String, var isOn: Boolean) {
    var minTextWatcher: TextWatcher? = null
    var maxTextWatcher: TextWatcher? = null

    init {
        minTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                minEditText = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        }

        maxTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                maxEditText = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        }
    }
}