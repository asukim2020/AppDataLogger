package kr.co.greentech.dataloggerapp.fragment.measure.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateMargins
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.channel.enums.UnitType
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.ChartUtil
import kr.co.greentech.dataloggerapp.util.TextUtil
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel.ExcelHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel.ExcelItem

object BluetoothMeasureUIManager {
    private const val channelSpace: Float = 150.0F
    private const val menuSpace: Float = 40.7F

    fun getLinearLayout(context: Context): LinearLayout {
        val csvLayout = LinearLayout(context)
        val height = 40.0F

        csvLayout.layoutParams = LinearLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                CalculatorUtil.dpToPx(height)
        )

        csvLayout.orientation = LinearLayout.HORIZONTAL

        return csvLayout
    }

    fun getCSVHeaderLayout(context: Context, excelItem: ExcelItem): LinearLayout {
        val csvLayout = LinearLayout(context)

        val width = 60.0F
        val height = 40.0F
        val padding = 2.0F
        val fontSize = 14.0F

        csvLayout.layoutParams = LinearLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                CalculatorUtil.dpToPx(height)
        )

        csvLayout.orientation = LinearLayout.HORIZONTAL
        var count = 0
        for (idx in 0 .. excelItem.dataList.size + 1) {
            val tv = TextView(context)
            tv.setBackgroundResource(R.drawable.excel_border)

            when(idx) {
                0 -> {
                    tv.text = ""
                    tv.setBackgroundResource(R.drawable.excel_border)
                }

                1 -> {
                    tv.text = "Time"
                    tv.setBackgroundResource(R.drawable.excel_header_border)
                }

                else -> {
                    if (excelItem.elapsedTime == "") {
                        tv.text = "CH${idx - 1}"
                    } else {
                        tv.text = excelItem.elapsedTime.split(",")[count]
                        count++
                    }
                    tv.setBackgroundResource(R.drawable.excel_header_border)
                }
            }

            tv.setTextColor(DataLogApplication.getColor(R.color.font))
            tv.textSize = fontSize
            tv.gravity = Gravity.CENTER

            val padding = CalculatorUtil.dpToPx(padding)
            tv.setPadding(padding, padding, 0, 0)
            tv.layoutParams = ConstraintLayout.LayoutParams(
                    when(idx) {
                        0 -> CalculatorUtil.dpToPx(height + 5.0F)
                        1 -> CalculatorUtil.dpToPx(width + 15.0F)
                        else -> CalculatorUtil.dpToPx(width)
                    },
                    LinearLayout.LayoutParams.MATCH_PARENT
            )

            csvLayout.addView(tv)
        }

        return csvLayout
    }

    fun getStepHeaderLayout(context: Context): View {
        val fontSize = 18.0F
        val height = 40.0F
        val tv = TextView(context)
        tv.tag = 0

        tv.setBackgroundColor(DataLogApplication.getColor(R.color.red))
        tv.setTextColor(DataLogApplication.getColor(R.color.background))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.textSize = fontSize
        tv.gravity = Gravity.CENTER

        tv.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                CalculatorUtil.dpToPx(height),
        )

        return tv
    }

    fun setCSVLayout(context: Context, csvLayout: LinearLayout, excelItem: ExcelItem, position: Int = -1, copyChannelList: List<CopyChannel>?) {

        val width = 60.0F
        val height = 40.0F
        val padding = 2.0F
        val fontSize = 14.0F

        if (csvLayout.childCount == 0) {

            for (idx in 0..excelItem.dataList.size + 1) {

                val tv = TextView(context)
                tv.tag = idx

                when(idx) {
                    0 -> tv.setBackgroundResource(R.drawable.excel_header_border)
                    else -> tv.setBackgroundResource(R.drawable.excel_border)
                }

                tv.setTextColor(DataLogApplication.getColor(R.color.font))
                tv.textSize = fontSize
                tv.gravity = Gravity.CENTER

                val padding = CalculatorUtil.dpToPx(padding)
                tv.setPadding(padding, padding, 0, 0)

                tv.layoutParams = LinearLayout.LayoutParams(
                        when(idx) {
                            0 -> CalculatorUtil.dpToPx(height + 5.0F)
                            1 -> CalculatorUtil.dpToPx(width + 15.0F)
                            else -> CalculatorUtil.dpToPx(width)
                        },
                        LinearLayout.LayoutParams.MATCH_PARENT
                )

                csvLayout.addView(tv)
            }
        }

        for (idx in 0..excelItem.dataList.size + 1) {
            val tv = csvLayout.findViewWithTag<TextView?>(idx) ?: continue
            when(idx) {
                0 -> tv.text = "${excelItem.number}"
                1 -> tv.text = excelItem.elapsedTime
                else -> {
                    tv.text = if (copyChannelList == null)
                        excelItem.dataList[idx - 2].toString()
                    else
                        TextUtil.floatToString(excelItem.dataList[idx - 2], copyChannelList[idx - 2].decPoint)

                    if (excelItem.readLine > 0) {
                        tv.setOnClickListener {
                            val map = MapEvent(HashMap())
                            map.map[ExcelHolder.toString()] = ExcelHolder.toString()
                            map.map["excelItem"] = excelItem
                            map.map["index"] = idx - 2
                            map.map["position"] = position
                            GlobalBus.getBus().post(map)
                        }
                    }
                }
            }
        }
    }

    private fun setTextViewName(
            tv: TextView,
            index: Int,
            copyChannelList: List<CopyChannel>,
            pageCount: Int,
            channelCount: Int
    ) {
        val index = index + (pageCount * channelCount)
        if (index < copyChannelList.size) {
            val channel = copyChannelList[index]
            tv.text = " ${channel.name}"
        }
    }

    private fun setTextViewUnit(
            context: Context,
            tv: TextView,
            index: Int,
            copyChannelList: List<CopyChannel>,
            pageCount: Int,
            channelCount: Int
    ) {
        val index = index + (pageCount * channelCount)
        if (index < copyChannelList.size) {
            val channel = copyChannelList[index]
            if (channel.unit != UnitType.DIRECT_INPUT.value) {
                val unit = UnitType.fromInt(channel.unit).getTitle(context)
                tv.text = "(${unit})"
            } else {
                tv.text = "(${channel.unitInput})"
            }
        }
    }

    private fun setDateText(
            tv: TextView,
            index: Int,
            copyChannelList: List<CopyChannel>,
            textList: ArrayList<TextView>,
            pageCount: Int,
            channelCount: Int
    ) {
        val index = index + (pageCount * channelCount)
        if (index < copyChannelList.size) {
            tv.text = ""
//            Log.d("Asu", "add tv index: $index")
            textList.add(tv)
        }
    }

    fun getTextChartMultiColumn(
            context: Context,
            cellCount: Int,
            copyChannelList: List<CopyChannel>,
            textList: ArrayList<TextView>,
            column: Int,
            pageCount: Int = 0,
            isAsync: Boolean = false
    ): View {
        val weightSum = 100.0F

        val linearLayout = LinearLayout(context)
        linearLayout.weightSum = weightSum
        linearLayout.orientation = LinearLayout.VERTICAL

        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        if (isAsync) {
            GlobalScope.async(Dispatchers.Main) {
                setSubTextChartMultiColumn(
                        context,
                        cellCount,
                        copyChannelList,
                        textList,
                        column,
                        pageCount,
                        linearLayout
                )
            }
        } else {
            setSubTextChartMultiColumn(
                    context,
                    cellCount,
                    copyChannelList,
                    textList,
                    column,
                    pageCount,
                    linearLayout
            )
        }

        return linearLayout
    }

    private fun setSubTextChartMultiColumn(
            context: Context,
            cellCount: Int, // 보여줄 채널 개수
            copyChannelList: List<CopyChannel>,
            textList: ArrayList<TextView>,
            column: Int, // 가로로 몇개 분할 할 것인지
            pageCount: Int = 0,
            linearLayout: LinearLayout
    ) {
        val copyTestList = ArrayList<TextView>()
        val inflater = LayoutInflater.from(context)
        val weightSum = 100.0F

        for(idx in 0 until (cellCount / column)) {
            val rootLayout = LinearLayout(context)
            rootLayout.weightSum = weightSum
            rootLayout.orientation = LinearLayout.HORIZONTAL

            rootLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    (weightSum / (cellCount / column))
            )

            for (i in 0 until column) {
                val v = inflater.inflate(R.layout.fragment_measure_text, null, false)
                val layout = v.findViewById<LinearLayout>(R.id.root_layout)

                val layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        weightSum / column
                )

                if (idx == 0 && i == 0 && pageCount == 0) {
                    v.setOnLongClickListener {
                        val bobTheBuilder = AlertDialog.Builder(context)
                        bobTheBuilder.setView(R.layout.dialog_rename).setTitle(context.getString(R.string.input_a_value))
                        val alert = bobTheBuilder.create()
                        alert.show()
                        val editText = alert.findViewById<EditText>(R.id.renameText)
                        editText.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
                        editText.hint = context.getString(R.string.edit_text_placeholder)

                        editText.setText("${copyChannelList[0].adjustA}")

                        val ok = alert.findViewById<Button>(R.id.ok)
                        val cancel = alert.findViewById<Button>(R.id.cancel)
                        ok.setOnClickListener {
                            val data = editText.text.toString().toFloatOrNull()
                            if (data != null) {
                                copyChannelList[0].adjustA = data
                            }
                            alert.cancel()
                        }
                        cancel.setOnClickListener { alert.cancel() }
                        true
                    }
                }

                val margin = CalculatorUtil.dpToPx(6.0F)
                layoutParams.updateMargins(margin, margin, margin, margin)

                layout.layoutParams = layoutParams

                // idx == 헹
                // column == 총 열 수
                //i == 열
//                val index = (idx * column) + i
                val index = idx + ((cellCount / column) * i)
//                Log.d("Asu", "index: $index")

                setTextViewName(v.findViewById(R.id.tv_title), index, copyChannelList, pageCount, cellCount)
                setTextViewUnit(context, v.findViewById(R.id.tv_unit), index, copyChannelList, pageCount, cellCount)
                setDateText(v.findViewById(R.id.tv), index, copyChannelList, copyTestList, pageCount, cellCount)

                rootLayout.addView(layout)
            }

            linearLayout.addView(rootLayout)
        }

        val row = (cellCount / column)
        for (i in 0 until cellCount) {
            val order = i / row
            val mod = i % row
            val index = (mod * column) + order
            textList.add(copyTestList[index])
        }
    }


    private fun getSubChannelView(
            orientation: Int,
            visibleChannelCount: Int,
            copyChannelList: List<CopyChannel>,
            context: Context,
            chartUtil: ChartUtil?,
            verticalTimeTextList: ArrayList<TextView>,
            horizonTimeTextList: ArrayList<TextView>,
            verticalTimeTitleTextList: ArrayList<TextView>,
            horizonTimeTitleTextList: ArrayList<TextView>,
            verticalTimeCheckboxList: ArrayList<CheckBox>,
            horizonTimeCheckboxList: ArrayList<CheckBox>,
            verticalTimeCheckboxLayoutList: ArrayList<ConstraintLayout>,
            horizonTimeCheckboxLayoutList: ArrayList<ConstraintLayout>,
            selectChannelIndexList: ArrayList<Int>
    ): LinearLayout {
        var column: Int = 1

        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> column = 2
            Configuration.ORIENTATION_LANDSCAPE -> { }
        }

        if (visibleChannelCount == 1) {
            column = 1
        }

        val inflater = LayoutInflater.from(context)
        val weightSum = 100.0F

        val linearLayout = LinearLayout(context)
        linearLayout.weightSum = weightSum
        linearLayout.orientation = LinearLayout.VERTICAL

        linearLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        var isOnCount = 0
        for(idx in 0 until (visibleChannelCount / column)) {
            val rootLayout = LinearLayout(context)
            rootLayout.weightSum = weightSum
            rootLayout.orientation = LinearLayout.HORIZONTAL

            rootLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    (weightSum / (visibleChannelCount / column))
            )

            for (i in 0 until column) {
                val view = inflater.inflate(R.layout.list_item_checkbox_and_title, null, false)
                val constraintLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)

                val layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        weightSum / column
                )

                val margin = CalculatorUtil.dpToPx(3.0F)
                layoutParams.updateMargins(margin, margin, margin, margin)

                constraintLayout.layoutParams = layoutParams

                val checkbox = view.findViewById<CheckBox>(R.id.checkbox)
                val title = view.findViewById<TextView>(R.id.title)
                val subTitle = view.findViewById<TextView>(R.id.subtitle)

                val index = (idx * column) + i

                when (orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        verticalTimeCheckboxLayoutList.add(constraintLayout)
                        verticalTimeCheckboxList.add(checkbox)
                        verticalTimeTitleTextList.add(title)
                        verticalTimeTextList.add(subTitle)
                    }
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        horizonTimeCheckboxLayoutList.add(constraintLayout)
                        horizonTimeCheckboxList.add(checkbox)
                        horizonTimeTitleTextList.add(title)
                        horizonTimeTextList.add(subTitle)
                    }
                }

                if (index < selectChannelIndexList.size) {
                    val selectIndex = selectChannelIndexList[index]
                    title.text = "${copyChannelList[selectIndex].name}:"

                    if (copyChannelList[selectIndex].isOn && index < visibleChannelCount) {
                        val idx = isOnCount
                        constraintLayout.setOnClickListener {
                            checkbox.isChecked = !checkbox.isChecked
                            chartUtil?.setVisible(idx, checkbox.isChecked)
                        }

                        checkbox.setOnClickListener {
                            chartUtil?.setVisible(idx, checkbox.isChecked)
                        }
                        isOnCount++
                    }
                } else {
                    title.text = "-"
                    subTitle.text = "-"
                }

                rootLayout.addView(constraintLayout)
            }
            linearLayout.addView(rootLayout)
        }

        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> column = 2
            Configuration.ORIENTATION_LANDSCAPE -> { }
        }

        if (
                orientation == Configuration.ORIENTATION_PORTRAIT
                && visibleChannelCount == 1
        ) {
            val padding = CalculatorUtil.dpToPx(20.0F)
            linearLayout.setPadding(padding, padding, padding, padding)
        }

        return linearLayout
    }

    fun getXYTextLayout(context: Context): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.else_xy_scale, null, false)
    }

    fun updateGraphOrientationUI(
            orientation: Int,
            visibleChannelCount: Int,
            context: Context,
            copyChannelList: List<CopyChannel>,
            chartUtil: ChartUtil?,
            verticalTimeTextList: ArrayList<TextView>,
            horizonTimeTextList: ArrayList<TextView>,
            verticalTimeTitleTextList: ArrayList<TextView>,
            horizonTimeTitleTextList: ArrayList<TextView>,
            verticalTimeCheckboxList: ArrayList<CheckBox>,
            horizonTimeCheckboxList: ArrayList<CheckBox>,
            verticalTimeCheckboxLayoutList: ArrayList<ConstraintLayout>,
            horizonTimeCheckboxLayoutList: ArrayList<ConstraintLayout>,
            selectChannelIndexList: ArrayList<Int>,
            rootLayout: ConstraintLayout,
            chartLayout: ConstraintLayout,
            textLayout: FrameLayout,
            excelLayout: ConstraintLayout,
            verticalTimeTextLayout: LinearLayout?,
            horizonTimeTextLayout: LinearLayout?,
            menuPortrait: ConstraintLayout,
            menuLandscape: ConstraintLayout,
            bottomSeparator: View,
            endSeparator: View,
            xyTextLayout: View
    ): LinearLayout? {
        val chartParam = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        if(visibleChannelCount == 0) {
            chartParam.marginEnd = CalculatorUtil.dpToPx(0.0f)
            chartParam.bottomMargin = CalculatorUtil.dpToPx(0.0f)
            chartLayout.layoutParams = chartParam
            return null
        }

        val set = ConstraintSet()
        set.clone(rootLayout)

        var returnLayout: LinearLayout? = null

        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if(verticalTimeTextLayout == null) {
                    val linearLayout = getSubChannelView(
                        orientation,
                        visibleChannelCount,
                        copyChannelList,
                        context,
                        chartUtil,
                        verticalTimeTextList,
                        horizonTimeTextList,
                            verticalTimeTitleTextList,
                            horizonTimeTitleTextList,
                        verticalTimeCheckboxList,
                        horizonTimeCheckboxList,
                            verticalTimeCheckboxLayoutList,
                            horizonTimeCheckboxLayoutList,
                            selectChannelIndexList
                    )

                    linearLayout.id = View.generateViewId()
                    returnLayout = linearLayout
                    rootLayout.addView(linearLayout)

                    set.connect(linearLayout.id, ConstraintSet.TOP, chartLayout.id, ConstraintSet.BOTTOM)
                    set.connect(linearLayout.id, ConstraintSet.BOTTOM, menuPortrait.id, ConstraintSet.TOP)
                    set.connect(linearLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    set.connect(linearLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                }

                set.connect(xyTextLayout.id, ConstraintSet.TOP, chartLayout.id, ConstraintSet.BOTTOM)
                set.connect(xyTextLayout.id, ConstraintSet.BOTTOM, menuPortrait.id, ConstraintSet.TOP)
                set.connect(xyTextLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                set.connect(xyTextLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

                set.connect(textLayout.id, ConstraintSet.BOTTOM, menuPortrait.id, ConstraintSet.TOP)
                set.connect(textLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

                set.connect(excelLayout.id, ConstraintSet.BOTTOM, menuPortrait.id, ConstraintSet.TOP)
                set.connect(excelLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                set.applyTo(rootLayout)

                chartParam.marginEnd = CalculatorUtil.dpToPx(0.0F)
                chartParam.bottomMargin = CalculatorUtil.dpToPx(this.channelSpace + menuSpace)
                chartLayout.layoutParams = chartParam
                menuPortrait.visibility = View.VISIBLE
                menuLandscape.visibility = View.INVISIBLE
                bottomSeparator.visibility = View.VISIBLE
                endSeparator.visibility = View.INVISIBLE

                if(horizonTimeTextLayout != null) {
                    for(idx in horizonTimeCheckboxList.indices) {
                        if(horizonTimeCheckboxList[idx].isChecked != verticalTimeCheckboxList[idx].isChecked) {
                            verticalTimeCheckboxList[idx].isChecked = horizonTimeCheckboxList[idx].isChecked
                        }
                    }
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                if(horizonTimeTextLayout == null) {
                    val linearLayout = getSubChannelView(
                        orientation,
                        visibleChannelCount,
                        copyChannelList,
                        context,
                        chartUtil,
                        verticalTimeTextList,
                        horizonTimeTextList,
                            verticalTimeTitleTextList,
                            horizonTimeTitleTextList,
                        verticalTimeCheckboxList,
                        horizonTimeCheckboxList,
                            verticalTimeCheckboxLayoutList,
                            horizonTimeCheckboxLayoutList,
                            selectChannelIndexList
                    )

                    linearLayout.id = View.generateViewId()
                    returnLayout = linearLayout
                    rootLayout.addView(linearLayout)

                    set.connect(linearLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                    set.connect(linearLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                    set.connect(linearLayout.id, ConstraintSet.START, chartLayout.id, ConstraintSet.END)
                    set.connect(linearLayout.id, ConstraintSet.END, menuLandscape.id, ConstraintSet.START)
                }

                set.connect(xyTextLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                set.connect(xyTextLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                set.connect(xyTextLayout.id, ConstraintSet.START, chartLayout.id, ConstraintSet.END)
                set.connect(xyTextLayout.id, ConstraintSet.END, menuLandscape.id, ConstraintSet.START)

                set.connect(textLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                set.connect(textLayout.id, ConstraintSet.END, menuLandscape.id, ConstraintSet.START)

                set.connect(excelLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                set.connect(excelLayout.id, ConstraintSet.END, menuLandscape.id, ConstraintSet.START)

                set.applyTo(rootLayout)

                chartParam.marginEnd = CalculatorUtil.dpToPx(this.channelSpace + menuSpace)
                chartParam.bottomMargin = CalculatorUtil.dpToPx(0.0F)
                chartLayout.layoutParams = chartParam
                menuPortrait.visibility = View.INVISIBLE
                menuLandscape.visibility = View.VISIBLE
                bottomSeparator.visibility = View.INVISIBLE
                endSeparator.visibility = View.VISIBLE

                if(verticalTimeTextLayout != null) {
                    for(idx in verticalTimeCheckboxList.indices) {
                        if(verticalTimeCheckboxList[idx].isChecked != horizonTimeCheckboxList[idx].isChecked) {
                            horizonTimeCheckboxList[idx].isChecked = verticalTimeCheckboxList[idx].isChecked
                        }
                    }
                }
            }
        }

        return returnLayout
    }
}