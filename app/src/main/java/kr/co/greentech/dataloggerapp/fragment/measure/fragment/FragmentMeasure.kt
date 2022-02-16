package kr.co.greentech.dataloggerapp.fragment.measure.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.dialog.*
import kr.co.greentech.dataloggerapp.fragment.channel.enums.GraphAxisType
import kr.co.greentech.dataloggerapp.fragment.channel.enums.SensorType
import kr.co.greentech.dataloggerapp.fragment.channel.enums.UnitType
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.SensorGageRangeType
import kr.co.greentech.dataloggerapp.fragment.datalog.enums.StrainGageRangeType
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.FragmentBluetoothMeasure.Companion.connected
import kr.co.greentech.dataloggerapp.fragment.measure.page.PagerRecyclerAdapter
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType
import kr.co.greentech.dataloggerapp.fragment.setting.enum.ZeroPointType
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.realm.RealmDataLog
import kr.co.greentech.dataloggerapp.realm.RealmGraphScaleSet
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.realm.copy.CopyGraphScale
import kr.co.greentech.dataloggerapp.util.ChartUtil
import kr.co.greentech.dataloggerapp.util.FileUtil
import kr.co.greentech.dataloggerapp.util.TextUtil
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.AdjustCheckBox.AdjustCheckBoxItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.checkbox.CheckBoxItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel.ExcelItem
import kr.co.greentech.dataloggerapp.util.textview.VerticalTextView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import android.R.attr.mode
import android.view.ViewGroup.OnHierarchyChangeListener
import androidx.appcompat.widget.AppCompatCheckedTextView

import kr.co.greentech.dataloggerapp.fragment.savesetting.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class FragmentMeasure: Fragment() {

    companion object {
        fun newInstance(): FragmentMeasure {
            return FragmentMeasure()
        }

        const val TIME_CHART = 0
        private const val XY_CHART = 1
        private const val TEXT = 2
        private const val EXCEL = 3
    }

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    private var channelCount: Int = 0
    private var zeroPointType: Int = 0
    private var visibleChannelCount: Int = 0

    private lateinit var fileUtil: FileUtil
    private var timeChartUtil: ChartUtil? = null
    private lateinit var timeChart: LineChart
    private var xyChartUtil: ChartUtil? = null

    private var xyXAxisType = 0
    private var xyYAxisType = 1

    var initTimeInterval: Long = 0L

    private var playFlag = false
    private var graphType = 0
    private var textFlag = true // 동시성 용도
    private var initReceivedFlag = true
    private var zeroClickFlag = false

    private lateinit var copyChannelList: List<CopyChannel>
    private val selectedChannelList = ArrayList<CopyChannel>()
    private lateinit var realmDataLog: RealmDataLog

    private var ampGainList = ArrayList<Float>()
    private var supplyVList = ArrayList<Float>()
    private var zeroAdjustList = ArrayList<Boolean>()
    private var receivedData = ArrayList<Float>()
    private var receivedDataList = ArrayList<ExcelItem>()

    // 측정 코루틴
    private var mutex = Mutex()
    private var job: Job? = null
    private var timerJob: Job? = null
    private var receivedJob: Job? = null


    // 측전 전 격자 표현하기 위한 용도
    private var initMeasureFlag = true

    // 이어서 측정하기 위한 변수
    private var fileIsOn: Boolean? = null
    private var fileName: String? = null
    private var appendFlag: Boolean = false

    private var stepMeasureCount: Int = 0
    private lateinit var mode: SaveType
    private var currentPage: Int = 0
    private var textChartChannelCount = 0
    private var selectedChannelIndexList = ArrayList<Int>()
    private var maxChannelCount = 64
    private var stepp = ""
    private var measureSpeed = 1.0F
    private var visibleRightTitleFlag = false
    private val bufferSize = PreferenceManager.getInt(PreferenceKey.GRAPH_BUFFER_SIZE)
    private val stepGraphInterval = PreferenceManager.getInt(PreferenceKey.STEP_GRAPH_INTERVAL)
    private var measureCount = 0
    private var isExcelScroll = false
    private lateinit var btnDownArrow: ImageButton
    private lateinit var countTextView: TextView
    private var count = 0
    private var lastReceivedMsg = ""

    // UI
    private lateinit var xyChart: LineChart
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var topLayout: LinearLayout
    private lateinit var textLayout: FrameLayout
    private lateinit var excelLayout: ConstraintLayout
    private lateinit var excelTopLayout: LinearLayout
    private lateinit var chartLayout: ConstraintLayout
    private lateinit var tvTime: TextView
    private lateinit var tvFileName: TextView
    private lateinit var menuPortrait: ConstraintLayout
    private lateinit var menuLandscape: ConstraintLayout
    private lateinit var playPortrait: ImageButton
    private lateinit var playLandscape: ImageButton
    private lateinit var graphPortrait: ImageButton
    private lateinit var graphLandscape: ImageButton
    private lateinit var zeroPortrait: ImageButton
    private lateinit var zeroLandscape: ImageButton
    private lateinit var settingPortrait: ImageButton
    private lateinit var settingLandscape: ImageButton
    private lateinit var leftYAxisTitle: VerticalTextView
    private lateinit var rightYAxisTitle: VerticalTextView
    private lateinit var xAxisTitle: TextView
    private lateinit var bottomSeparator: View
    private lateinit var endSeparator: View

    private var verticalTimeTextLayout: LinearLayout? = null
    private var horizonTimeTextLayout: LinearLayout? = null
    private var xyTextLayout: View? = null
    private lateinit var xTextView: TextView
    private lateinit var yTextView: TextView
    private val verticalTimeTitleTextList = ArrayList<TextView>()
    private val horizonTimeTitleTextList = ArrayList<TextView>()
    private val verticalTimeTextList = ArrayList<TextView>()
    private val horizonTimeTextList = ArrayList<TextView>()

    private val verticalTimeCheckboxList = ArrayList<CheckBox>()
    private val horizonTimeCheckboxList = ArrayList<CheckBox>()
    private val verticalTimeCheckboxLayoutList = ArrayList<ConstraintLayout>()
    private val horizonTimeCheckboxLayoutList = ArrayList<ConstraintLayout>()

    private val textList = ArrayList<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        maxChannelCount = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)
        copyChannelList = RealmChannel.getCopyChannelList().subList(0, maxChannelCount)
        realmDataLog = RealmDataLog.select()!!
        zeroPointType = PreferenceManager.getInt(PreferenceKey.ZERO_POINT)
        visibleChannelCount = PreferenceManager.getInt(PreferenceKey.GRAPH_CHANNEL_COUNT)

        for (idx in copyChannelList.indices) {
            zeroAdjustList.add(false)
        }

        mode = SaveType.fromInt(PreferenceManager.getInt(PreferenceKey.MEASURE_MODE))

        fileUtil = FileUtil()

        graphType = PreferenceManager.getInt(PreferenceKey.MEASURE_GRAPH_TYPE)
        if(graphType == PreferenceManager.DEFAULT_VALUE_INT) {
            graphType = 0
        }

        val set = RealmGraphScaleSet.select()!!
        xyXAxisType = set.xyXAxisType
        xyYAxisType = set.xyYAxisType

        if (mode == SaveType.STEP)
            xyYAxisType = PreferenceManager.getInt(PreferenceKey.STEP_XY_Y_AXIS)

        CalculatorUtil.setMaxChannelCount(maxChannelCount)

        val data = PreferenceManager.getString(PreferenceKey.SELECTED_CHANNEL_TO_MEASURE)
        val dataList = data.split(",")
        for (item in dataList) {
            if (item.toFloatOrNull() != null) {
                val index = item.toFloat().toInt()
                if (index < maxChannelCount) {
                    selectedChannelIndexList.add(index)
                }
            }
        }
        selectedChannelIndexList.sort()
        setSelectedChannelList()

        measureSpeed = PreferenceManager.getFloat(PreferenceKey.MEASURE_SPEED)
        setFirstValueList()
    }

    override fun onDestroy() {
        receivedJob?.cancel()
        receivedJob = null
        measureEnd()
        super.onDestroy()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()
        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        timeChart = view.findViewById(R.id.time_chart)
        xyChart = view.findViewById(R.id.xy_chart)
        rootLayout = view.findViewById(R.id.graph_layout)
        topLayout = view.findViewById(R.id.top_layout)
        textLayout = view.findViewById(R.id.text_layout)
        excelLayout = view.findViewById(R.id.excel_layout)
        excelTopLayout = view.findViewById(R.id.excel_top_layout)
        recyclerView = view.findViewById(R.id.recyclerView)
        chartLayout = view.findViewById(R.id.time_chart_layout)
        leftYAxisTitle = view.findViewById(R.id.title_left_axis)
        rightYAxisTitle = view.findViewById(R.id.title_right_axis)
        xAxisTitle = view.findViewById(R.id.title_x_axis)
        bottomSeparator = view.findViewById(R.id.bottom_separator)
        endSeparator = view.findViewById(R.id.end_separator)
        btnDownArrow = view.findViewById(R.id.btn_down_arrow)
        btnDownArrow.visibility = View.INVISIBLE
        countTextView = view.findViewById(R.id.tv_count)

        btnDownArrow.setOnClickListener {
            isExcelScroll = false
            recyclerView.scrollToPosition(adapter.list.size - 1)
            btnDownArrow.visibility = View.INVISIBLE
        }


        timeChartUtil = ChartUtil(timeChart, context)
        xyChartUtil = ChartUtil(xyChart, context)
        timeChartUtil?.setUpLineChart()

        val flag = timeChartUtil?.setupGraphAxis()
        if (flag != null) {
            visibleRightTitleFlag = flag
        }

        setAutoRemoveChannelToMeasureList()

        adapter = RecyclerViewAdapter(this, receivedDataList as ArrayList<Any>)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isExcelScroll) {
                    if (!recyclerView.canScrollVertically(1)) {
                        isExcelScroll = false
                        btnDownArrow.visibility = View.INVISIBLE
                    }
                } else {
                    val position = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                    if (adapter.list.size > position + 1) {
                        isExcelScroll = true
                        btnDownArrow.visibility = View.VISIBLE
                    }
                }

                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                        val event = MapEvent(HashMap())
                        event.map[FragmentMeasure.toString()] = FragmentMeasure.toString()
                        event.map["addBackButtonEvent"] = "addBackButtonEvent"

                        GlobalBus.getBus().post(event)
