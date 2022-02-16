package kr.co.greentech.dataloggerapp.fragment.savesetting.fragment

import android.os.Bundle
import androidx.fragment.app.ListFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.EqualIntervalSaveSettingType.*
import kr.co.greentech.dataloggerapp.fragment.savesetting.item.SaveSettingItem
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.util.listview.ListViewAdapter
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem

class FragmentEqualIntervalSaveSetting: ListFragment() {

    companion object {
        fun newInstance(): FragmentEqualIntervalSaveSetting {
            return FragmentEqualIntervalSaveSetting()
        }
    }

    lateinit var adapter: ListViewAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val itemList = ArrayList<String>()
        itemList.add("10HZ")
        itemList.add("1HZ")

        val measureSpeed = PreferenceManager.getFloat(PreferenceKey.MEASURE_SPEED)
        val position = if (measureSpeed == 10.0f) 0 else 1

        val list = ArrayList<Any>()
        list.addAll(
                listOf(
                    SpinnerItem(
                        "측정 HZ",
                        position,
                        itemList
                    ),
                        SaveSettingItem(INTERVAL)
                )
        )

        adapter = ListViewAdapter(this, list)

        listAdapter = adapter
        (activity as ActivityStart?)!!.setTitle(getString(R.string.save_equal_interval_setting))
        listView.divider = null
    }

    override fun onStop() {
        super.onStop()
        saveRealm()
    }

    private fun saveRealm() {
        val item = RealmSaveSetting.select()!!
        for (i in adapter.list) {
            if (i is SaveSettingItem) {
                if (i.item == INTERVAL) {
                    if (i.editText.toLongOrNull() != null) {
                        item.updateInterval(i.editText.toLong())
                    }
                }
            } else if (i is SpinnerItem) {
                val measureSpeed = if (i.selectItemPosition == 0) 10.0f else 1.0f
                PreferenceManager.setFloat(PreferenceKey.MEASURE_SPEED, measureSpeed)
                item.updateInterval(1L)
            }
        }
    }
}