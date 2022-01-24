package kr.co.greentech.dataloggerapp.util

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType.INTERVAL
import kr.co.greentech.dataloggerapp.fragment.savesetting.enum.SaveType.STEP
import kr.co.greentech.dataloggerapp.realm.RealmGraphScaleSet
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.realm.copy.CopyGraphScale
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager

class ChartUtil(
    val chart: LineChart,
    val context: Context
) {

    var leftYAxis = PreferenceManager.DEFAULT_VALUE_INT
    var rightYAxis = PreferenceManager.DEFAULT_VALUE_INT

    val saveSetting = RealmSaveSetting.select()!!

    private val bufferSize = PreferenceManager.getInt(PreferenceKey.GRAPH_BUFFER_SIZE)
    private val graphLineType = PreferenceManager.getInt(PreferenceKey.GRAPH_LINE_TYPE)

    var isReview: Boolean = false
    private var xScale: CopyGraphScale? = null

    var xMax: Float = 0.0F

    fun setUpLineChart(isReview: Boolean = false) {
        this.isReview = isReview

        val graphGridType = PreferenceManager.getBoolean(PreferenceKey.GRAPH_GRID_TYPE)

        val textSize: Float = 12.0F
        chart.setBackgroundColor(DataLogApplication.getColor(R.color.itemBackground))
        chart.setDrawGridBackground(false)

        chart.description.isEnabled = false

        chart.setTouchEnabled(isReview)
        chart.isClickable = isReview
        chart.isDragEnabled = isReview
        chart.setScaleEnabled(isReview)
        chart.setPinchZoom(isReview)

        chart.setDrawGridBackground(false)
        val data = LineData()
        data.setValueTextColor(DataLogApplication.getColor(R.color.font))
        chart.data = data

        val l = chart.legend
        l.form = Legend.LegendForm.LINE
        l.textColor = DataLogApplication.getColor(R.color.font)
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.textSize = textSize

        val xl = chart.xAxis
        xl.textColor = DataLogApplication.getColor(R.color.font)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true
        xl.position = XAxis.XAxisPosition.BOTTOM
        xl.granularity = 1.0F
        xl.isGranularityEnabled = true

        xl.textSize = textSize

        val leftAxis = chart.axisLeft
        leftAxis.textColor = DataLogApplication.getColor(R.color.font)
        leftAxis.textSize = textSize

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        rightAxis.textSize = textSize


        if (!graphGridType) {
            xl.enableGridDashedLine(10f, 10f, 0f)
            leftAxis.enableGridDashedLine(10f, 10f, 0f)
            rightAxis.enableGridDashedLine(10f, 10f, 0f)
        }
    }

    fun refresh() {
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    fun setupGraphAxis(): Boolean {
        val set = RealmGraphScaleSet.select()!!

        leftYAxis = set.timeLeftYAxisType
        rightYAxis = set.timeRightYAxisType

        val x = set.timeXAxis[0]!!.getCopy()
        setGraphXScale(x)

        val leftY = set.timeYAxis[leftYAxis]!!.getCopy()
        val rightY = set.timeYAxis[rightYAxis]!!.getCopy()

        val rightAxis = chart.axisRight
        rightAxis.textColor = DataLogApplication.getColor(R.color.font)

        setGraphYScale(leftY, rightY, leftYAxis == rightYAxis)
        return leftYAxis == rightYAxis
    }

    fun setGraphXScale(x: CopyGraphScale) {
        xScale = x

        val xl = chart.xAxis
        chart.fitScreen()
        if (x.isOn) {
            xl.resetAxisMinimum()
            xl.resetAxisMaximum()
        } else {
            xl.axisMinimum = x.min
            xl.axisMaximum = x.max
        }

        refresh()
    }

    fun setGraphLeftYScale(leftY: CopyGraphScale) {
        val leftAxis = chart.axisLeft
        if (leftY.isOn) {
            leftAxis.resetAxisMinimum()
            leftAxis.resetAxisMaximum()
        } else {
            leftAxis.axisMinimum = leftY.min
            leftAxis.axisMaximum = leftY.max
        }

        refresh()
    }

    fun setGraphYScale(
        leftY: CopyGraphScale,
        rightY: CopyGraphScale,
        isEqual: Boolean
    ) {
        val leftAxis = chart.axisLeft
        if (leftY.isOn) {
            leftAxis.resetAxisMinimum()
            leftAxis.resetAxisMaximum()
        } else {
            leftAxis.axisMinimum = leftY.min
            leftAxis.axisMaximum = leftY.max
        }

        val rightAxis = chart.axisRight
        if (rightY.isOn) {
            rightAxis.resetAxisMinimum()
            rightAxis.resetAxisMaximum()
        } else {
            rightAxis.axisMinimum = rightY.min
            rightAxis.axisMaximum = rightY.max
        }

        rightAxis.isEnabled = !isEqual

        refresh()
    }

    private fun setupScale() {
        val x = xScale
        if (x != null) {
            if (x.isOn && !isReview) {
                chart.setVisibleXRangeMaximum(bufferSize.toFloat() * saveSetting.interval)
            }
        }
    }

    fun changeGraphYAxis(
        leftYAxis: Int,
        rightYAxis: Int,
        copyChannelList: List<CopyChannel>,
        selectedChannelIndexList: ArrayList<Int>
    ) {
        val sets = chart.data.dataSets

        for(idx in sets.indices) {
            val index = selectedChannelIndexList[idx % selectedChannelIndexList.size]
            val copyChannel = copyChannelList.getOrNull(index)
            val set = sets[idx]
            if (copyChannel != null) {
                if (copyChannel.graphAxis == rightYAxis) {
                    set.axisDependency = YAxis.AxisDependency.RIGHT
                } else {
                    set.axisDependency = YAxis.AxisDependency.LEFT
                }
            }
        }

        this.leftYAxis = leftYAxis
        this.rightYAxis = rightYAxis
    }

    fun clearChart() {
        chart.clearValues()
        xMax = 0.0F
    }

    fun dummyValueChart(channelList: List<CopyChannel>) {
        if(xScale == null) {
            xScale = CopyGraphScale(true, 0F, 100F)
        }
        addIntervalEntry(1000L, listOf((Math.random().toFloat() * 40) + 30F), channelList)
        addIntervalEntry(1000L, listOf((Math.random().toFloat() * 400) + 30F), channelList)
        setVisible(0, false)

        xMax = 0.0F
    }

    fun addFilterStepEntry(
        x: Float,
        selectedChannelIndexList: ArrayList<Int>,
        list: List<Float>,
        copyChannelList: List<CopyChannel>,
        isUpdate: Boolean = true,
        measureCount: Int = 0
    ) {
        addFilterEntry(
            STEP,
            1000L,
            x,
            selectedChannelIndexList,
            list,
            copyChannelList,
            isUpdate,
            measureCount
        )
    }

    private fun addFilterEntry(
        type: SaveType,
        interval: Long,
        x: Float,
        selectedChannelIndexList: ArrayList<Int>,
        list: List<Float>,
        copyChannelList: List<CopyChannel>,
        isUpdate: Boolean = true,
        measureCount: Int = 0
    ) {
        val newCopyChannelList = ArrayList<CopyChannel>()

        for (i in selectedChannelIndexList) {
            newCopyChannelList.add(copyChannelList[i])
        }

        val newList = ArrayList<Float>()
        for (i in selectedChannelIndexList) {
            if (i < list.size) {
                newList.add(list[i])
            } else {
                break
            }
        }

        addEntry(
            type,
            interval,
            x,
            newList,
            newCopyChannelList,
            isUpdate,
            measureCount
        )
    }


    fun addIntervalEntry(
        interval: Long,
        list: List<Float>,
        copyChannelList: List<CopyChannel>,
        isUpdate: Boolean = true,
        measureCount: Int = 0
    ) {
        addEntry(
            INTERVAL,
            interval,
            1.0F,
            list,
            copyChannelList,
            isUpdate,
            measureCount
        )
    }

    fun addStepEntry(
        x: Float,
        list: List<Float>,
        copyChannelList: List<CopyChannel>,
        isUpdate: Boolean = true,
        measureCount: Int = 0,
        stepList: ArrayList<Long>? = null
    ) {
        addEntry(
            STEP,
            1000L,
            x,
            list,
            copyChannelList,
            isUpdate,
            measureCount,
                stepList
        )
    }

    fun addIntervalXYEntry(
        list: List<Float>,
        xIdx: Int,
        yIdx: Int,
        isUpdate: Boolean
    ) {
        try {
            if (list.isEmpty()) return
            if (list.size <= xIdx || list.size <= yIdx) return

            val idx = 0
            val x = list[xIdx]
            val y = list[yIdx]
            val data = chart.data ?: return
            var set = data.getDataSetByIndex(idx)

            if (set == null) {
                val color = DataLogApplication.getColor(R.color.edit)
                set = createSet("xy", x, y, color)
                data.addDataSet(set)
            } else {
                if (set.entryCount > 0) {
                    if (x <= set.xMax) return
                }
            }

            data.addEntry(Entry(x, y), idx)
            if (isUpdate) {
                chart.notifyDataSetChanged()
                chart.moveViewToX(data.entryCount.toFloat())
            }
        } catch (e: Exception) {
            Log.d("Asu", e.stackTrace.toString())
        }
    }

    fun addStepXYEntry(
            x: Float,
            y: Float,
            isUpdate: Boolean
    ) {
        val data = chart.data ?: return
        val idx = 0

        var set = data.getDataSetByIndex(idx)

        if (set == null) {
            val color = DataLogApplication.getColor(R.color.edit)
            set = createSet("xy", x, y, color)
            data.addDataSet(set)
        }

        if (set.entryCount > 0) {
            val entry = set.getEntryForIndex(set.entryCount - 1)

            if (entry.x == x) {
                if (entry.y < y)
                    set.removeLast()
                else
                    return
            } else if (entry.x > x)
                return
        }

        data.addEntry(Entry(x, y), idx)
        Log.d("Asu", "addStepXYEntry, x: $x, y: $y")
        if (isUpdate) {
            chart.notifyDataSetChanged()
            chart.moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun addEntry(
        type: SaveType,
        interval: Long,
        x: Float,
        list: List<Float>,
        copyChannelList: List<CopyChannel>,
        isUpdate: Boolean = true,
        measureCount: Int = 0,
        stepList: ArrayList<Long>? = null
    ) {
        if (list.isEmpty()) return
        if (copyChannelList.isEmpty()) return

        val data = chart.data
        if (data != null) {
            var isOnCount = 0
            for (i in list.indices) {
                if (i < list.size
                        && i < copyChannelList.size
                        && (copyChannelList[i].isOn || isReview)
                ) {
                    val idx = isOnCount + (list.size * measureCount)
                    val axis = copyChannelList[i].graphAxis
                    val flag = (rightYAxis != axis) || (leftYAxis == rightYAxis)
                    var set = data.getDataSetByIndex(idx)
                    if (set == null) {
                        set = createSet(list[i], copyChannelList[i], measureCount, isAxis = flag)
                        data.addDataSet(set)
                    }

                    when(type) {
                        INTERVAL -> {
                            val x = xMax * interval.toFloat() / 1000.0F
                            data.addEntry(Entry(x, list[i]), idx)

                            while (
                                    !isReview
                                    && bufferSize + 10 < set.entryCount
                            ) {
                                set.removeFirst()
                            }

                            if (isUpdate) {
                                chart.notifyDataSetChanged()
                                setupScale()
                                chart.moveViewToX(x)
                            }
                        }

                        STEP -> {
                            data.addEntry(Entry(x, list[i]), idx)

                            val curData = x.toInt()
                            if (curData % 60 == 0 && stepList != null) {
                                var idx = 0
                                var size = set.entryCount - 1

                                while(idx < size) {
                                    val entry = set.getEntryForIndex(idx)
                                    val prevData = entry.x.toInt()
                                    if (prevData % 60 == 0) {
                                        idx++
                                    } else {
                                        size--
                                        set.removeEntry(entry)
                                    }
                                }

                                val data = x.toLong() / 60L
                                if (stepList.contains(data)) {
                                    var idx = 0
                                    var size = set.entryCount - 1

                                    while(idx < size) {
                                        val entry = set.getEntryForIndex(idx)
                                        val prevData = entry.x.toLong() / 60L

                                        if (stepList.contains(prevData)) {
                                            idx++
                                        } else {
                                            size--
                                            set.removeEntry(entry)
                                        }
                                    }
                                }
                            }

                            if (isUpdate) {
                                chart.notifyDataSetChanged()
                                chart.moveViewToX(x)
                            }
                        }
                    }

                    isOnCount++
                }
            }

            xMax++
        }
    }

    private fun createSet(
        value: Float,
        copyChannel: CopyChannel,
        createCount: Int,
        isAxis: Boolean = true
    ): LineDataSet {
        val name = if (createCount == 0) {
            copyChannel.name
        } else {
            copyChannel.name + "_${createCount + 1}"
        }

        val stringColor = copyChannel.lineColor

        val color = if (stringColor != null) {
            Color.parseColor("#${stringColor}")
        } else {
            DataLogApplication.getColor(R.color.colorAccent)
        }

        return createSet(name, 0.0F, value, color, isAxis)
    }

    private fun createSet(name: String, x: Float, y: Float, color: Int, isAxis: Boolean = true): LineDataSet {
        val values = ArrayList<Entry>()
        values.add(Entry(x, y))
        val set = LineDataSet(values, name)

        if (isAxis) {
            set.axisDependency = YAxis.AxisDependency.LEFT
        } else {
            set.axisDependency = YAxis.AxisDependency.RIGHT
        }

        set.lineWidth = 1.5F
        set.circleRadius = 2.25F
        set.fillAlpha = 65
        set.valueTextColor = DataLogApplication.getColor(R.color.font)
        set.valueTextSize = 9F
        set.setDrawValues(false)
        set.setDrawCircleHole(false)

        if (graphLineType == 0) {
            set.mode = LineDataSet.Mode.LINEAR
        } else {
            set.mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        set.color = color
        set.setCircleColor(color)
        set.fillColor = color
        set.highLightColor = color
        return set
    }

    fun setVisible(index: Int, isVisible: Boolean) {
        val data = chart.data
        val set = data.getDataSetByIndex(index)

        if (set != null) {
            set.isVisible = isVisible
        }

        refresh()
    }

    fun setVisible(index: Int, isVisible: Boolean, size: Int) {
        val data = chart.data

        var set = data.getDataSetByIndex(index)
        var count = 0
        while (set != null) {
            set.isVisible = isVisible
            set.xMax
            count++
            set = data.getDataSetByIndex(index + (size * count))
        }
    }
}