//                        Log.d("Asu", "SCROLL_STATE_IDLE")
                    }
                }
            }
        })

        xyChartUtil?.setUpLineChart()
        setAxisTitle()

        tvTime = view.findViewById(R.id.tv_time)
        tvFileName = view.findViewById(R.id.tv_file_name)

        menuPortrait = view.findViewById(R.id.menu_portrait)
        menuLandscape = view.findViewById(R.id.menu_landscape)
        menuPortrait.visibility = View.INVISIBLE
        menuLandscape.visibility = View.INVISIBLE

        zeroPortrait = view.findViewById<ImageButton>(R.id.zero_portrait)
        zeroLandscape = view.findViewById<ImageButton>(R.id.zero_landscape)

        settingPortrait = view.findViewById<ImageButton>(R.id.setting_portrait)
        settingLandscape = view.findViewById<ImageButton>(R.id.setting_landscape)

        val zeroClick = View.OnClickListener {
            DialogFragmentAdjustZero
                .newInstance(zeroAdjustList)
                .show(fragmentManager!!, "DialogFragmentAdjustZero")
        }

        zeroPortrait.setOnClickListener(zeroClick)
        zeroLandscape.setOnClickListener(zeroClick)

        playPortrait = view.findViewById<ImageButton>(R.id.play_portrait)
        playLandscape = view.findViewById<ImageButton>(R.id.play_landscape)

        val playClick = View.OnClickListener {
            if (playFlag) {
                // 클릭 시 측정 종료
                alertEnd(false)
            } else {
                // 클릭 시 측정 시작
                    // TODO: - 블루투스가 아닌경우 예외처리 추가
                if (connected == FragmentBluetoothMeasure.Connected.False) {
                    val event = MapEvent(HashMap())
                    event.map[FragmentMeasure.toString()] = FragmentMeasure.toString()
                    event.map["notConnect"] = "notConnect"

                    GlobalBus.getBus().post(event)
                } else {
                    appendMeasure()
                }
            }
        }

        playPortrait.setOnClickListener(playClick)
        playLandscape.setOnClickListener(playClick)

        graphPortrait = view.findViewById<ImageButton>(R.id.change_portrait)
        graphLandscape = view.findViewById<ImageButton>(R.id.change_landscape)

        val graphClick = View.OnClickListener {
            chartChange()
        }

        val count = PreferenceManager.getInt(PreferenceKey.MEASURE_TEXT_CHART_LAYOUT)
        if (count == PreferenceManager.DEFAULT_VALUE_INT) {
            setTextLayout(4)
        } else {
            setTextLayout(count)
        }

        val settingClick = View.OnClickListener {
            when(graphType) {
                TIME_CHART -> alertTimeChartSetting(context)
                XY_CHART -> {
                    if (xyChartUtil == null) return@OnClickListener

                    if (mode == SaveType.INTERVAL) {
                        DialogFragmentChangeAxisAndScale
                                .newInstance(false)
                                .show(fragmentManager!!, "DialogFragmentChangeGraphYAxis")
                    } else {
                        DialogFragmentStepXYSetting
                                .newInstance()
                                .show(fragmentManager!!, "DialogFragmentStepXYSetting")
                    }
                }
                TEXT -> alertTextChartSetting(context)
                EXCEL -> { }
            }
        }

        settingPortrait.setOnClickListener(settingClick)
        settingLandscape.setOnClickListener(settingClick)

        graphPortrait.setOnClickListener(graphClick)
        graphLandscape.setOnClickListener(graphClick)

        val visibleMeasureTimeText = PreferenceManager.getInt(PreferenceKey.VISIBLE_MEASURE_TIME_TEST)
        if (visibleMeasureTimeText == 0) {
            tvTime.visibility = View.VISIBLE
        } else {
            tvTime.visibility = View.INVISIBLE
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        updateOrientationUI(resources.configuration.orientation, channelCount)
        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        GlobalBus.getBus().unregister(this)
        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateOrientationUI(newConfig.orientation)
    }

    private fun setSelectedChannelList() {
        selectedChannelList.clear()
        for (i in selectedChannelIndexList) {
            selectedChannelList.add(copyChannelList[i])
        }
    }

    private fun updateOrientationUI(
            orientation: Int,
            visibleChannelCount: Int = PreferenceManager.getInt(PreferenceKey.GRAPH_CHANNEL_COUNT)
    ) {
        if(xyTextLayout == null) {
            val xyTextLayout = BluetoothMeasureUIManager.getXYTextLayout(requireContext())
            xyTextLayout.id = View.generateViewId()
            rootLayout.addView(xyTextLayout)

            xyTextLayout.layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    0
            )

            this.xyTextLayout = xyTextLayout

            xTextView = xyTextLayout.findViewById(R.id.x_subtitle)
            yTextView = xyTextLayout.findViewById(R.id.y_subtitle)
        }

        val layout = BluetoothMeasureUIManager.updateGraphOrientationUI(
                orientation,
                visibleChannelCount,
            requireContext(),
                copyChannelList,
                timeChartUtil,
                verticalTimeTextList,
                horizonTimeTextList,
                verticalTimeTitleTextList,
                horizonTimeTitleTextList,
                verticalTimeCheckboxList,
                horizonTimeCheckboxList,
                verticalTimeCheckboxLayoutList,
                horizonTimeCheckboxLayoutList,
                selectedChannelIndexList,
                rootLayout,
                chartLayout,
                textLayout,
                excelLayout,
                verticalTimeTextLayout,
                horizonTimeTextLayout,
                menuPortrait,
                menuLandscape,
                bottomSeparator,
                endSeparator,
                xyTextLayout!!
        )

        if(layout != null) {
            when (orientation) {
                Configuration.ORIENTATION_PORTRAIT -> verticalTimeTextLayout = layout
                Configuration.ORIENTATION_LANDSCAPE -> horizonTimeTextLayout = layout
            }
        }

        if (graphType != TIME_CHART) {
            verticalTimeTextLayout?.visibility = View.INVISIBLE
            horizonTimeTextLayout?.visibility = View.INVISIBLE
        } else {
            verticalTimeTextLayout?.visibility = View.VISIBLE
            horizonTimeTextLayout?.visibility = View.VISIBLE
        }

        if (job == null) {
            timeChartUtil?.setVisible(0, false)
        }
    }

    private fun setAxisTitle(leftIndex: Int = -1, rightIndex: Int = -1) {
        if (leftIndex == -1 && rightIndex == -1) {
            val set = RealmGraphScaleSet.select()!!
            leftYAxisTitle.text = GraphAxisType.fromInt(set.timeLeftYAxisType).getTitle()
            rightYAxisTitle.text = GraphAxisType.fromInt(set.timeRightYAxisType).getTitle()
        } else {
            leftYAxisTitle.text = GraphAxisType.fromInt(leftIndex).getTitle()
            rightYAxisTitle.text = GraphAxisType.fromInt(rightIndex).getTitle()
        }
    }

    fun alertEnd(flag: Boolean) {
        if (!playFlag) {
            clearFragmentStack()
            return
        }

        AlertUtil.alertOkAndCancelCancelable(
            requireContext(),
                getString(R.string.measure_end_msg),
                getString(R.string.end)
        ) { dialog, _ ->
            // 측정 종료
            dialog.dismiss()
            measureEnd()
            stepMeasureCount++
            if(flag) {
                clearFragmentStack()
            }
        }
    }

    private fun clearFragmentStack() {
        val event = MapEvent(HashMap())
        event.map[FragmentMeasure.toString()] = FragmentMeasure.toString()
        event.map["clearFragmentStack"] = "clearFragmentStack"

        GlobalBus.getBus().post(event)
    }

    fun measureEnd() {
        timeChartUtil?.refresh()
        xyChartUtil?.refresh()

        playPortrait.setImageResource(R.drawable.ic_play_arrow_white_24)
        playLandscape.setImageResource(R.drawable.ic_play_arrow_white_24)
        topLayout.setBackgroundColor(DataLogApplication.getColor(R.color.separator))
        playFlag = false
        job?.cancel()
        job = null
        timerJob?.cancel()
        timerJob = null
    }

    private fun appendMeasure() {
        if(fileIsOn != null && fileName != null) {
            AlertUtil.alertOkAndCancel(
                requireContext(),
                    getString(R.string.measure_append_msg),
                    getString(R.string.ok),
                    getString(R.string.close),
                    { _, _ ->
                        appendFlag = false
                        DialogFragmentFileSave.newInstance().show(
                                fragmentManager!!,
                                "DialogFragmentFileSave"
                        )
                    }
            ) { _, _ ->
                appendMeasureExecute()
            }
        } else {
            appendFlag = false
            when(mode) {
                SaveType.INTERVAL -> DialogFragmentFileSave.newInstance().show(
                        fragmentManager!!,
                        "DialogFragmentFileSave"
                )
                SaveType.STEP -> stepSelectAlert(requireContext(), false)
            }
        }
    }

    private fun appendMeasureExecute() {
        appendFlag = true
        fileUtil.setStepAppend(true)

        if (mode == SaveType.STEP) {
            initTimeInterval = System.currentTimeMillis()
            tvTime.text = "00:00:00"
        }

        when(mode) {
            SaveType.INTERVAL -> measureStart(fileIsOn!!, true)
            SaveType.STEP -> stepSelectAlert(requireContext(), true)
        }
    }

    private fun measureStart(fileIsOn: Boolean, timeAppendFlag: Boolean = false) {
        playPortrait.setImageResource(R.drawable.ic_pause_white_24)
        playLandscape.setImageResource(R.drawable.ic_pause_white_24)
        playFlag = true

        if (job != null) return
        if (timerJob != null) return
        val saveSetting = RealmSaveSetting.select() ?: return
        var interval = saveSetting.interval * 1000L
        val selectedStep = saveSetting.selectedStep
        val realmList = saveSetting.stepSettingList
        val list = ArrayList<Long>()

        for(item in realmList) {
            if (item.key == selectedStep) {
                for (step in item.stepList) {
                    list.add(step)
                }
            }
        }

        if (!(mode == SaveType.INTERVAL && appendFlag)) {
            receivedDataList.clear()
            measureCount = 0
        }

        job = GlobalScope.launch {
            if (!appendFlag) {
                stepMeasureCount = 0
                initMeasureFlag = false

                timeChart.post {
                    timeChartUtil?.clearChart()
                }

                xyChart.post {
                    xyChartUtil?.clearChart()
                    xyChartUtil?.addInitPoint(graphType == XY_CHART)
                }
            }

            when(mode) {
                SaveType.INTERVAL -> {
                    while (true) {
                        measureData(fileIsOn, mode, (interval / measureSpeed).toLong())
                        delay(getDelay(interval))
                    }
                }

                SaveType.STEP -> {
                    if (list.isEmpty()) {
                        GlobalScope.async(Dispatchers.Main) {
                            measureEnd()
                            AlertUtil.alert(
                                requireContext(),
                                    getString(R.string.measure_step_not_found_msg)
                            )
                        }

                        return@launch
                    }


                    if (!list.contains(0L)) {
                        list.add(0, 0L)
                    }

                    if (interval > 60L * 1000L) {
                        interval = 60L * 1000L
                    }

                    when (stepGraphInterval) {


                        0 -> {
                            val interval = 60L * 1000L

                            for (idx in list.indices) {
                                if (idx <= 0) {
                                    val delayInterval = list[idx] * interval
                                    delay(getDelay(delayInterval))
                                } else {
                                    val delayInterval = (list[idx] - list[idx - 1]) * interval
                                    delay(getDelay(delayInterval))
                                }

                                var totalInterval = list[idx]

                                totalInterval *= interval
                                measureData(fileIsOn, mode, totalInterval, list)
                            }
                        }


                        1 -> {
                            var elapsedTime = 0L
                            var index = 0
                            while (index < list.size) {
                                val start = System.currentTimeMillis()

                                var isOn = false
                                if (list[index] * 60L * 1000L <= elapsedTime) {
                                    isOn = fileIsOn
                                    index++
                                }

                                measureData(isOn, mode, elapsedTime, list)

                                val diff = System.currentTimeMillis() - start
                                delay(getDelay(interval) - diff)
                                elapsedTime += interval
                            }


                        }


                    }


                    GlobalScope.async(Dispatchers.Main) {
                        measureEnd()
                        AlertUtil.alertOkAndCancel(
                            requireContext(),
                            getString(R.string.measure_step_end_msg),
                            getString(R.string.append_measure),
                            getString(R.string.end),
                            { _, _ ->
                                stepMeasureCount++
                            },
                            { _, _ ->
                                stepMeasureCount++
                                appendMeasureExecute()
                            }
                        )
                    }


                }
            }
        }

        timerJob = GlobalScope.launch {
            if (!timeAppendFlag) {
                initTimeInterval = System.currentTimeMillis()
            }
            topLayout.post {
                topLayout.setBackgroundColor(DataLogApplication.getColor(R.color.edit))
            }

            while (true) {
                val startTime = System.currentTimeMillis()
                val elapsedTime = FileUtil.getElapsedTime(initTimeInterval, startTime, 1f)
                Log.d("Asu", "elapsedTime: " + elapsedTime + ", diff: " + (startTime - initTimeInterval) + ", measure speed: " + measureSpeed)
                tvTime.post {
                    tvTime.text = elapsedTime
                }
                delay(getDelay(1000L))
            }
        }
    }

    private fun measureData(
            fileIsOn: Boolean,
            mode: SaveType,
            interval: Long,
            stepList: ArrayList<Long>? = null
    ) {
        GlobalScope.async(Dispatchers.Main) {
            if(selectedChannelIndexList.size != selectedChannelList.size) return@async

//            val start = System.currentTimeMillis()

            lateinit var list: ArrayList<Float>
            val selectedList = ArrayList<Float>()
            mutex.withLock {
                list = ArrayList(receivedData)

                if (fileIsOn) {
                    while (receivedDataList.size > bufferSize) {
                        receivedDataList.removeFirst()
                    }
                    val floatList = ArrayList<Float>()
                    for (idx in list.indices) {
                        floatList.add(list[idx])
                    }
                    receivedDataList.add(ExcelItem(measureCount++, tvTime.text.toString(), floatList, -1))
                    if (graphType == EXCEL) {

                        if (!isExcelScroll) {
                            recyclerView.scrollToPosition(adapter.list.size - 1)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }


                for (i in selectedChannelIndexList) {
                    if (i < list.size) {
                        selectedList.add(list[i])

                        if (selectedList.size >= visibleChannelCount)
                            break
                    } else {
                        break
                    }
                }

                if (measureSpeed <= 1) {
                    receivedData.clear()
                }
            }

            if (list.isNotEmpty()) {
                when(mode) {
                    SaveType.INTERVAL -> {
                        if (fileIsOn) {
                            fileUtil.saveFile(list, mode, interval, copyChannelList)
                        }
                        timeChartUtil?.addIntervalEntry(
                                interval,
                                selectedList,
                                selectedChannelList,
                                graphType == TIME_CHART
                        )

                        xyChartUtil?.addIntervalXYEntry(list, xyXAxisType, xyYAxisType, graphType == XY_CHART)
                    }
                    SaveType.STEP -> {
                        if (fileIsOn) {
                            fileUtil.saveFile(list, mode, interval / 1000L, copyChannelList, stepp)
                        }
                        timeChartUtil?.addStepEntry(
                                interval.toFloat() / 1000.0F,
                                selectedList,


                                selectedChannelList,
                                graphType == TIME_CHART,
                                stepMeasureCount,
                                stepList
                        )

                        var max = list[0]

                        if (list.getOrNull(xyYAxisType) != null) {
                            max = list[xyYAxisType]
                        }

                        xyChartUtil?.addStepXYEntry(stepp.toFloat(), max, graphType == XY_CHART)
                    }
                }

//                val diff = System.currentTimeMillis() - start
//                Log.d("Asu", "measureData diff: $diff")
            }
        }
    }

    private fun stepSelectAlert(context: Context, startFlag: Boolean) {
        DialogFragmentStepSelect.newInstance(startFlag).show(
                fragmentManager!!,
                "DialogFragmentStepSelect"
        )
    }

    private fun chartChange(initFlag: Boolean = false) {
        if(!initFlag) {
            graphType++
            PreferenceManager.setInt(PreferenceKey.MEASURE_GRAPH_TYPE, graphType)
        } else {
            PreferenceManager.setInt(PreferenceKey.MEASURE_GRAPH_TYPE, graphType)
        }

        if(EXCEL < graphType || graphType < TIME_CHART) {
            graphType = 0
        }

        if (maxChannelCount == 1
                && graphType == XY_CHART) {
            graphType++
        }

        when (graphType) {
            TIME_CHART -> {
                timeChart.visibility = View.VISIBLE
                horizonTimeTextLayout?.visibility = View.VISIBLE
                verticalTimeTextLayout?.visibility = View.VISIBLE
                xyTextLayout?.visibility = View.INVISIBLE
                xyChart.visibility = View.INVISIBLE
                textLayout.visibility = View.INVISIBLE
                leftYAxisTitle.visibility = View.VISIBLE
                excelLayout.visibility = View.INVISIBLE
                visibleRightTitle(visibleRightTitleFlag)
                xAxisTitle.visibility = View.VISIBLE

                xAxisTitle.text = "Time (sec)"
                setAxisTitle()

                if (maxChannelCount == 1) {
                    graphPortrait.setImageResource(R.drawable.ic_text_white_24)
                    graphLandscape.setImageResource(R.drawable.ic_text_white_24)
                } else {
                    graphPortrait.setImageResource(R.drawable.ic_escalator_white_24)
                    graphLandscape.setImageResource(R.drawable.ic_escalator_white_24)
                }

                timeChartUtil?.refresh()
            }

            XY_CHART -> {
                timeChart.visibility = View.INVISIBLE
                horizonTimeTextLayout?.visibility = View.INVISIBLE
                verticalTimeTextLayout?.visibility = View.INVISIBLE
                xyTextLayout?.visibility = View.VISIBLE
                xyChart.visibility = View.VISIBLE
                textLayout.visibility = View.INVISIBLE
                leftYAxisTitle.visibility = View.VISIBLE
                rightYAxisTitle.visibility = View.INVISIBLE
                xAxisTitle.visibility = View.VISIBLE
                excelLayout.visibility = View.INVISIBLE

                if (mode == SaveType.INTERVAL) {
                    val xChannel = copyChannelList[xyXAxisType]
                    xAxisTitle.text = xChannel.name + " (${GraphAxisType.fromInt(xChannel.graphAxis).getTitle()})"
                } else {
                    xAxisTitle.text = "Step p"
                }

                val yChannel = copyChannelList[xyYAxisType]
                leftYAxisTitle.text = yChannel.name + " (${GraphAxisType.fromInt(yChannel.graphAxis).getTitle()})"

                graphPortrait.setImageResource(R.drawable.ic_text_white_24)
                graphLandscape.setImageResource(R.drawable.ic_text_white_24)

                xyChartUtil?.refresh()
            }

            TEXT -> {
                timeChart.visibility = View.INVISIBLE
                horizonTimeTextLayout?.visibility = View.INVISIBLE
                verticalTimeTextLayout?.visibility = View.INVISIBLE
                xyTextLayout?.visibility = View.INVISIBLE
                xyChart.visibility = View.INVISIBLE
                textLayout.visibility = View.VISIBLE
                leftYAxisTitle.visibility = View.INVISIBLE
                rightYAxisTitle.visibility = View.INVISIBLE
                xAxisTitle.visibility = View.INVISIBLE
                excelLayout.visibility = View.INVISIBLE

                graphPortrait.setImageResource(R.drawable.ic_baseline_view_module_24)
                graphLandscape.setImageResource(R.drawable.ic_baseline_view_module_24)
            }

            EXCEL -> {
                timeChart.visibility = View.INVISIBLE
                horizonTimeTextLayout?.visibility = View.INVISIBLE
                verticalTimeTextLayout?.visibility = View.INVISIBLE
                xyTextLayout?.visibility = View.INVISIBLE
                xyChart.visibility = View.INVISIBLE
                textLayout.visibility = View.INVISIBLE
                leftYAxisTitle.visibility = View.INVISIBLE
                rightYAxisTitle.visibility = View.INVISIBLE
                xAxisTitle.visibility = View.INVISIBLE
                excelLayout.visibility = View.VISIBLE

                graphPortrait.setImageResource(R.drawable.ic_graph_white_24)
                graphLandscape.setImageResource(R.drawable.ic_graph_white_24)

                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun visibleRightTitle(flag: Boolean) {
        if (flag) {
            rightYAxisTitle.visibility = View.INVISIBLE
        } else {
            rightYAxisTitle.visibility = View.VISIBLE
        }
    }

    private fun setTextLayout(channelCount: Int) {
        textFlag = false
        textChartChannelCount = channelCount
        textList.clear()
        PreferenceManager.setInt(PreferenceKey.MEASURE_TEXT_CHART_LAYOUT, channelCount)

        this.textLayout.removeAllViews()

        val pager = ViewPager2(requireContext())
        textLayout.addView(pager)

        pager.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        )

        pager.adapter = PagerRecyclerAdapter(maxChannelCount, channelCount, textList, copyChannelList)
        pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
            }
        })

        textFlag = true
    }

    private fun alertTimeChartSetting(
            context: Context
    ) {
        val titles = if (job != null) {
            arrayOf(
                    context.getString(R.string.setting_x_axis),
                    context.getString(R.string.setting_y_axis),
            )
        } else {
            arrayOf(
                    context.getString(R.string.setting_x_axis),
                    context.getString(R.string.setting_y_axis),
                    context.getString(R.string.select_channel_to_measure)
            )
        }

        AlertDialog
                .Builder(context)
                .setTitle(context.getString(R.string.select))
                .setItems(titles) { _, which ->
                    when(which) {
                        0 -> {
                            if (timeChartUtil != null) {
                                DialogFragmentChangeGraphXAxis
                                        .newInstance()
                                        .show(fragmentManager!!, "DialogFragmentAdjustGraphRange")
                            }
                        }
                        1 -> {
                            if (timeChartUtil != null) {
                                DialogFragmentChangeAxisAndScale
                                        .newInstance(true, playFlag)
                                        .show(fragmentManager!!, "DialogFragmentChangeGraphYAxis")
                            }
                        }
                        2 -> {
                            alertSelectChannelToMeasure(context, channelCount)
                        }
                        else -> {}
                    }
                }
                .setNeutralButton(context.getString(R.string.cancel), null)
                .show()
    }

    private fun alertSelectChannelToMeasure(
            context: Context,
            channelCount: Int
    ) {
        val leftYAxis = timeChartUtil?.leftYAxis ?: return
        val rightYAxis = timeChartUtil?.rightYAxis ?: return

        val list = copyChannelList.subList(0, channelCount).map { it.name }.toTypedArray()

        val checkList = ArrayList<Boolean>()
        for(i in 0 until channelCount) {
            var flag = false
            if (selectedChannelIndexList.contains(i)) {
                flag = true
            }
            checkList.add(flag)
        }

        val alert = AlertDialog
                .Builder(context)
                .setTitle(getString(R.string.select_channel_to_measure))
                .setMultiChoiceItems(
                        list,
                        checkList.toBooleanArray()
                ) { _: DialogInterface?, which: Int, isChecked: Boolean ->
                    checkList[which] = isChecked
                }
                .setPositiveButton(getString(R.string.ok)) { _, _ -> }
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

        alert.setOnShowListener {
            val button: Button = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val list = ArrayList<Int>()
                for (idx in checkList.indices) {
                    if (checkList[idx]) {
                        list.add(idx)
                    }
                }

                if (visibleChannelCount < list.size) {
                    val str = context.getString(R.string.select_channel_to_measure_over_count_msg)
                    val text = String.format(str, "$visibleChannelCount")
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                } else {
                    setSelectChannelToMeasureList(list)
                    alert.dismiss()
                }
            }
        }

        alert.listView.setOnHierarchyChangeListener(
            object : OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View, child: View) {
                    try {
                        val tv = (child as? AppCompatCheckedTextView) ?: return
                        val copyChannel = copyChannelList.filter { it.name == tv.text }.first()
                        val axis = copyChannel.graphAxis
                        child.isEnabled = (axis == leftYAxis || axis == rightYAxis)
                        if (!(axis == leftYAxis || axis == rightYAxis)) {
                            child.setOnClickListener(null)
                        }
                    } catch(e: Exception) {
                        Log.d("Asu", e.localizedMessage)
                    }
                }

                override fun onChildViewRemoved(view: View, view1: View) {}
            }
        )

        alert.show()
        Toast.makeText(context, context.getString(R.string.measure_axis_exception), Toast.LENGTH_LONG).show()
    }



    private fun setAutoSelectChannelToMeasureList() {
        val leftYAxis = timeChartUtil?.leftYAxis ?: return
        val rightYAxis = timeChartUtil?.rightYAxis ?: return

        val list = ArrayList<Int>()
        for (idx in copyChannelList.indices) {
            val axis = copyChannelList[idx].graphAxis
            if (axis == leftYAxis || axis == rightYAxis)
                list.add(idx)
        }

        setSelectChannelToMeasureList(list)
    }

    private fun setAutoRemoveChannelToMeasureList() {
        val leftYAxis = timeChartUtil?.leftYAxis ?: return
        val rightYAxis = timeChartUtil?.rightYAxis ?: return

        val removeList = ArrayList<Int>()

        selectedChannelIndexList
        for (idx in selectedChannelIndexList) {
            val axis = copyChannelList[idx].graphAxis
            if (!(axis == leftYAxis || axis == rightYAxis))
                removeList.add(idx)
        }

        for (item in removeList) {
            if (selectedChannelIndexList.contains(item))
                selectedChannelIndexList.remove(item)
        }

        setSelectChannelToMeasureList(selectedChannelIndexList)
    }

    private fun setSelectChannelToMeasureList(list: ArrayList<Int>) {
        selectedChannelIndexList = list
        setSelectedChannelList()

        var selectChannelString = ""
        for (item in list) {
            selectChannelString += ",$item"
        }

        PreferenceManager.setString(PreferenceKey.SELECTED_CHANNEL_TO_MEASURE, selectChannelString)

        var isOnCount = 0

        for (i in 0 until visibleChannelCount) {
            verticalTimeTitleTextList.getOrNull(i)?.text = "-"
            horizonTimeTitleTextList.getOrNull(i)?.text = "-"
            verticalTimeTextList.getOrNull(i)?.text = "-"
            horizonTimeTextList.getOrNull(i)?.text = "-"

            verticalTimeCheckboxLayoutList.getOrNull(i)?.setOnClickListener(null)
            horizonTimeCheckboxLayoutList.getOrNull(i)?.setOnClickListener(null)
            verticalTimeCheckboxList.getOrNull(i)?.setOnClickListener(null)
            horizonTimeCheckboxList.getOrNull(i)?.setOnClickListener(null)
        }

        for (i in list.indices) {
            val index = list[i]
            val name = "${copyChannelList[index].name}:"

            verticalTimeTitleTextList.getOrNull(i)?.text = name
            horizonTimeTitleTextList.getOrNull(i)?.text = name

            verticalTimeTextList.getOrNull(i)?.text = "-"
            horizonTimeTextList.getOrNull(i)?.text = "-"

            if (copyChannelList[index].isOn && index < visibleChannelCount) {
                val idx = isOnCount

                if (i < verticalTimeCheckboxLayoutList.size) {
                    verticalTimeCheckboxLayoutList[i].setOnClickListener {
                        val checkbox = verticalTimeCheckboxList[i]
                        checkbox.isChecked = !checkbox.isChecked
                        timeChartUtil?.setVisible(idx, checkbox.isChecked)
                    }
                }

                if (i < verticalTimeCheckboxList.size) {
                    verticalTimeCheckboxList[i].setOnClickListener {
                        timeChartUtil?.setVisible(idx, verticalTimeCheckboxList[i].isChecked)
                    }
                }

                if (i < horizonTimeCheckboxLayoutList.size) {
                    horizonTimeCheckboxLayoutList[i].setOnClickListener {
                        val checkbox = horizonTimeCheckboxList[i]
                        checkbox.isChecked = !checkbox.isChecked
                        timeChartUtil?.setVisible(idx, checkbox.isChecked)
                    }
                }

                if (i < horizonTimeCheckboxList.size) {
                    horizonTimeCheckboxList[i].setOnClickListener {
                        timeChartUtil?.setVisible(idx, horizonTimeCheckboxList[i].isChecked)
                    }
                }

                isOnCount++
            }
        }
    }

    fun isTablet(context: Context): Boolean {
        val xlarge = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK === 4
        val large = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK === Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge or large
    }

    private fun alertTextChartSetting(
            context: Context
    ) {
        val titles = if (isTablet(context)) {
            arrayOf(
                context.getString(R.string.channel_1),
                context.getString(R.string.channel_2),
                context.getString(R.string.channel_4),
                context.getString(R.string.channel_8),
                context.getString(R.string.channel_16)
            )

        } else {
            arrayOf(
                context.getString(R.string.channel_1),
                context.getString(R.string.channel_2),
                context.getString(R.string.channel_4),
                context.getString(R.string.channel_8),
            )
        }

        AlertDialog
            .Builder(context)
            .setTitle(context.getString(R.string.select))
            .setItems(titles) { dialog, which ->
                when (which) {
                    0 -> setTextLayout(1)
                    1 -> setTextLayout(2)
                    2 -> setTextLayout(4)
                    3 -> setTextLayout(8)
                    4 -> setTextLayout(16)

                    else -> {
                    }

                }
            }
            .setNeutralButton(context.getString(R.string.cancel), null)
            .show()
    }

    private fun setStartData(isOn: Boolean, fileName: String, path: String = fileUtil.getFilePath()) {
        if (fileName.isNotEmpty() && isOn) {
            fileUtil.setFileName(fileName, path)
            tvFileName.post {
                tvFileName.text = "${fileName}.csv"
            }
        } else {
            tvFileName.text = ""
        }
        this.fileIsOn = isOn
        this.fileName = fileName
        measureStart(isOn)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: MapEvent) {
        val map = event.map

        val fileSaveFragment = map.getOrDefault(DialogFragmentFileSave.toString(), null)
        if(fileSaveFragment != null) {
            val isOn: Boolean? = map.getOrDefault("isOn", null) as? Boolean
            val fileName: String? = map.getOrDefault("fileName", null) as? String
            val path: String? = map.getOrDefault("path", null) as? String

            if (isOn != null
                    && fileName != null) {
                if (path != null) {
                    setStartData(isOn, fileName, path)
                } else {
                    setStartData(isOn, fileName)
                }
            }
            return
        }

        val stepSelectFragment = map.getOrDefault(DialogFragmentStepSelect.toString(), null)
        if(stepSelectFragment != null) {
            stepp = map.getOrDefault("STEP P", null) as String
            val startFlag = map.getOrDefault("startFlag", null) as Boolean
            if (startFlag) {
                measureStart(this.fileIsOn!!)
            }
            return
        }

        val adjustGraphRangeFragment = map.getOrDefault(DialogFragmentChangeGraphXAxis.toString(), null)
        if(adjustGraphRangeFragment != null) {
            val xAxis = map.getOrDefault("xAxis", null) as? CopyGraphScale
            if (xAxis != null) {
                timeChartUtil?.setGraphXScale(xAxis)
            }
            return
        }

        val changeGraphYAxisFragment = map.getOrDefault(DialogFragmentChangeAxisAndScale.toString(), null)
        if(changeGraphYAxisFragment != null) {

            val firstAxisType = map.getOrDefault("firstAxisType", null) as Int
            val secondAxisType = map.getOrDefault("secondAxisType", null) as Int

            val firstAxisScale = map.getOrDefault("firstAxisScale", null) as CopyGraphScale
            val secondAxisScale = map.getOrDefault("secondAxisScale", null) as CopyGraphScale

            val isTimeChart = map.getOrDefault("isTimeChart", null) as Boolean

            timeChartUtil?.changeGraphYAxis(
                    firstAxisType,
                    secondAxisType,
                    copyChannelList,
                    selectedChannelIndexList
            )

            if (isTimeChart) {
                visibleRightTitleFlag = firstAxisType == secondAxisType
                visibleRightTitle(visibleRightTitleFlag)
                timeChartUtil?.setGraphYScale(firstAxisScale, secondAxisScale, firstAxisType == secondAxisType)
                setAxisTitle(firstAxisType, secondAxisType)
                setAutoSelectChannelToMeasureList()
            } else {
                xyChartUtil?.setGraphXScale(firstAxisScale)
                xyChartUtil?.setGraphLeftYScale(secondAxisScale)
                xyChartUtil?.clearChart()

                val xName = copyChannelList.getOrNull(firstAxisType)
                if (xName != null) {
                    xAxisTitle.text = xName.name + " (${GraphAxisType.fromInt(xName.graphAxis).getTitle()})"
                    xyXAxisType = firstAxisType
                }

                val yName = copyChannelList.getOrNull(secondAxisType)
                if (yName != null) {
                    leftYAxisTitle.text = yName.name + " (${GraphAxisType.fromInt(yName.graphAxis).getTitle()})"
                    xyYAxisType = secondAxisType
                }
            }

            return
        }

        val stepXYSetting = map.getOrDefault(DialogFragmentStepXYSetting.toString(), null)
        if(stepXYSetting != null) {

            val firstAxisType = map.getOrDefault("firstAxisType", null) as Int

            val firstAxisScale = map.getOrDefault("firstAxisScale", null) as CopyGraphScale
            val secondAxisScale = map.getOrDefault("secondAxisScale", null) as CopyGraphScale

            xyChartUtil?.setGraphXScale(firstAxisScale)
            xyChartUtil?.setGraphLeftYScale(secondAxisScale)
            xyChartUtil?.clearChart()

            var xName = copyChannelList.getOrNull(firstAxisType)
            if (xName != null) {
                xyYAxisType = firstAxisType
                if (xyYAxisType >= channelCount) {
                    xyYAxisType = 0
                    xName = copyChannelList[xyYAxisType]
                }

                PreferenceManager.setInt(PreferenceKey.STEP_XY_Y_AXIS, xyYAxisType)

                xAxisTitle.text = "Step p"
                leftYAxisTitle.text = xName.name + " (${GraphAxisType.fromInt(xName.graphAxis).getTitle()})"
            }

            return
        }

        val channelAllSetting = map.getOrDefault(DialogFragmentAdjustZero.toString(), null)
        if(channelAllSetting != null) {
            val list = map.getOrDefault("CheckBoxItemList", null) as? ArrayList<AdjustCheckBoxItem>
            if (list != null) {
                for (item in list) {
                    for (idx in list.indices) {
                        if (list[idx].isOn) {
                            zeroAdjustList[idx] = list[idx].isOn
                            CalculatorUtil.firstValueList[idx] = 0.0f
                        }
                    }
                }
            } else {
                for (idx in zeroAdjustList.indices) {
                    zeroAdjustList[idx] = false
                }
            }
            zeroClickFlag = true
            return
        }
    }

    fun setLastReceivedMsg(lastReceivedMsg: String) {
        this.lastReceivedMsg = lastReceivedMsg

        count++
        countTextView.text = "Cnt: $count"

        if (initReceivedFlag) {
            intervalReceivedData()
        }
    }

    private fun intervalReceivedData() {
        receivedJob?.cancel()
        receivedJob = GlobalScope.async(Dispatchers.Main) {
            while (true) {
                if (lastReceivedMsg != "")
                    setBluetoothDataList()

                delay(getDelay(1000L))
            }
        }
    }

    private fun setBluetoothDataList() {
//        val start = System.currentTimeMillis()
        val list: ArrayList<Float>? = CalculatorUtil.getChannelDataList(
                lastReceivedMsg,
                ampGainList,
                supplyVList,
                copyChannelList,
                zeroAdjustList,
                zeroClickFlag
        )

        zeroClickFlag = false

//        Log.d("Asu", "list: $list")
        lastReceivedMsg = ""
        if (list != null) {
            runBlocking {
                GlobalScope.launch {
                    mutex.withLock {
                        receivedData = list
                    }
                }
            }
            val orientation = resources.configuration.orientation
            when(graphType) {
                TIME_CHART -> setGridData(list, orientation)
                XY_CHART -> setXYTextData(list)
                TEXT -> setTextData(list)
            }
            channelCount = list.size

            if (initReceivedFlag) {
                Log.d("Asu", "set measure layout")
                initReceivedFlag = false
                updateOrientationUI(resources.configuration.orientation)
                topLayout.visibility = View.VISIBLE

                timeChartUtil?.clearChart()
                timeChartUtil?.dummyValueChart(copyChannelList)
                xyChartUtil?.clearChart()
                xyChartUtil?.dummyValueChart(copyChannelList)

                val floatList = ArrayList<Float>()
                for (item in list) {
                    floatList.add(item)
                }

                var channelNames = ""
                for (idx in copyChannelList.indices) {
                    if (idx > 0) {
                        channelNames += ","
                    }
                    channelNames += copyChannelList[idx].name
                }

                val layout = BluetoothMeasureUIManager.getCSVHeaderLayout(requireContext(), ExcelItem(-1, channelNames, floatList, -1))
                excelTopLayout.addView(layout)
                chartChange(true)

                if (channelCount < maxChannelCount) {
                    zeroAdjustList = ArrayList<Boolean>(zeroAdjustList.subList(0, channelCount))
                }
            }
        }

//        val diff = System.currentTimeMillis() - start
//        Log.d("Asu", "setBluetoothDataList diff: $diff")
    }

    private fun setGridData(list: ArrayList<Float>, orientation: Int) {
        // getOrNull 로 IndexOutOfBoundsException 크래시가 해결되지 않아 try catch 를 사용
        // 가끔 textList size 가 0 이 될 때가 있음, 원인를 모르겠음
        try {
            for (i in selectedChannelIndexList.indices) {
                val index = selectedChannelIndexList[i]
                val channel = copyChannelList[index]

                if(i < visibleChannelCount
                        && channel.isOn) {
                    when (orientation) {
                        Configuration.ORIENTATION_PORTRAIT -> {
                            if (list.size <= index) {
                                if (verticalTimeTextList[i].text != "-") {
                                    verticalTimeTextList[i].text = "-"
                                }
                                continue
                            }
                            setText(list[index], verticalTimeTextList[i], channel)
                        }

                        Configuration.ORIENTATION_LANDSCAPE -> {
                            if (list.size <= index) {
                                if (horizonTimeTextList[i].text != "-") {
                                    horizonTimeTextList[i].text = "-"
                                }
                                continue
                            }
                            setText(list[index], horizonTimeTextList[i], channel)
                        }
                    }
                }

            }
        } catch (e: IndexOutOfBoundsException) {
            Log.d("Asu", "error: ${e.localizedMessage}")
        }
    }

    private fun setXYTextData(list: ArrayList<Float>) {
        try {
            if (mode == SaveType.INTERVAL) {
                val xChannel = copyChannelList[xyXAxisType]
                setText(list[xyXAxisType], xTextView, xChannel)

                val yChannel = copyChannelList[xyYAxisType]
                setText(list[xyYAxisType], yTextView, yChannel)
            } else {
                xTextView.text = stepp
                var max = list[0]

                if (list.getOrNull(xyYAxisType) != null) {
                    max = list[xyYAxisType]
                }

                yTextView.text = max.toString()
            }

        } catch (e: IndexOutOfBoundsException) {

        }
    }

    private fun setTextData(list: ArrayList<Float>) {
        try {
            if (!textFlag) return
            for (idx in 0 until textChartChannelCount) {
                val index = idx + (currentPage * textChartChannelCount)
                if (list.size <= index) return
                val channel = copyChannelList[index]
                if (index < copyChannelList.size
                        && index < textList.size
                        && channel.isOn
                ) {
                    if (!textFlag) return
                    textList[index].text = "${TextUtil.floatToString(list[index], channel.decPoint)}"
//                    Log.d("Asu", "setTextData: ${textList[index].text}")
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.d("Asu", "error: ${e.localizedMessage}")
        }
    }

    private fun setText(text: Float, tv: TextView, channel: CopyChannel) {
        if (UnitType.fromInt(channel.unit) != UnitType.DIRECT_INPUT)
            tv.text = "${TextUtil.floatToString(text, channel.decPoint)} (${UnitType.fromInt(channel.unit).getTitle(requireContext())})"
        else
            tv.text = "${TextUtil.floatToString(text, channel.decPoint)} (${channel.unitInput})"
    }

    fun getChannelSettingString(): String {
        if (!initReceivedFlag) {
            appendMeasure()
        }

        val sendString= StringBuilder("")
        ampGainList.clear()
        supplyVList.clear()

        for (idx in 0 until maxChannelCount) {
            sendString.append("*U") // 설정 명령어
            sendString.append(String.format("%02d", idx)) // 채널 번호: 2자리
            sendString.append(String.format("%01d", if (copyChannelList[idx].isOn) 1 else 0)) // on/off: 한자리
            sendString.append(String.format("%02d", copyChannelList[idx].sensorType)) // 센서 종류: 2자리

            val sensorType = SensorType.fromInt(copyChannelList[idx].sensorType)
            val strainGageType = StrainGageRangeType.fromInt(realmDataLog.strainGageRange)
            val sensorGageType = SensorGageRangeType.fromInt(realmDataLog.sensorGageRange)
            val copyChannel = copyChannelList[idx]
            val ampGain = sensorType.getAmpGain(strainGageType, sensorGageType, copyChannel)
            val applyV = sensorType.getAppliedVoltage(sensorGageType, copyChannel)

            sendString.append(ampGain) // 엠프게인: 1자리
            sendString.append(applyV) // 인가전압: 1자리
            sendString.append(String.format("%01d", copyChannelList[idx].filter)) // filter: 한자리
            sendString.append("$")

            ampGainList.add(ampGain.toFloat())
            supplyVList.add(applyV.toFloat())
        }

        sendString.append("*E$")
        sendString.append("*S$")

//        Log.d("Asu", "sendString: $sendString")
        return sendString.toString()
    }

    private fun getDelay(interval: Long): Long {
        val measureSpeed1000 = (measureSpeed * 1000.0).toLong()
        return (interval * 1000L) / measureSpeed1000
    }

    private fun setFirstValueList() {
        val data = PreferenceManager.getString(PreferenceKey.ZERO_POINT_LIST)
        val dataList = ArrayList<String>(data.split(","))
        val firstValueList = ArrayList<Float>()

        var i = 0
        while(true) {
            if (i < dataList.size) {
                if (dataList[i].toFloatOrNull() == null) {
                    dataList.removeAt(i)
                    continue
                }

                val value = dataList[i].toFloat()
                firstValueList.add(value)
                i++
            } else {
                break
            }
        }

        CalculatorUtil.setFirstValueData(firstValueList)

        val size = if (firstValueList.size > zeroAdjustList.size) zeroAdjustList.size else firstValueList.size
        for (i in 0 until size) {
            zeroAdjustList[i] = firstValueList[i] != 0.0F
        }
    }
}

