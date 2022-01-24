package kr.co.greentech.dataloggerapp.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication

class AdapterUtil {

    companion object {

        fun <T> getSpinnerAdapter(context: Context, spinner: Spinner, list: List<T>): ArrayAdapter<T> {
            return object: ArrayAdapter<T>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    list
            ){
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: TextView = super.getView(
                            position,
                            convertView,
                            parent
                    ) as TextView

                    view.textAlignment = View.TEXT_ALIGNMENT_CENTER

                    return view
                }

                override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                ): View {
                    val view: TextView = super.getDropDownView(
                            position,
                            convertView,
                            parent
                    ) as TextView

                    view.setTextColor(DataLogApplication.getColor(R.color.font))

                    if (position == spinner.selectedItemPosition){
                        view.setBackgroundColor(DataLogApplication.getColor(R.color.colorControlHighlight))
                    } else {
                        view.setBackgroundColor(DataLogApplication.getColor(R.color.itemBackground))
                    }

                    view.textAlignment = View.TEXT_ALIGNMENT_CENTER

                    return view
                }
            }
        }
    }
}