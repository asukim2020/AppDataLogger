package kr.co.greentech.dataloggerapp.fragment.savesetting.fragment

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.fragment.savesetting.adapter.SaveSettingListAdapter
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.realm.RealmStepSetting

class FragmentStepSettingList: ListFragment() {

    companion object {
        fun newInstance(): FragmentStepSettingList {
            return FragmentStepSettingList()
        }
    }

    lateinit var adapter: SaveSettingListAdapter
    lateinit var item: RealmSaveSetting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = RealmSaveSetting.select()!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = ArrayList<RealmStepSetting>()

        for (item in item.stepSettingList) {
            list.add(item)
        }

        list.sortBy { it.key }

        adapter = SaveSettingListAdapter(item, list)
        listAdapter = adapter
        listView.divider = null

        (activity as ActivityStart?)!!.setTitle(getString(R.string.save_step_list))
    }


    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        when(position) {
            0 -> {

            }

            else -> {
                if(adapter.list.size > position - 1) {
                    val fragment: Fragment = FragmentStepSetting.newInstance(adapter.list[position - 1].key)
                    fragmentManager!!.beginTransaction()
                        .replace(R.id.fragment, fragment, "FragmentStepSetting")
                        .addToBackStack(null).commit()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        updateSelectedStep()
    }

    private fun updateSelectedStep() {
        if (adapter.list.size > adapter.stepType
            && adapter.stepType >= 0 ) {
            val item = RealmSaveSetting.select()!!
            item.updateSelectedStep(adapter.list[adapter.stepType].key)
        }
    }
}