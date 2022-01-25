package kr.co.greentech.dataloggerapp.fragment.review.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.dialog.*
import kr.co.greentech.dataloggerapp.fragment.channel.enums.GraphAxisType
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.BluetoothMeasureUIManager
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType.*
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.realm.RealmGraphScaleSet
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.realm.copy.CopyGraphScale
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.ChartUtil
import kr.co.greentech.dataloggerapp.util.FileUtil
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.FileManager
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel.ExcelHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel.ExcelItem
import kr.co.greentech.dataloggerapp.util.textview.VerticalTextView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FragmentGraphReview: Fragment() {

    companion object {
        const val MAX_GRAPH_POINT_COUNT = 20000

        const val TIME_CHART = 0
        private const val XY_CHART = 1
        private const val EXCEL = 2

        fun newInstance(file: File): FragmentGraphReview {
            val f = FragmentGraphReview()
            val args = Bundle()
            args.putSerializable("file", file)
            f.arguments = args
            return f
        }
    }

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var excelLayout: HorizontalScrollView
    private val bufferSize = 50
    private var receivedDataList = ArrayList<Any>()
    private var excelCount = 1
    private var readLineCount = 0
    private var addExcelFlag = false

    private var graphType = 0

    private lateinit var file: File
    private lateinit var timeChartUtil: ChartUtil
    private lateinit var timeChart: LineChart
    private lateinit var xyChartUtil: ChartUtil
    private lateinit var xyChart: LineChart
    private lateinit var xAxisTitle: TextView
    private lateinit var leftYAxisTitle: VerticalTextView
    private lateinit var rightYAxisTitle: VerticalTextView
    private lateinit var chartLayout: ConstraintLayout
    private lateinit var progressLayout: FrameLayout
    private lateinit var readLine: List<String>

    private lateinit var excelTopLayout: LinearLayout

    private lateinit var menuPortrait: ConstraintLayout
    private lateinit var menuLandscape: ConstraintLayout
    private lateinit var downloadPortrait: ImageButton
    private lateinit var downloadLandscape: ImageButton
    private lateinit var filterPortrait: ImageButton
    private lateinit var filterLandscape: ImageButton

    private var xyXAxisType = 0
    private var xyYAxisType = 1

    private var interval: Long = -1L
    private var skipCount: Int = 0
    private var channelCount: Int = 0
    private var channelNames: String = ""
    private lateinit var mode: SaveType

    private lateinit var copyChannelList: List<CopyChannel>
    private var filterList = ArrayList<Boolean>()
    private var visibleRightTitleFlag = false
    private var editLineMap = HashMap<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        file = args?.getSerializable("file") as File

        copyChannelList = RealmChannel.getCopyChannelList()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_graph_review, container, false)
        chartLayout = view.findViewById(R.id.chart_layout)
        timeChart = view.findViewById(R.id.time_chart)
        timeChartUtil = ChartUtil(timeChart, requireContext())
        timeChartUtil.setUpLineChart(true)
        visibleRightTitleFlag = timeChartUtil.setupGraphAxis()

        xyChart = view.findViewById(R.id.xy_chart)
        xyChartUtil = ChartUtil(xyChart, requireContext())
        xyChartUtil.setUpLineChart(true)

        xAxisTitle = view.findViewById(R.id.title_x_axis)
        leftYAxisTitle = view.findViewById(R.id.title_left_axis)
        rightYAxisTitle = view.findViewById(R.id.title_right_axis)

        menuPortrait = view.findViewById(R.id.menu_portrait)
        menuLandscape = view.findViewById(R.id.menu_landscape)

        excelTopLayout = view.findViewById(R.id.excel_top_layout)
        recyclerView = view.findViewById(R.id.recyclerView)
        excelLayout = view.findViewById(R.id.excel_layout)

        adapter = RecyclerViewAdapter(this, receivedDataList as ArrayList<Any>)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mode == INTERVAL) {
                    if (!recyclerView.canScrollVertically(1)) {
                        addExcelItem()
                    }
                }
            }
        })

        setAxisTitle()

        downloadPortrait = view.findViewById<ImageButton>(R.id.download_portrait)
        downloadLandscape = view.findViewById<ImageButton>(R.id.download_landscape)

        val downloadClick = View.OnClickListener {
            if (graphType == TIME_CHART || graphType == XY_CHART) {
                AlertUtil.alertOkAndCancel(
                    requireContext(),
                        getString(R.string.download_alert_title),
                        getString(R.string.save)
                ) { _, _ ->
                    downloadImage()
                }
            }
        }

        downloadPortrait.setOnClickListener(downloadClick)
        downloadLandscape.setOnClickListener(downloadClick)

        filterPortrait = view.findViewById<ImageButton>(R.id.filter_portrait)
        filterLandscape = view.findViewById<ImageButton>(R.id.filter_landscape)

        val filterClick = View.OnClickListener {
            if (graphType == TIME_CHART) {
                alertFilterGraph(
                    requireContext(),
                        channelCount
                )
            }
        }
        filterPortrait.setOnClickListener(filterClick)
        filterLandscape.setOnClickListener(filterClick)


        val graphPortrait = view.findViewById<ImageButton>(R.id.change_portrait)
        val graphLandscape = view.findViewById<ImageButton>(R.id.change_landscape)

        val graphClick = View.OnClickListener {

            ++graphType
            if(EXCEL < graphType || graphType < TIME_CHART) {
                graphType = 0
            }

            when(graphType) {
                TIME_CHART -> {
                    if (editLineMap.isNotEmpty()) {
                        FileManager.editExcelFile(file, readLine, editLineMap)
                        drawGraph()
                    }

                    timeChart.visibility = View.VISIBLE
                    xyChart.visibility = View.INVISIBLE
                    leftYAxisTitle.visibility = View.VISIBLE
                    xAxisTitle.visibility = View.VISIBLE
                    visibleRightTitle(visibleRightTitleFlag)

                    excelLayout.visibility = View.INVISIBLE

                    graphPortrait.setImageResource(R.drawable.ic_escalator_white_24)
                    graphLandscape.setImageResource(R.drawable.ic_escalator_white_24)
                    setAxisTitle()

                    xAxisTitle.text = "Time (sec)"

                    filterPortrait.alpha = 1.0F
                    filterLandscape.alpha = 1.0F
                    downloadPortrait.alpha = 1.0F
                    downloadLandscape.alpha = 1.0F
                }

                XY_CHART -> {
                    timeChart.visibility = View.INVISIBLE
                    xyChart.visibility = View.VISIBLE
                    leftYAxisTitle.visibility = View.VISIBLE
                    rightYAxisTitle.visibility = View.INVISIBLE
                    xAxisTitle.visibility = View.VISIBLE
                    excelLayout.visibility = View.INVISIBLE

                    graphPortrait.setImageResource(R.drawable.ic_baseline_view_module_24)
                    graphLandscape.setImageResource(R.drawable.ic_baseline_view_module_24)

                    val xChannel = copyChannelList[xyXAxisType]
                    val yChannel = copyChannelList[xyYAxisType]

                    if (mode == INTERVAL) {
                        xAxisTitle.text = xChannel.name + " (${GraphAxisType.fromInt(xChannel.graphAxis).getTitle()})"
                    } else {
                        xAxisTitle.text = "Step p"
                    }
                    leftYAxisTitle.text = yChannel.name + " (${GraphAxisType.fromInt(yChannel.graphAxis).getTitle()})"

                    filterPortrait.alpha = 0.3F
                    filterLandscape.alpha = 0.3F
                    downloadPortrait.alpha = 1.0F
                    downloadLandscape.alpha = 1.0F
                }

                EXCEL -> {
                    if (readLineCount == 0) {
                        addExcelItem()
                    }
                    timeChart.visibility = View.INVISIBLE
                    xyChart.visibility = View.INVISIBLE
                    leftYAxisTitle.visibility = View.INVISIBLE
                    rightYAxisTitle.visibility = View.INVISIBLE
                    xAxisTitle.visibility = View.INVISIBLE

                    excelLayout.visibility = View.VISIBLE

                    graphPortrait.setImageResource(R.drawable.ic_graph_white_24)
                    graphLandscape.setImageResource(R.drawable.ic_graph_white_24)

                    filterPortrait.alpha = 0.3F
                    filterLandscape.alpha = 0.3F
                    downloadPortrait.alpha = 0.3F
                    downloadLandscape.alpha = 0.3F
                }
            }
        }

        graphPortrait.setOnClickListener(graphClick)
        graphLandscape.setOnClickListener(graphClick)

        val settingPortrait = view.findViewById<ImageButton>(R.id.setting_portrait)
        val settingLandscape = view.findViewById<ImageButton>(R.id.setting_landscape)

        val settingClick = View.OnClickListener {
            when(graphType) {
                TIME_CHART -> alertTimeChartSetting(requireContext())
                XY_CHART -> {
                    if (mode == INTERVAL) {
                        DialogFragmentChangeAxisAndScale
                                .newInstance(false)
                                .show(fragmentManager!!, "DialogFragmentChangeGraphYAxis")
                    } else {
                        DialogFragmentStepXYSetting
                                .newInstance()
                                .show(fragmentManager!!, "DialogFragmentStepXYSetting")
                    }
                }
                EXCEL -> {}
            }
        }

        settingPortrait.setOnClickListener(settingClick)
        settingLandscape.setOnClickListener(settingClick)

        progressLayout = view.findViewById(R.id.progress_layout)
        progressLayout.visibility = View.VISIBLE

        return view
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        GraphReviewManager.updateOrientationUI(
            requireContext(),
            newConfig.orientation,
            chartLayout,
            menuPortrait,
            menuLandscape
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as ActivityStart?)!!.setTitle(getString(R.string.graph_review))

        readLine = file.readLines()
        if (readLine.isEmpty()) {
            AlertUtil.alert(requireContext(), getString(R.string.empty_file_msg)) { _, _ ->
                fragmentManager?.popBackStack()
            }
            return
        }

        mode = if (readLine[0].contains("Step")) STEP else INTERVAL

        val set = RealmGraphScaleSet.select()!!

        xyXAxisType = set.xyXAxisType
        xyYAxisType = set.xyYAxisType

        if (mode == STEP)
            xyYAxisType = PreferenceManager.getInt(PreferenceKey.STEP_XY_Y_AXIS)

        drawGraph()
    }

    override fun onStart() {
        super.onStart()
        GraphReviewManager.updateOrientationUI(
            requireContext(),
            resources.configuration.orientation,
            chartLayout,
            menuPortrait,
            menuLandscape
        )
        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        super.onStop()
        GlobalBus.getBus().unregister(this)

        FileManager.editExcelFile(file, readLine, editLineMap)
    }

    private fun drawIntervalGraph(intervalList: ArrayList<ArrayList<Float>>) {
        GlobalScope.async(Dispatchers.Main) {
            timeChartUtil.clearChart()

            val selectedChannelList = ArrayList<CopyChannel>()
            for (i in filterList.indices) {
                if(filterList[i]) {
                    selectedChannelList.add(copyChannelList[i])
                }
            }

            for (i in intervalList.indices) {
                timeChartUtil.addIntervalEntry(interval * (skipCount + 1), intervalList[i], selectedChannelList, false)
            }

            progressLayout.visibility = View.INVISIBLE
            visibleRightTitle(visibleRightTitleFlag)
            chartLayout.visibility = View.VISIBLE
            timeChartUtil.chart.notifyDataSetChanged()
            timeChartUtil.chart.invalidate()


        }
    }

    private fun getIntervalDataList(readLine: List<String>): ArrayList<ArrayList<Float>> {
        if (readLine.isEmpty()) return ArrayList()
        val dataList = ArrayList<ArrayList<Float>>()

        progressLayout.post {
            progressLayout.visibility = View.VISIBLE
        }

        timeChartUtil.chart.post {
            chartLayout.visibility = View.INVISIBLE
        }

        channelCount = 0
        skipCount = 0

        var line = 0
        while(line < readLine.size) {
            val splitList = readLine[line].split(",")

            var flag = false
            val list = ArrayList<Float>()
            var listSize = 0
            for (idx in splitList.indices) {
                if (flag) {
                    if (splitList[idx].toFloatOrNull() == null) {
                        break
                    } else {
                        if (filterList[listSize]) {
                            list.add(splitList[idx].toFloat())
                        }

                        if (splitList.size - 1 == idx) {
                            flag = false
                            dataList.add(list)
                        }
                        listSize++
                    }
                } else {
                    // header data extract
                    flag = splitList[idx].toFloatOrNull() != null
                    checkChannelNames(readLine[line])

                    if (channelCount == 0) {
                        val string = splitList[idx].toLowerCase()
                        if (string.contains("interval")
                            || string.contains("measure")
                        ) {
                            val interval = splitList.getOrNull(idx + 1)?.toFloatOrNull()
                            if (interval != null) {
                                this.interval = (interval * 1000).toLong()
                            }
                        }

                        if (string.contains("channel number")) {
                            val floatChannelCount = splitList.getOrNull(idx + 1)?.toFloatOrNull()
                            if (floatChannelCount != null) {
                                val channelCount = floatChannelCount.toInt()
                                this.channelCount = channelCount

                                if (filterList.isEmpty()) {
                                    for (i in 0 until channelCount) {
                                        filterList.add(true)
                                    }
                                }

                                val size = filterList.filter { it }.size

                                if ((size * readLine.size) > MAX_GRAPH_POINT_COUNT) {
                                    skipCount = (size * readLine.size) / MAX_GRAPH_POINT_COUNT
                                    Log.d("Asu", "skipCount: $skipCount")
                                }
                            }
                        }
                    }
                }
            }

            if (skipCount > 0) {
                line += skipCount + 1
            } else {
                line++
            }
        }
        return dataList
    }

    private fun drawStepGraph(
            readLine: List<String>,
            copyChannelList: List<CopyChannel>
    ) {
        if (readLine.isEmpty()) return

        GlobalScope.async(Dispatchers.Main) {
            progressLayout.visibility = View.VISIBLE
            chartLayout.visibility = View.INVISIBLE
        }

        timeChartUtil.clearChart()

        var channelCount = 0
        var measureCount = 0
        var appendFlag = false
        var addInitFlag = false

        val selectChannelIndexList = ArrayList<Int>()

        for (line in readLine.indices) {

            val splitList = readLine[line].split(",")

            var flag = false
            var stepInterval: Float = 1F
            val list = ArrayList<Float>()
            for (idx in splitList.indices) {
                if (flag) {
                    if (splitList[idx].toFloatOrNull() == null) {
                        break
                    } else {
                        list.add(splitList[idx].toFloat())
                        Log.d("Asu", "selectChannelIndexList: ${selectChannelIndexList}")
                        if (splitList.size - 1 == idx) {
                            if (selectChannelIndexList.size > 0) {
                                timeChartUtil.addFilterStepEntry(stepInterval, selectChannelIndexList, list, copyChannelList, false, measureCount)
                            }

                            if (!addInitFlag) addInitFlag = true
                        } else {
                        }
                    }
                } else {
                    if(appendFlag && addInitFlag) {
                        if (readLine[line].toUpperCase().contains("CH")) {
                            measureCount++
                        }
                    }

                    flag = splitList[idx].toFloatOrNull() != null
                    if (flag) {
                        stepInterval = splitList[idx].toFloat()
                    }
                    appendFlag = flag
                    checkChannelNames(readLine[line])

                    if (channelCount == 0) {
                        val string = splitList[idx].toLowerCase()
                        if (string.contains("channel number")) {
                            val floatChannelCount = splitList.getOrNull(idx + 1)?.toFloatOrNull()
                            if (floatChannelCount != null) {
                                channelCount = floatChannelCount.toInt()
                                this.channelCount = channelCount

                                if (filterList.isEmpty()) {
                                    for (i in 0 until channelCount) {
                                        filterList.add(true)
                                    }
                                }

                                for (i in filterList.indices) {
                                    if(filterList[i]) {
                                        selectChannelIndexList.add(i)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        GlobalScope.async(Dispatchers.Main) {
            progressLayout.visibility = View.INVISIBLE
            chartLayout.visibility = View.VISIBLE
            visibleRightTitle(visibleRightTitleFlag)
            timeChartUtil.chart.notifyDataSetChanged()
            timeChartUtil.chart.invalidate()
        }
    }

    private fun drawIntervalXYGraph(
            readLine: List<String>,
            xIdx: Int,
            yIdx: Int
    ) {
        if (readLine.isEmpty()) return

        GlobalScope.async {
            val dataList = ArrayList<ArrayList<Float>>()

            var line: Int = 0
            var breakFlag = true
            var headerFlag = true
            var skipCount = 0

            xyChartUtil.clearChart()

            while (line < readLine.size && breakFlag) {
                val splitList = readLine[line].split(",")

                var flag = false
                val list = ArrayList<Float>()
                for (idx in splitList.indices) {
                    if (flag) {
                        if (splitList[idx].toFloatOrNull() == null) {
                            if (dataList.size > 0) {
                                breakFlag = false
                            }
                            break
                        } else {
                            if (splitList.size - 1 == idx) {
                                flag = false
                                list.add(splitList[idx].toFloat())
                                dataList.add(list)
                            } else {
                                list.add(splitList[idx].toFloat())
                            }
                        }
                    } else {
                        // header data extract
                        flag = splitList[idx].toFloatOrNull() != null

                        if (headerFlag) {
                            val string = splitList[idx].toLowerCase()

                            if (string.contains("channel number")) {
                                val floatChannelCount = splitList.getOrNull(idx + 1)?.toFloatOrNull()
                                if (floatChannelCount != null) {
                                    headerFlag = false
                                    val channelCount = floatChannelCount.toInt()

                                    if ((channelCount * readLine.size) > MAX_GRAPH_POINT_COUNT) {
                                        skipCount = (channelCount * readLine.size) / MAX_GRAPH_POINT_COUNT
                                    }
                                }
                            }
                        }
                    }
                }

                if (skipCount > 0) {
                    line += skipCount
                } else {
                    line++
                }
            }

            GlobalScope.async(Dispatchers.Main) {
                for (list in dataList) {
                    val l = ArrayList<Float>()
                    if (yIdx < list.size) {
                        l.add(list[yIdx])
                        xyChartUtil.addIntervalXYEntry(list, xIdx, yIdx, false)
                    } else {
                        break
                    }
                }

                progressLayout.visibility = View.INVISIBLE
                chartLayout.visibility = View.VISIBLE

                xyChart.notifyDataSetChanged()
                xyChart.invalidate()
            }
        }
    }

    private fun drawStepXYGraph(
            readLine: List<String>
    ) {
        if (readLine.isEmpty()) return

        GlobalScope.async(Dispatchers.Main) {
            progressLayout.visibility = View.VISIBLE
            chartLayout.visibility = View.INVISIBLE
        }

        xyChartUtil.clearChart()

        var stepp = "0"

        for (line in readLine.indices) {

            val splitList = readLine[line].split(",")

            var flag = false
            val list = ArrayList<Float>()
            var max = Float.MIN_VALUE
            for (idx in splitList.indices) {
                if (flag) {
                    if (splitList[idx].toFloatOrNull() == null) {
                        break
                    } else {
                        list.add(splitList[idx].toFloat())
                        if (splitList.size - 1 == idx) {
                            max = list[0]

                            if (list.getOrNull(xyYAxisType) != null) {
                                max = list[xyYAxisType]
                            }

                            xyChartUtil.addStepXYEntry(stepp.toFloat(), max, false)
                            Log.d("Asu", "drawStepXYGraph, x: ${stepp.toFloat()}, y: $max")
                        }
                    }
                } else {
                    flag = splitList[idx].toFloatOrNull() != null

                    val string = splitList[idx].toLowerCase()
                    if (string.contains("horizontal pressure force :")) {
                        stepp = splitList[idx + 1].toFloat().toString()
                    }
                }
            }
        }

        GlobalScope.async(Dispatchers.Main) {
            progressLayout.visibility = View.INVISIBLE
            chartLayout.visibility = View.VISIBLE
            xyChartUtil.chart.notifyDataSetChanged()
            xyChartUtil.chart.invalidate()
        }
    }

    private fun addExcelItem() {
        if (addExcelFlag) return
        addExcelFlag = true

        var count = if (mode == INTERVAL) bufferSize else MAX_GRAPH_POINT_COUNT
        while(
                readLineCount < readLine.size
                && count > 0
        ) {
            val splitList = readLine[readLineCount].split(",")

            var flag = false
            val list = ArrayList<Float>()
            val item = ExcelItem(excelCount, "", list, readLineCount)

            for (idx in splitList.indices) {
                if (flag) {
                    if (splitList[idx].toFloatOrNull() == null) {
                        break
                    } else {
                        list.add(splitList[idx].toFloat())

                        if (splitList.size - 1 == idx) {
                            flag = false
                            receivedDataList.add(item)
                            count--
                            excelCount++
                        }
                    }
                } else {
                    // header data extract
                    flag = splitList[idx].toFloatOrNull() != null
                    checkChannelNames(readLine[readLineCount])

                    if (flag) {
                        val time = splitList[idx].toFloat().toLong()
                        item.elapsedTime = FileUtil.getElapsedTime(0, time * 1000, 1.0F)
                    }

                    val string = splitList[idx].toLowerCase()

                    if (channelCount == 0) {
                        if (string.contains("channel number")) {
                            val floatChannelCount = splitList.getOrNull(idx + 1)?.toFloatOrNull()
                            if (floatChannelCount != null) {
                                val channelCount = floatChannelCount.toInt()
                                this.channelCount = channelCount
                            }
                        }
                    }

                    if (string.contains("horizontal pressure force :")) {
                        val stepp = splitList[idx + 1].toFloat().toInt().toString()
                        receivedDataList.add(stepp)
                    }
                }
            }

            readLineCount++
        }

        if (excelTopLayout.childCount == 0) {
            val floatList = ArrayList<Float>()
            for (item in 0 until channelCount) {
                floatList.add(0.0F)
            }

            val layout = BluetoothMeasureUIManager.getCSVHeaderLayout(requireContext(), ExcelItem(-1, channelNames, floatList, -1))
            excelTopLayout.addView(layout)
        }

        adapter.notifyDataSetChanged()

        addExcelFlag = false
    }

    private fun alertFilterGraph(
            context: Context,
            channelCount: Int
    ) {
        val list = copyChannelList.subList(0, channelCount).map { it.name }.toTypedArray()

        val checkList = ArrayList<Boolean>()
        for(item in filterList) {
            checkList.add(item)
        }

        AlertDialog
                .Builder(context)
                .setTitle(getString(R.string.filter_graph))
                .setMultiChoiceItems(
                        list,
                        checkList.toBooleanArray()
                ) {
                    _: DialogInterface?, which: Int, isChecked: Boolean ->
                    checkList[which] = isChecked
                }
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    if(checkList.isEmpty()) return@setPositiveButton

                    for (idx in list.indices) {
                        filterList[idx] = checkList[idx]
                    }

                    if (mode == INTERVAL) {
                        GlobalScope.async {
                            val intervalList = getIntervalDataList(readLine)
                            drawIntervalGraph(intervalList)
                        }
                    } else {
                        drawStepGraph(readLine, copyChannelList)
                    }
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: MapEvent) {
        val map = event.map

        val adjustGraphRangeFragment = map.getOrDefault(DialogFragmentChangeGraphXAxis.toString(), null)
        if(adjustGraphRangeFragment != null) {
            val xAxis = map.getOrDefault("xAxis", null) as? CopyGraphScale
            if (xAxis != null) {
                timeChartUtil.setGraphXScale(xAxis)
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

            val selectedChannelIndexList = ArrayList<Int>()
            for (i in filterList.indices) {
                if (filterList[i])
                    selectedChannelIndexList.add(i)
            }

            timeChartUtil.changeGraphYAxis(
                    firstAxisType,
                    secondAxisType,
                    copyChannelList,
                    selectedChannelIndexList
            )

            if (isTimeChart) {
                visibleRightTitleFlag = firstAxisType == secondAxisType
                visibleRightTitle(visibleRightTitleFlag)
                timeChartUtil.setGraphYScale(firstAxisScale, secondAxisScale, firstAxisType == secondAxisType)
                setAxisTitle(firstAxisType, secondAxisType)
            } else {
                xyChartUtil.setGraphLeftYScale(secondAxisScale)
                xyChartUtil.setGraphXScale(firstAxisScale)

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

                drawIntervalXYGraph(readLine, xyXAxisType, xyYAxisType)
            }

            return
        }

        val stepXYSetting = map.getOrDefault(DialogFragmentStepXYSetting.toString(), null)
        if(stepXYSetting != null) {

            val firstAxisType = map.getOrDefault("firstAxisType", null) as Int

            val firstAxisScale = map.getOrDefault("firstAxisScale", null) as CopyGraphScale
            val secondAxisScale = map.getOrDefault("secondAxisScale", null) as CopyGraphScale

            xyChartUtil.setGraphLeftYScale(secondAxisScale)
            xyChartUtil.setGraphXScale(firstAxisScale)

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

            drawStepXYGraph(readLine)
            return
        }

        val excelHolderString = map.getOrDefault(ExcelHolder.toString(), null)
        if (excelHolderString != null) {
            editExcelItemAlert(map)
        }
    }

    private fun editExcelItemAlert(map: HashMap<String, Any>) {
        val excelItem = map.getOrDefault("excelItem", null) as ExcelItem
        val index = map.getOrDefault("index", null) as Int
        val position = map.getOrDefault("position", null) as Int

        val bobTheBuilder = android.app.AlertDialog.Builder(context)
        bobTheBuilder.setView(R.layout.dialog_rename).setTitle(getString(R.string.edit) + " (CH${index + 1}, ${position + 1})")
        val alert = bobTheBuilder.create()
        alert.show()

        val editText = alert.findViewById<EditText>(R.id.renameText)
        editText.hint = getString(R.string.edit_text_placeholder)
        editText.setText(excelItem.dataList[index].toString())
        editText.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL

        val ok = alert.findViewById<Button>(R.id.ok)
        val cancel = alert.findViewById<Button>(R.id.cancel)
        ok.setOnClickListener {
            if (editText.text.toString().toFloatOrNull() != null) {
                editExcelItem(map, editText.text.toString())
                alert.cancel()
            } else {
                AlertUtil.alert(requireContext(), getString(R.string.edit_text_placeholder))
            }
        }
        cancel.setOnClickListener { alert.cancel() }
    }

    private fun editExcelItem(map: HashMap<String, Any>, editTextString: String) {
        val excelItem = map.getOrDefault("excelItem", null) as ExcelItem
        val index = map.getOrDefault("index", null) as Int
        val position = map.getOrDefault("position", null) as Int

        val string =
                if (editLineMap[excelItem.readLine] != null)
                    editLineMap[excelItem.readLine]!!
                else
                    readLine[excelItem.readLine]

        val splitList = string.split(",")

        val editString: StringBuilder = java.lang.StringBuilder("")

        for (idx in splitList.indices) {
            if (index == idx - 2) {
                editString.append(", ${editTextString.toFloat()}")
            } else if (idx == 0) {
                editString.append(splitList[idx])
            } else {
                editString.append(", ${splitList[idx].toFloat()}")
            }
        }

        excelItem.dataList[index] = editTextString.toFloat()
        editLineMap[excelItem.readLine] = editString.toString()
        adapter.notifyItemChanged(position)

        Log.d("Asu", "원본: ${readLine[excelItem.readLine]}")
        Log.d("Asu", "수정: $editString")
        Log.d("Asu", "경로: ${file.absolutePath}")
    }

    private fun downloadImage() {
        if (GraphReviewManager.captureLayout(chartLayout, file))
            Toast.makeText(context, getString(R.string.download_success_msg), Toast.LENGTH_SHORT).show()
        else {
            AlertUtil.alertOkAndCancel(
                requireContext(),
                    getString(R.string.save_to_gallery_fail_msg),
                    getString(R.string.overwrite)
            ) { _, _ ->
                if (GraphReviewManager.captureLayout(chartLayout, file, true))
                    Toast.makeText(context, getString(R.string.download_success_msg), Toast.LENGTH_SHORT).show()
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

    private fun alertTimeChartSetting(
            context: Context
    ) {
        val titles = arrayOf(
                context.getString(R.string.setting_x_axis),
                context.getString(R.string.setting_y_axis),
        )

        AlertDialog
                .Builder(context)
                .setTitle(context.getString(R.string.select))
                .setItems(titles) { _, which ->
                    when(which) {
                        0 -> {
                            DialogFragmentChangeGraphXAxis
                                    .newInstance()
                                    .show(fragmentManager!!, "DialogFragmentAdjustGraphRange")
                        }
                        1 -> {
                            DialogFragmentChangeAxisAndScale
                                    .newInstance(true)
                                    .show(fragmentManager!!, "DialogFragmentChangeGraphYAxis")
                        }
                        else -> {}
                    }
                }
                .setNeutralButton(context.getString(R.string.cancel), null)
                .show()
    }

    private fun drawGraph() {
        readLine = file.readLines()
        GlobalScope.async {

            when(mode) {
                INTERVAL -> {
                    val intervalList = getIntervalDataList(readLine)
                    drawIntervalGraph(intervalList)
                    drawIntervalXYGraph(readLine, xyXAxisType, xyYAxisType)
                }

                STEP -> {
                    drawStepGraph(readLine, copyChannelList)
                    drawStepXYGraph(readLine)
                }
            }
        }
    }

    private fun checkChannelNames(string: String) {
        if (string.toLowerCase(Locale.ROOT).contains("datetime")) {
            if (channelCount >= 0 && channelNames == "") {
                val split = string.split(",")
                val subList =
                    split.subList(split.size - channelCount, split.size)
                var channelNames = ""
                for (idx in subList.indices) {
                    if (idx != 0) {
                        channelNames += ","
                    }
                    channelNames += subList[idx]
                }
                this.channelNames = channelNames

                try {
                    val names = channelNames.split(",")
                    for (idx in names.indices) {
                        copyChannelList[idx].name = names[idx]
                    }
                } catch (e: Exception) {
                    Log.d("Asu", e.localizedMessage)
                }
            }
        }
    }
}


