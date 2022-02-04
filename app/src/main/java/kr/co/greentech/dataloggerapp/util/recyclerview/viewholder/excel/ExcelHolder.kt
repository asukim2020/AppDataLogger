package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.BluetoothMeasureUIManager
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter

class ExcelHolder(
        val context: Context,
        val view: View,
        val adapter: RecyclerViewAdapter
): RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position]

        if (item is ExcelItem) {
            BluetoothMeasureUIManager.setCSVLayout(context, view as LinearLayout, item, position, copyChannelList)
        }

        if (item is String) {
            (view as TextView).text = "STEP P: $item"
        }
    }


    companion object {
        var copyChannelList: List<CopyChannel>? = null

        override fun toString(): String {
            return "ExcelHolder"
        }
    }
}