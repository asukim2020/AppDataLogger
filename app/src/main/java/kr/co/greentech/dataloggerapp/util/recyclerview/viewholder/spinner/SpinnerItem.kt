package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner

import android.view.View
import android.widget.AdapterView

class SpinnerItem(
        val title: String,
        var selectItemPosition: Int,
        val itemList: ArrayList<String>
        ) {
    var onItemSelectedListener: AdapterView.OnItemSelectedListener

    init {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectItemPosition = position
            }
        }
    }
}