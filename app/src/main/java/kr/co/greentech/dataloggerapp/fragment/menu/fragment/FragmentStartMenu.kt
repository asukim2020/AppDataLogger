package kr.co.greentech.dataloggerapp.fragment.menu.fragment

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent

import android.view.View
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.fragment.menu.enums.StartMenu
import kr.co.greentech.dataloggerapp.fragment.menu.item.MenuItem
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.listview.ListViewAdapter

class FragmentStartMenu: ListFragment() {

    companion object {
        fun newInstance(): FragmentStartMenu {
            return FragmentStartMenu()
        }
    }

    lateinit var adapter: ListViewAdapter
    private val menuList: Array<StartMenu> = StartMenu.values()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as? ActivityStart)?.setTitle(getString(R.string.app_title))

        val list = ArrayList<MenuItem>()
        for (menu in menuList) {
            val item = getMenuItem(menu)
            list.add(item)
        }

        adapter = ListViewAdapter(this, list as ArrayList<Any>)
        listAdapter = adapter
        listView.divider = null

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.action == KeyEvent.ACTION_UP) {
                AlertUtil.alertOkAndCancelCancelable(
                    requireContext(),
                        getString(R.string.end_msg),
                        getString(R.string.end),
                ) { _, _ ->
                    activity?.finish()
                }

                true
            } else false
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.notifyDataSetChanged()
        adapter.notifyDataSetInvalidated()
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val item = StartMenu.fromInt(position)
        val fragment: Fragment = item.getFragment()
        fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment, item.getFragmentTag()).addToBackStack(null).commit()
    }

    private fun getMenuItem(menu: StartMenu): MenuItem {
        val activity = this.requireActivity()
        return MenuItem(menu.getIcon()!!, menu.getTitle(requireContext()), menu.getSubtitle(requireContext()))
    }

}