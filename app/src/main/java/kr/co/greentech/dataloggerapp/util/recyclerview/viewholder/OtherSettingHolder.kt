package kr.co.greentech.dataloggerapp.util.recyclerview.viewholder

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.util.AdapterUtil
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.edittext.EditTextItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem

class OtherSettingHolder(
        val context: Context,
        val view: View,
        val adapter: RecyclerViewAdapter,
) : RecyclerView.ViewHolder(view) {
    fun bind(position: Int) {

        when {
            adapter.list[position] is SpinnerItem -> {
                val item = adapter.list[position] as SpinnerItem

                val title = view.findViewById<TextView>(R.id.name)
                title.text = item.title

                val spinner = view.findViewById<Spinner>(R.id.spinner)
                val spinnerAdapter: ArrayAdapter<String> = AdapterUtil.getSpinnerAdapter(
                        context,
                        spinner,
                        item.itemList
                )
                spinner.adapter = spinnerAdapter
                spinner.onItemSelectedListener = item.onItemSelectedListener

                spinner.setSelection(item.selectItemPosition)
            }

            adapter.list[position] is EditTextItem -> {
                val data = adapter.list[position] as EditTextItem
                val title = view.findViewById<TextView>(R.id.name)
                title.text = data.title

                val tvEdit = view.findViewById<EditText>(R.id.tv_edit)

                val list = ArrayList<EditTextItem>()
                for (item in adapter.list) {
                    if(item is EditTextItem) {
                        list.add(item)
                    }
                }

                for (item in list) {
                    tvEdit.removeTextChangedListener(item.textWatcher)
                }

                tvEdit.setText(data.editText)

                if (data.title == context.getString(R.string.graph_buffer_size)) {
                    data.textWatcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                            data.editText = s.toString()

                            val bufferSize = data.editText.toIntOrNull()
                            if (bufferSize != null) {
                                if (bufferSize > 5000) {
                                    data.editText = "5000"
                                    tvEdit.setText(data.editText)
                                } else if (bufferSize < 100) {
                                    data.editText = "100"
                                    tvEdit.setText(data.editText)
                                }
                            }
                        }

                        override fun afterTextChanged(s: Editable) {}
                    }
                }

                tvEdit.addTextChangedListener(data.textWatcher)

                if (data.title == context.getString(R.string.measure_speed)) {
                    tvEdit.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
                } else {
                    tvEdit.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }

            adapter.list[position] is String -> {
                val data = adapter.list[position] as String
                val title = view.findViewById<TextView>(R.id.tv)
                title.text = data


                val outValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                view.foreground = AppCompatResources.getDrawable(context, outValue.resourceId)
                view.isClickable = true
                view.focusable = View.FOCUSABLE
            }
        }
    }
}