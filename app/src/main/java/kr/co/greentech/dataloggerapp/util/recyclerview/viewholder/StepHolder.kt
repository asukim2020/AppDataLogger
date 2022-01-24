package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentStepSelect
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter

class StepHolder(
        val fragment: DialogFragmentStepSelect,
        val view: View,
        val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {
    fun bind(position: Int) {
        val title = view.findViewById<TextView>(R.id.title)
        val str = fragment.requireContext().getString(R.string.step_count)
        val text = String.format(str, "${position + 1}")
        title.text = text
        val contents = view.findViewById<TextView>(R.id.contents)
        contents.text = (adapter.list[position] as Long).toString()
    }
}