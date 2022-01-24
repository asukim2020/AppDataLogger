package kr.co.greentech.dataloggerapp.fragment.savesetting.fragment

import android.os.Bundle
import androidx.fragment.app.ListFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.EqualIntervalSaveSettingType.*
import kr.co.greentech.dataloggerapp.fragment.savesetting.item.SaveSettingItem
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.util.listview.ListViewAdapter

class FragmentEqualIntervalSaveSetting: ListFragment() {

    companion object {
        fun newInstance(): FragmentEqualIntervalSaveSetting {
            return FragmentEqualIntervalSaveSetting()
        }
    }

    lateinit var adapter: ListViewAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = ArrayList<SaveSettingItem>()
        list.addAll(
                listOf(
                        SaveSettingItem(INTERVAL)
                )
        )

        adapter = ListViewAdapter(this, list as ArrayList<Any>)

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
        for (i in adapter.list as ArrayList<SaveSettingItem>) {
            if (i.item == INTERVAL) {
                if (i.editText.toLongOrNull() != null) {
                    item.updateInterval(i.editText.toLong())
                }
            }
        }
    }
}