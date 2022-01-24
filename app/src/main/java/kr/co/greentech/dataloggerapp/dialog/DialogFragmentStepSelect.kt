package kr.co.greentech.dataloggerapp.dialog

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.realm.RealmSaveSetting
import kr.co.greentech.dataloggerapp.realm.RealmStepSetting
import kr.co.greentech.dataloggerapp.util.AdapterUtil
import kr.co.greentech.dataloggerapp.util.objects.CalculatorUtil
import kr.co.greentech.dataloggerapp.util.eventbus.GlobalBus
import kr.co.greentech.dataloggerapp.util.eventbus.MapEvent
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kotlin.properties.Delegates

class DialogFragmentStepSelect: DialogFragment() {

    companion object {
        fun newInstance(startFlag: Boolean): DialogFragmentStepSelect {
            val f = DialogFragmentStepSelect()
            val args = Bundle()
            args.putBoolean("startFlag", startFlag)
            f.arguments = args
            return f
        }
    }

    lateinit var adapter: RecyclerViewAdapter
    var startFlag by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        startFlag = args!!.getBoolean("startFlag")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_step_select, container, false)
        isCancelable = false

        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val ok = view.findViewById<Button>(R.id.ok)
        val cancel = view.findViewById<Button>(R.id.cancel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val tvEdit = view.findViewById<EditText>(R.id.tv_edit)

        RecyclerViewAdapter.settingDivider(requireContext(), recyclerView)

        adapter = RecyclerViewAdapter(this, ArrayList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val item = RealmSaveSetting.select()!!

        val list = ArrayList<RealmStepSetting>()
        for (item in item.stepSettingList) {
            list.add(item)
        }
        list.sortBy { it.key }
        var stepType = 0
        val itemString = ArrayList<String>()
        for (idx in list.indices) {
            if (item.selectedStep == list[idx].key) {
                stepType = idx
            }

            val str = getString(R.string.step_number)
            val text = String.format(str, "${idx + 1}")
            itemString.add(text)
        }

        val spinnerAdapter: ArrayAdapter<String> = AdapterUtil.getSpinnerAdapter(
            requireContext(),
                spinner,
                itemString
        )

        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, idx: Int, id: Long) {
                item.updateSelectedStep(list[idx].key)
                val stepList = ArrayList<Long>()

                for (item in item.stepSettingList) {
                    if(item.key == list[idx].key) {
                        for(step in item.stepList) {
                            stepList.add(step)
                        }
                        adapter.list = stepList as ArrayList<Any>
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
        spinner.setSelection(stepType)

        ok.setOnClickListener {
            if (tvEdit.text.toString() == "") {
                AlertUtil.alert(requireContext(), getString(R.string.input_step_p_msg))
                return@setOnClickListener
            }

            val map = MapEvent(HashMap())
            map.map[DialogFragmentStepSelect.toString()] = DialogFragmentStepSelect.toString()
            map.map["startFlag"] = startFlag
            map.map["STEP P"] = tvEdit.text.toString()
            GlobalBus.getBus().post(map)

            if (!startFlag) {
                DialogFragmentFileSave.newInstance().show(fragmentManager!!, "DialogFragmentFileSave")
            }

            dismiss()
        }
        cancel.setOnClickListener { dismiss() }

        return view
    }

    override fun onResume() {
        super.onResume()

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = CalculatorUtil.dpToPx(220.0F)
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

}