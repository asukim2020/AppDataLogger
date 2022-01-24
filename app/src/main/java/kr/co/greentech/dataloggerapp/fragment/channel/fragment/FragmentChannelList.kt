package kr.co.greentech.dataloggerapp.fragment.channel.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentChannelAllSetting
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.realm.copy.CopyGraphScale
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerItemClickListener
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.ChannelHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.checkbox.CheckBoxItem
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FragmentChannelList: Fragment() {

    companion object {
        fun newInstance(): FragmentChannelList {
            return FragmentChannelList()
        }
    }

    lateinit var adapter: RecyclerViewAdapter
    private val channelList = ArrayList<CopyChannel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        channelList.addAll(RealmChannel.getCopyChannelList())

        adapter = RecyclerViewAdapter(this, channelList as ArrayList<Any>)
        val channelCount = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)
        changeChannelCount(channelCount)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_channel_count, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.ch_1 -> clickChangeChannelCount(1)
            R.id.ch_2 -> clickChangeChannelCount(2)
            R.id.ch_4 -> clickChangeChannelCount(4)
            R.id.ch_8 -> clickChangeChannelCount(8)
            R.id.ch_16 -> clickChangeChannelCount(16)
            R.id.ch_32 -> clickChangeChannelCount(32)
            R.id.ch_64 -> clickChangeChannelCount(64)
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

//        RecyclerViewAdapter.settingDivider(requireContext(), recyclerView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as ActivityStart?)!!.setTitle(getString(R.string.channel_list))
    }

    override fun onStart() {
        super.onStart()

        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        GlobalBus.getBus().unregister(this)
        super.onStop()

        val channelList = RealmChannel.select()

        for (copyChannel in adapter.list as ArrayList<CopyChannel>) {
            for(channel in channelList) {
                if (copyChannel.key == channel.key) {
                    if (copyChannel.isOn != channel.isOn) {
                        channel.updateIsOn(copyChannel.isOn)
                    }
                }
            }
        }
    }

    private fun clickChangeChannelCount(count: Int) {
        PreferenceManager.setInt(PreferenceKey.CHANNEL_COUNT, count)
        changeChannelCount(count)
    }

    private fun changeChannelCount(count: Int) {
        val list = ArrayList<CopyChannel>()
        list.addAll(channelList.subList(0, count))
        adapter.list = list as ArrayList<Any>
        adapter.notifyDataSetChanged()
    }

    private fun itemClick(position: Int) {
        if (position != 0) {
            val settingFragment: Fragment = FragmentChannelSetting.newInstance(adapter.list[position - 1] as CopyChannel)
            fragmentManager!!.beginTransaction().replace(R.id.fragment, settingFragment, "FragmentChannelSetting").addToBackStack(null).commit()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: MapEvent) {
        val map = event.map

        val channelHolder = map.getOrDefault(ChannelHolder.toString(), null)
        if(channelHolder != null) {
            val position = map.getOrDefault("position", null) as? Int
            if (position != null) {
                itemClick(position)
            }
        }
    }
}



