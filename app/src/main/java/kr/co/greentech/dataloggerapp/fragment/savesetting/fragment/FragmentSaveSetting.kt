package kr.co.greentech.dataloggerapp.fragment.savesetting.fragment

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.menu.item.MenuItem
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType
import kr.co.greentech.dataloggerapp.util.listview.ListViewAdapter

class FragmentSaveSetting: ListFragment() {

    companion object {
        fun newInstance(): FragmentSaveSetting {
            return FragmentSaveSetting()
        }
    }

    lateinit var adapter: ListViewAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = ArrayList<MenuItem>()
        list.add(MenuItem(
                DataLogApplication.getDrawable(R.drawable.ic_equal_interval_white_24)!!,
                SaveType.INTERVAL.getTitle(requireContext()),
                getString(R.string.equal_interval_time_msg)
        ))

        list.add(MenuItem(
                DataLogApplication.getDrawable(R.drawable.ic_step_white_24)!!,
                SaveType.STEP.getTitle(requireContext()),
                getString(R.string.step_time_msg)
        ))

        adapter = ListViewAdapter(this, list as ArrayList<Any>)
        listAdapter = adapter
        listView.divider = null

        (activity as ActivityStart?)!!.setTitle(getString(R.string.save_setting))
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        when (position) {
            0 -> {
                val fragment: Fragment = FragmentEqualIntervalSaveSetting.newInstance()
                fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment, "FragmentEqualIntervalSaveSetting").addToBackStack(null).commit()
            }

            1 -> {
                val fragment: Fragment = FragmentStepSettingList.newInstance()
                fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment, "FragmentStepSettingList").addToBackStack(null).commit()
            }

            else -> {

            }
        }
    }
}