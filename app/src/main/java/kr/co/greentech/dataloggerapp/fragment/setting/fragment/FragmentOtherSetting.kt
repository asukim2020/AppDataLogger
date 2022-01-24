package kr.co.greentech.dataloggerapp.fragment.setting.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.BuildConfig
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType
import kr.co.greentech.dataloggerapp.fragment.setting.enum.ZeroPointType
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerItemClickListener
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.edittext.EditTextItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem


class FragmentOtherSetting: Fragment() {
    lateinit var adapter: RecyclerViewAdapter

    companion object {
        fun newInstance(): FragmentOtherSetting {
            return FragmentOtherSetting()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val list = ArrayList<Any>()
        val sumString = "Sum Set"
        val avgString = "Average Set"

        val modeItems = ArrayList<String>()
        for (item in SaveType.values()) {
            modeItems.add(item.getShortTitle(requireContext()))
        }

        val zeroItems = ArrayList<String>()
        for (item in ZeroPointType.values()) {
            zeroItems.add(item.getTitle(requireContext()))
        }

        val graphChannelCountItems = ArrayList<String>()
        graphChannelCountItems.addAll(arrayOf("1", "2", "4", "6", "8"))

        val graphGridTypeItems = ArrayList<String>()
        graphGridTypeItems.addAll(arrayOf(getString(R.string.line), getString(R.string.dot)))

        val graphLineTypeItems = ArrayList<String>()
        graphLineTypeItems.addAll(arrayOf(getString(R.string.linear), getString(R.string.cubic_bezier)))

        val stepGraphIntervalItems = ArrayList<String>()
        stepGraphIntervalItems.addAll(arrayOf(getString(R.string.draw_save_timing), getString(R.string.always_draw)))

        val visibleMeasureTextItems = ArrayList<String>()
        visibleMeasureTextItems.addAll(arrayOf(getString(R.string.show), getString(R.string.hide)))

        list.add(
            SpinnerItem(
                requireContext().getString(R.string.select_save_type),
                PreferenceManager.getInt(PreferenceKey.MEASURE_MODE),
                modeItems
            )
        )

        list.add(
            SpinnerItem(
                requireContext().getString(R.string.select_zero_point_type),
                PreferenceManager.getInt(PreferenceKey.ZERO_POINT),
                zeroItems
            )
        )

        val channelCountSelectPosition = when(PreferenceManager.getInt(PreferenceKey.GRAPH_CHANNEL_COUNT)) {
            1 -> 0
            2 -> 1
            4 -> 2
            6 -> 3
            8 -> 4
            else -> 2
        }

        list.add(
            SpinnerItem(
                requireContext().getString(R.string.graph_channel_count),
                    channelCountSelectPosition,
                graphChannelCountItems
            )
        )

        val graphGridType = PreferenceManager.getBoolean(PreferenceKey.GRAPH_GRID_TYPE)
        list.add(
            SpinnerItem(
                requireContext().getString(R.string.select_grid_type),
                if (graphGridType) 0 else 1,
                graphGridTypeItems
            )
        )

        val graphLineType = PreferenceManager.getInt(PreferenceKey.GRAPH_LINE_TYPE)
        list.add(
                SpinnerItem(
                    requireContext().getString(R.string.graph_line_type),
                        graphLineType,
                        graphLineTypeItems
                )
        )

        val stepGraphInterval = PreferenceManager.getInt(PreferenceKey.STEP_GRAPH_INTERVAL)
        list.add(
                SpinnerItem(
                    requireContext().getString(R.string.step_graph_interval),
                        stepGraphInterval,
                        stepGraphIntervalItems
                )
        )

        val visibleMeasureTimeText = PreferenceManager.getInt(PreferenceKey.VISIBLE_MEASURE_TIME_TEST)
        list.add(
            SpinnerItem(
                requireContext().getString(R.string.visible_measure_text),
                visibleMeasureTimeText,
                visibleMeasureTextItems
            )
        )
        
        val bufferSize = PreferenceManager.getInt(PreferenceKey.GRAPH_BUFFER_SIZE)
        list.add(
            EditTextItem(bufferSize.toString(), getString(R.string.graph_buffer_size))
        )

        val measureSpeed = PreferenceManager.getFloat(PreferenceKey.MEASURE_SPEED)
        list.add(
            EditTextItem(measureSpeed.toString(), getString(R.string.measure_speed))
        )

        list.add(sumString)
        list.add(avgString)

        val versionName = BuildConfig.VERSION_NAME
        list.add("Version: $versionName")

        adapter = RecyclerViewAdapter(this, list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        activity,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                if (adapter.list[position] is String) {
                                    val str = adapter.list[position] as String
                                    if (str == sumString) {
                                        fragmentManager!!.beginTransaction().replace(R.id.fragment, FragmentSumAvgSet.newInstance(true), "FragmentSumAvgSet").addToBackStack(null).commit()
                                    } else if (str == avgString) {
                                        fragmentManager!!.beginTransaction().replace(R.id.fragment, FragmentSumAvgSet.newInstance(false), "FragmentSumAvgSet").addToBackStack(null).commit()
                                    }
                                }

                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )
        
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as ActivityStart?)!!.setTitle(getString(R.string.setting))
    }

    override fun onStop() {
        super.onStop()
        updateData()
    }

    private fun updateData() {
        val mode = (adapter.list[0] as SpinnerItem).selectItemPosition
        PreferenceManager.setInt(PreferenceKey.MEASURE_MODE, mode)

        val zeroPoint = (adapter.list[1] as SpinnerItem).selectItemPosition
        PreferenceManager.setInt(PreferenceKey.ZERO_POINT, zeroPoint)

        val countPosition = (adapter.list[2] as SpinnerItem).selectItemPosition
        val channelCount = (adapter.list[2] as SpinnerItem).itemList[countPosition]
        PreferenceManager.setInt(PreferenceKey.GRAPH_CHANNEL_COUNT, channelCount.toInt())

        val graphGridType = (adapter.list[3] as SpinnerItem).selectItemPosition
        PreferenceManager.setBoolean(PreferenceKey.GRAPH_GRID_TYPE, graphGridType == 0)

        val graphLineType = (adapter.list[4] as SpinnerItem).selectItemPosition
        PreferenceManager.setInt(PreferenceKey.GRAPH_LINE_TYPE, graphLineType)

        val stepGraphInterval = (adapter.list[5] as SpinnerItem).selectItemPosition
        PreferenceManager.setInt(PreferenceKey.STEP_GRAPH_INTERVAL, stepGraphInterval)

        val visibleMeasureTimeText = (adapter.list[6] as SpinnerItem).selectItemPosition
        PreferenceManager.setInt(PreferenceKey.VISIBLE_MEASURE_TIME_TEST, visibleMeasureTimeText)

        val bufferSize = (adapter.list[7] as EditTextItem).editText.toInt()
        PreferenceManager.setInt(PreferenceKey.GRAPH_BUFFER_SIZE, bufferSize)

        val measureSpeed = (adapter.list[8] as EditTextItem).editText.toFloat()
        if (BuildConfig.DEBUG) {
            PreferenceManager.setFloat(PreferenceKey.MEASURE_SPEED, measureSpeed)
        } else {
            if (measureSpeed > 10.0F) {
                PreferenceManager.setFloat(PreferenceKey.MEASURE_SPEED, 10.0F)
            } else {
                PreferenceManager.setFloat(PreferenceKey.MEASURE_SPEED, measureSpeed)
            }
        }
    }
}