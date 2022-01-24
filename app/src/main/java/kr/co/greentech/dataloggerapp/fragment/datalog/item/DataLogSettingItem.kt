package kr.co.greentech.dataloggerapp.fragment.datalog.item

import android.view.View
import android.widget.AdapterView
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.DataLogSettingType

class DataLogSettingItem(val item: DataLogSettingType) {
    var selectItemPosition: Int = -1
    var onItemSelectedListener: AdapterView.OnItemSelectedListener? = null

    init {

        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectItemPosition = position
            }
        }
    }
}