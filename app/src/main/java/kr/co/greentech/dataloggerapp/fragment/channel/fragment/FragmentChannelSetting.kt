package kr.co.greentech.dataloggerapp.fragment.channel.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentChannelAllSetting
import kr.co.greentech.dataloggerapp.fragment.channel.adapter.ChannelSettingAdapter
import kr.co.greentech.dataloggerapp.fragment.channel.enums.ChannelSettingType.*
import kr.co.greentech.dataloggerapp.fragment.channel.item.ChannelSettingItem
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.checkbox.CheckBoxItem
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import petrov.kristiyan.colorpicker.ColorPicker

class FragmentChannelSetting: Fragment() {

    companion object {
        fun newInstance(item: CopyChannel): FragmentChannelSetting {
            val f = FragmentChannelSetting()
            val args = Bundle()
            args.putSerializable("item", item)
            f.arguments = args
            return f
        }
    }

    lateinit var adapter: ChannelSettingAdapter

    private lateinit var item: CopyChannel
    private var channelAllList: ArrayList<CheckBoxItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val args = arguments
        item = args?.getSerializable("item") as CopyChannel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_channel_setting, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.all_setting -> {
                DialogFragmentChannelAllSetting
                        .newInstance()
                        .show(fragmentManager!!, "DialogFragmentChannelAllSetting")
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val channels = RealmChannel.select()
        for(channel in channels) {
            if(item.key == channel.key) {
                adapter = ChannelSettingAdapter(this, channel)
            }
        }
        adapter.list.addAll(
                listOf(
                        ChannelSettingItem(NAME, editFlag = true, spinnerFlag = false),
                        ChannelSettingItem(SENSOR_TYPE, false, spinnerFlag = true),
                        ChannelSettingItem(DEC_POINT, editFlag = false, spinnerFlag = true),
                        ChannelSettingItem(UNIT, false, spinnerFlag = true),
                        ChannelSettingItem(CAPACITY, true, spinnerFlag = false),
                        ChannelSettingItem(RO, true, spinnerFlag = false),
                        ChannelSettingItem(GF, true, spinnerFlag = false),
                        ChannelSettingItem(GRAPH_AXIS, editFlag = false, spinnerFlag = true),
                        ChannelSettingItem(FILTER, false, spinnerFlag = true),
                        ChannelSettingItem(ADJUST_A, true, spinnerFlag = false),
                        ChannelSettingItem(ADJUST_B, true, spinnerFlag = false),
                        ChannelSettingItem(LINE_COLOR, false, spinnerFlag = false)
                )
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        if (adapter.isUnitInput()) {
            adapter.addUnitInput()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        (activity as ActivityStart?)!!.setTitle(getString(R.string.channel_setting))
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        super.onStop()
        adapter.saveRealm()
        GlobalBus.getBus().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: MapEvent) {
        val map = event.map

        val channelAllSetting = map[DialogFragmentChannelAllSetting.toString()]
        if(channelAllSetting != null) {
            val list = map["CheckBoxItemList"] as? ArrayList<CheckBoxItem>
            if (list != null) {
                channelAllList = list
                adapter.saveRealm(channelAllList)
            }
        }
    }

    fun showColorPicker() {
        val colorPicker = ColorPicker(activity) // ColorPicker 객체 생성
        colorPicker.dialogBaseLayout.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                        R.color.background
                )
        )

        colorPicker.positiveButton.post {
            colorPicker.positiveButton.text = getString(R.string.ok)
        }

        colorPicker.negativeButton.post {
            colorPicker.negativeButton.text = getString(R.string.cancel)
        }

        val colors: ArrayList<String> = ArrayList() // Color 넣어줄 list


        colors.add("#FF5252")
        colors.add("#6200EA")
        colors.add("#536DFE")
        colors.add("#00b07b")
        colors.add("#8D6E63")

        colors.add("#FF6E40")
        colors.add("#7C4DFF")
        colors.add("#448AFF")
        colors.add("#00BFA5")
        colors.add("#607D8B")

        colors.add("#FFAB40")
        colors.add("#B388FF")
        colors.add("#40C4FF")
        colors.add("#69F0AE")
        colors.add("#424242")

        colorPicker.setColors(colors) // 만들어둔 list 적용
            .setColumns(5) // 5열로 설정
            .setRoundColorButton(true) // 원형 버튼으로 설정
            .setTitle(getString(R.string.color_choose))
            .setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    for (item in adapter.list) {
                        if (item.item == LINE_COLOR) {
                            item.color = Integer.toHexString(color)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancel() {}
            }).show() // dialog 생성
    }
}