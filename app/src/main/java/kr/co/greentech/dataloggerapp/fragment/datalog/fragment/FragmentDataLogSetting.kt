package kr.co.greentech.dataloggerapp.fragment.datalog.fragment

import android.os.Bundle
import androidx.fragment.app.ListFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.DataLogSettingType
import kr.co.greentech.dataloggerapp.fragment.datalog.item.DataLogSettingItem
import kr.co.greentech.dataloggerapp.realm.RealmDataLog
import kr.co.greentech.dataloggerapp.util.listview.ListViewAdapter

class FragmentDataLogSetting: ListFragment() {

    companion object {
        fun newInstance(): FragmentDataLogSetting {
            return FragmentDataLogSetting()
        }
    }

    lateinit var item: RealmDataLog
    lateinit var adapter: ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = RealmDataLog.select()!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = ArrayList<DataLogSettingItem>()

        list.addAll(
                listOf(
                        DataLogSettingItem(DataLogSettingType.STRAIN_GAGE_RANGE),
                        DataLogSettingItem(DataLogSettingType.SENSOR_GAGE_RANGE)
                )
        )

        adapter = ListViewAdapter(this, list as ArrayList<Any>)

        listAdapter = adapter
        (activity as ActivityStart?)!!.setTitle(getString(R.string.data_log_setting))
        listView.divider = null
    }

    override fun onStop() {
        super.onStop()
        saveRealm()
    }

    private fun saveRealm() {

        var strainGageRange: Int = -1
        var sensorGageRange: Int = -1

        for (item in adapter.list as ArrayList<DataLogSettingItem>) {
            when(item.item) {
                DataLogSettingType.STRAIN_GAGE_RANGE -> strainGageRange = item.selectItemPosition
                DataLogSettingType.SENSOR_GAGE_RANGE -> sensorGageRange = item.selectItemPosition
            }
        }

        item.update(
                strainGageRange,
                sensorGageRange
        )
    }
}