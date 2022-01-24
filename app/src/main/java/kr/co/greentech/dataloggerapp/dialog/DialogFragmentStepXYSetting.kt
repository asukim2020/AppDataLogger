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
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.graphaxis.GraphAxisItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.enum.XYAxisType

class DialogFragmentStepXYSetting: DialogFragment() {
    lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    companion object {
        fun newInstance(): DialogFragmentStepXYSetting {
            return DialogFragmentStepXYSetting()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()

        val view = inflater.inflate(R.layout.dailog_fragment_recycler_ok_cancel, container, false)
        isCancelable = false

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        RecyclerViewAdapter.settingDivider(context, recyclerView)

        val set = RealmGraphScaleSet.select()!!

        var firstAxisType = PreferenceManager.getInt(PreferenceKey.STEP_XY_Y_AXIS)

        val xyItem = ArrayList<String>()
        var count = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)

        if (count < firstAxisType + 1) {
            count = firstAxisType + 1
        }

        for (i in 0 until count) {
            xyItem.add("CH${i+1}")
        }

        val list = ArrayList<Any>()
        list.add(
                SpinnerItem(
                        XYAxisType.fromInt(1).getTitle(),
                        firstAxisType,
                        xyItem
                )
        )

        val firstAxis = set.xyXAxis[0]!!
        list.add(
                GraphAxisItem(
                        "X axis auto scale",
                        firstAxis.min.toString(),
                        firstAxis.max.toString(),
                        firstAxis.isOn
                )
        )

        val secondAxis = set.xyYAxis[0]!!
        list.add(
                GraphAxisItem(
                        "Y axis auto scale",
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
            val copyFirstAxis = (list[1] as GraphAxisItem)
            val copySecondAxis = (list[2] as GraphAxisItem)

            if (
                    copyFirstAxis.minEditText.toFloatOrNull() == null
                    || copyFirstAxis.maxEditText.toFloatOrNull() == null
                    || copySecondAxis.minEditText.toFloatOrNull() == null
                    || copySecondAxis.maxEditText.toFloatOrNull() == null
            ) {
                AlertUtil.alert(context, getString(R.string.all_input_mas))
                return@setOnClickListener
            } else if (copyFirstAxis.minEditText.toFloat() > copyFirstAxis.maxEditText.toFloat()
                    && copySecondAxis.minEditText.toFloat() > copySecondAxis.maxEditText.toFloat()) {
                AlertUtil.alert(context, getString(R.string.min_max_exception_msg))
            }

            val list = adapter.list

            firstAxisType = (list[0] as SpinnerItem).selectItemPosition

            if(!copyFirstAxis.isOn) {
                firstAxis.update(
                        copyFirstAxis.isOn,
                        copyFirstAxis.minEditText.toFloat(),
                        copyFirstAxis.maxEditText.toFloat()
                )
            } else {
                firstAxis.updateIsOn(copyFirstAxis.isOn)
            }

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
            event.map[DialogFragmentStepXYSetting.toString()] = DialogFragmentStepXYSetting.toString()
            event.map["firstAxisType"] = firstAxisType
            event.map["firstAxisScale"] = firstAxisScale
            event.map["secondAxisScale"] = secondAxisScale

            GlobalBus.getBus().post(event)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()

        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = CalculatorUtil.dpToPx(300.0F)

        val maxHeight = CalculatorUtil.dpToPx(332.0F)

        if((size.y * 0.9) > maxHeight) {
            params?.height = maxHeight
        } else {
            params?.height = (size.y * 0.9).toInt()
        }
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}