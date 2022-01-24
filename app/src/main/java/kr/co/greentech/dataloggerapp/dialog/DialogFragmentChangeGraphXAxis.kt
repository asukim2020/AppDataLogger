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
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.graphaxis.GraphAxisItem

class DialogFragmentChangeGraphXAxis: DialogFragment() {

    lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    companion object {
        fun newInstance(): DialogFragmentChangeGraphXAxis {
            return DialogFragmentChangeGraphXAxis()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dailog_fragment_recycler_ok_cancel, container, false)
        isCancelable = false

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        RecyclerViewAdapter.settingDivider(requireContext(), recyclerView)

        val set = RealmGraphScaleSet.select()!!

        val list = ArrayList<GraphAxisItem>()

        val x = set.timeXAxis[0]!!.getCopy()
        list.add(
                GraphAxisItem(
                        "X axis auto scale",
                        x.min.toString(),
                        x.max.toString(),
                        x.isOn
                )
        )

        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val okButton = view.findViewById<Button>(R.id.ok)
        val cancelButton = view.findViewById<Button>(R.id.cancel)

        okButton.setOnClickListener {
            if (
                    list[0].minEditText.toFloatOrNull() == null
                    || list[0].maxEditText.toFloatOrNull() == null
            ) {
                        AlertUtil.alert(requireContext(), getString(R.string.all_input_mas))
                return@setOnClickListener
            } else if (list[0].minEditText.toFloat() > list[0].maxEditText.toFloat()) {
                AlertUtil.alert(requireContext(), getString(R.string.min_max_exception_msg))
            }

            val list = adapter.list as ArrayList<GraphAxisItem>
            val x = set.timeXAxis[0]!!
            if(!list[0].isOn) {
                x.update(
                        list[0].isOn,
                        list[0].minEditText.toFloat(),
                        list[0].maxEditText.toFloat()
                )
            } else {
                x.updateIsOn(list[0].isOn)
            }

            val xAxis = CopyGraphScale(list[0].isOn, list[0].minEditText.toFloat(), list[0].maxEditText.toFloat())
            val event = MapEvent(HashMap())
            event.map[DialogFragmentChangeGraphXAxis.toString()] = DialogFragmentChangeGraphXAxis.toString()
            event.map["xAxis"] = xAxis

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

        val maxHeight = CalculatorUtil.dpToPx(172.0F)

        if((size.y * 0.9) > maxHeight) {
            params?.height = maxHeight
        } else {
            params?.height = (size.y * 0.9).toInt()
        }
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}