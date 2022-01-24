package kr.co.greentech.dataloggerapp.dialog

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.realm.RealmGraphScaleSet
import kr.co.greentech.dataloggerapp.realm.copy.CopyGraphScale
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.graphaxis.GraphAxisItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.enum.XYAxisType
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.enum.YAxisType

class DialogFragmentChangeAxisAndScale: DialogFragment() {

    lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    var isTimeChart = true

    companion object {
        fun newInstance(isTimeChart: Boolean): DialogFragmentChangeAxisAndScale {
            val f = DialogFragmentChangeAxisAndScale()
            val args = Bundle()
            args.putBoolean("isTimeChart", isTimeChart)
            f.arguments = args
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        isTimeChart = args.getBoolean("isTimeChart", true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dailog_fragment_recycler_ok_cancel, container, false)
        isCancelable = false

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        RecyclerViewAdapter.settingDivider(requireContext(), recyclerView)

        val set = RealmGraphScaleSet.select()!!

        var firstAxisType = if (isTimeChart) set.timeLeftYAxisType else set.xyXAxisType
        var secondAxisType = if (isTimeChart) set.timeRightYAxisType else set.xyYAxisType

        val xyItem = ArrayList<String>()
        var count = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)

        if (count < firstAxisType + 1) {
            count = firstAxisType + 1
        }

        if (count < secondAxisType + 1) {
            count = secondAxisType + 1
        }

        for (i in 0 until count) {
            xyItem.add("CH${i+1}")
        }

        val list = ArrayList<Any>()
        list.add(
                SpinnerItem(
                        if (isTimeChart) YAxisType.fromInt(0).getTitle() else XYAxisType.fromInt(0).getTitle(),
                        firstAxisType,
                        if (isTimeChart) YAxisType.getItems() else xyItem
                )
        )

        list.add(
                SpinnerItem(
                        if (isTimeChart) YAxisType.fromInt(1).getTitle() else XYAxisType.fromInt(1).getTitle(),
                        secondAxisType,
                        if (isTimeChart) YAxisType.getItems() else xyItem
                )
        )

        val firstAxis = if (isTimeChart) set.timeYAxis[firstAxisType]!!else set.xyXAxis[0]!!
        list.add(
                GraphAxisItem(
                        if (isTimeChart) "Left Y axis auto scale" else "X axis auto scale",
                        firstAxis.min.toString(),
                        firstAxis.max.toString(),
                        firstAxis.isOn
                )
        )

        val secondAxis = if (isTimeChart) set.timeYAxis[secondAxisType]!! else set.xyYAxis[0]!!
        list.add(
                GraphAxisItem(
                        if (isTimeChart) "Right Y axis auto scale" else "Y axis auto scale",
                        secondAxis.min.toString(),
                        secondAxis.max.toString(),
                        secondAxis.isOn
                )
        )

        adapter = RecyclerViewAdapter(this, list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val okButton = view.findViewById<Button>(R.id.ok)
        val cancelButton = view.findViewById<Button>(R.id.cancel)

        okButton.setOnClickListener {
            val copyFirstAxis = (list[2] as GraphAxisItem)
            val copySecondAxis = (list[3] as GraphAxisItem)

            if (
                    copyFirstAxis.minEditText.toFloatOrNull() == null
                    || copyFirstAxis.maxEditText.toFloatOrNull() == null
                    || copySecondAxis.minEditText.toFloatOrNull() == null
                    || copySecondAxis.maxEditText.toFloatOrNull() == null
            ) {
                AlertUtil.alert(requireContext(), getString(R.string.all_input_mas))
                return@setOnClickListener
            } else if (copyFirstAxis.minEditText.toFloat() > copyFirstAxis.maxEditText.toFloat()
                    && copySecondAxis.minEditText.toFloat() > copySecondAxis.maxEditText.toFloat()) {
                AlertUtil.alert(requireContext(), getString(R.string.min_max_exception_msg))
            }

            val list = adapter.list

            firstAxisType = (list[0] as SpinnerItem).selectItemPosition
            secondAxisType = (list[1] as SpinnerItem).selectItemPosition

            if (isTimeChart) {
                set.updateTimeChartAxisType(firstAxisType, secondAxisType)
            } else {
                set.updateXYChartAxisType(firstAxisType, secondAxisType)
            }

            val firstAxis = if (isTimeChart) set.timeYAxis[firstAxisType]!!else set.xyXAxis[0]!!
            if(!copyFirstAxis.isOn) {
                firstAxis.update(
                        copyFirstAxis.isOn,
                        copyFirstAxis.minEditText.toFloat(),
                        copyFirstAxis.maxEditText.toFloat()
                )
            } else {
                firstAxis.updateIsOn(copyFirstAxis.isOn)
            }

            val secondAxis = if (isTimeChart) set.timeYAxis[secondAxisType]!! else set.xyYAxis[0]!!
            if(!copySecondAxis.isOn) {
                secondAxis.update(
                        copySecondAxis.isOn,
                        copySecondAxis.minEditText.toFloat(),
                        copySecondAxis.maxEditText.toFloat()
                )
            } else {
                secondAxis.updateIsOn(copySecondAxis.isOn)
            }

            val firstAxisScale = CopyGraphScale(copyFirstAxis.isOn, copyFirstAxis.minEditText.toFloat(), copyFirstAxis.maxEditText.toFloat())
            val secondAxisScale = CopyGraphScale(copySecondAxis.isOn, copySecondAxis.minEditText.toFloat(), copySecondAxis.maxEditText.toFloat())

            val event = MapEvent(HashMap())
            event.map[DialogFragmentChangeAxisAndScale.toString()] = DialogFragmentChangeAxisAndScale.toString()
            event.map["firstAxisType"] = firstAxisType
            event.map["secondAxisType"] = secondAxisType
            event.map["firstAxisScale"] = firstAxisScale
            event.map["secondAxisScale"] = secondAxisScale
            event.map["isTimeChart"] = isTimeChart

            GlobalBus.getBus().post(event)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    fun changeData() {
        if (!isTimeChart) return

        val set = RealmGraphScaleSet.select()!!

        val firstAxisType = (adapter.list[0] as SpinnerItem).selectItemPosition
        val secondAxisType = (adapter.list[1] as SpinnerItem).selectItemPosition

        val firstAxisScale = set.timeYAxis[firstAxisType]!!.getCopy()
        adapter.list[2] = GraphAxisItem(
                if (isTimeChart) "Left Y axis auto scale" else "X axis auto scale",
                firstAxisScale.min.toString(),
                firstAxisScale.max.toString(),
                firstAxisScale.isOn
        )

        val secondAxisScale = set.timeYAxis[secondAxisType]!!.getCopy()
        adapter.list[3] = GraphAxisItem(
                if (isTimeChart) "Right Y axis auto scale" else "Y axis auto scale",
                secondAxisScale.min.toString(),
                secondAxisScale.max.toString(),
                secondAxisScale.isOn
        )

        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()

        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = CalculatorUtil.dpToPx(300.0F)

        val maxHeight = CalculatorUtil.dpToPx(392.0F)

        if((size.y * 0.9) > maxHeight) {
            params?.height = maxHeight
        } else {
            params?.height = (size.y * 0.9).toInt()
        }
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}