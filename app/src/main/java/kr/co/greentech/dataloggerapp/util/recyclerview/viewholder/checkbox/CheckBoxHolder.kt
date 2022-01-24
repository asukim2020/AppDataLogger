package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.checkbox

import android.view.View
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter

class CheckBoxHolder(
        val view: View,
        val adapter: RecyclerViewAdapter,
        val copyChannelList: List<CopyChannel>
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {

        val item = adapter.list[position] as CheckBoxItem
        val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)
        val checkbox = view.findViewById<CheckBox>(R.id.checkbox)

        checkbox.isChecked = item.isOn
        checkbox.text = copyChannelList[position].name

        rootLayout.setOnClickListener {
            checkbox.isChecked = !checkbox.isChecked
            item.isOn = checkbox.isChecked
        }

        checkbox.setOnClickListener {
            item.isOn = checkbox.isChecked
        }

    }

}