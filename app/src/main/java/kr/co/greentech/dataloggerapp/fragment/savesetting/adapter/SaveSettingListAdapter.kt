package kr.co.greentech.dataloggerapp.fragment.savesetting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.realm.RealmStepSetting
import kr.co.greentech.dataloggerapp.util.AdapterUtil
import kr.co.greentech.dataloggerapp.util.extension.addSeparator

class SaveSettingListAdapter(val item: RealmSaveSetting, val list: ArrayList<RealmStepSetting>): BaseAdapter() {
    var stepType: Int = -1
    
    override fun getCount(): Int {
        return list.size + 1
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val context = parent.context

        lateinit var view: View

        view = if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            when(position) {
                0 -> { inflater.inflate(R.layout.list_item_spinner, parent, false)}
                else -> { inflater.inflate(R.layout.list_item_text, parent, false) }
            }
        } else {
            convertView
        }

        if (convertView == null) {
            view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
        }

        when(position) {
            0 -> {
                val title = view.findViewById<TextView>(R.id.name)
                val spinner = view.findViewById<Spinner>(R.id.spinner)
                title.text = context.getString(R.string.select_using_step)

                val itemString = ArrayList<String>()

                for (idx in list.indices) {
                    if (item.selectedStep == list[idx].key) {
                        stepType = idx
                    }
                    val str = context.getString(R.string.step_number)
                    val text = String.format(str, "${idx + 1}")
                    itemString.add(text)
                }

                if (stepType == -1) {
                    stepType = 0
                }
                
                val spinnerAdapter: ArrayAdapter<String> = AdapterUtil.getSpinnerAdapter(
                    context,
                    spinner,
                    itemString
                )
                spinner.adapter = spinnerAdapter
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, idx: Int, id: Long) {
                        stepType = idx
                    }
                }
                spinner.setSelection(stepType)
            }

            else -> {
                val tv = view.findViewById<TextView>(R.id.tv)
                val str = context.getString(R.string.step_setting_number)
                val text = String.format(str, "$position")
                tv.text = text
            }
        }

        return view
    }

}