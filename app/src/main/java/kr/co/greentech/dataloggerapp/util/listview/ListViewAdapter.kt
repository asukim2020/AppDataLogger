package kr.co.greentech.dataloggerapp.util.listview

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.datalog.item.DataLogSettingItem
import kr.co.greentech.dataloggerapp.fragment.menu.item.MenuItem
import kr.co.greentech.dataloggerapp.fragment.savesetting.item.SaveSettingItem
import kr.co.greentech.dataloggerapp.realm.RealmDataLog
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.listview.enum.ListViewType
import kr.co.greentech.dataloggerapp.util.listview.enum.ListViewType.*
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem

class ListViewAdapter(val fragment: Fragment, var list: ArrayList<Any>): BaseAdapter() {

    companion object {
        fun settingDivider(context: Context, listView: ListView) {
            listView.divider = ColorDrawable(DataLogApplication.getColor(R.color.separator))
            val height = CalculatorUtil.dpToPx(0.7F)
            if (height < 1) {
                listView.dividerHeight = 1
            } else {
                listView.dividerHeight = CalculatorUtil.dpToPx(0.7F)
            }
        }
    }


    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    fun getItemOrNull(position: Int): Any? {
        return list.getOrNull(position)
    }

    fun add(item: Any) {
        list.add(item)
    }

    fun removeAt(position: Int) {
        list.removeAt(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        return when(ListViewType.getType(fragment)) {
            DATA_LOG -> {
                val item = RealmDataLog.select()!!
                ListViewGetViewSet.dataLogSettingGetView(position, convertView, parent, list as ArrayList<DataLogSettingItem>, item)
            }

            MENU -> {
                ListViewGetViewSet.menuGetView(position, convertView, parent, list as ArrayList<MenuItem>)
            }

            INTERVAL -> {
                val list = getItem(position)
                if (list is SaveSettingItem) {
                    val item = RealmSaveSetting.select()!!
                    ListViewGetViewSet.equalIntervalGetView(
                        convertView,
                        parent,
                        list,
                        item
                    )
                } else {
                    val item = getItem(position)
                    ListViewGetViewSet.spinnerGetView(
                        convertView,
                        parent,
                        this,
                        item as SpinnerItem
                    )
                }
            }
        }
    }
}