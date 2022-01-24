package kr.co.greentech.dataloggerapp.fragment.savesetting.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.realm.RealmStepSetting
import kr.co.greentech.dataloggerapp.util.objects.AlertUtil
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.helper.ItemTouchHelperCallback
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.edittext.EditTextItem


class FragmentStepSetting: Fragment() {

    companion object {
        fun newInstance(stepKey: Long): FragmentStepSetting {
            val f = FragmentStepSetting()
            val args = Bundle()
            args.putLong("key", stepKey)
            f.arguments = args
            return f
        }
    }

    lateinit var adapter: RecyclerViewAdapter
    lateinit var item: RealmStepSetting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val args = arguments
        val key = args?.getLong("key")
        item = RealmStepSetting.select().first { it.key == key }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview_and_floatting_button, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val floatingBtn = view.findViewById<FloatingActionButton>(R.id.btn_float)
        val title = view.findViewById<TextView>(R.id.tv_header)

        title.text = getString(R.string.step_header)

        floatingBtn.visibility = View.VISIBLE

        val list = ArrayList<EditTextItem>()

        if(item.stepList.size > 0 ) {
            for (step in item.stepList) {
                list.add(EditTextItem(step.toString()))
            }
        } else {
            list.add(EditTextItem(""))
        }

        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        floatingBtn.setOnClickListener {
            adapter.list.add(EditTextItem(""))
            val position = adapter.list.size - 1
            adapter.notifyItemInserted(position)
            recyclerView.post {
                recyclerView.scrollToPosition(adapter.list.size - 1)
            }
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            android.R.id.home -> {
                checkData()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as ActivityStart?)!!.setTitle(getString(R.string.save_step_setting))

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.action == KeyEvent.ACTION_UP) {
                checkData()
                true
            } else false
        }
    }

    fun checkData() {
        if (checkStepData()) {
            fragmentManager?.popBackStack()
        } else {
            checkInvalidAlert()
        }
    }

    private fun checkStepData(): Boolean {
        val list = adapter.list as ArrayList<EditTextItem>
        var max = -1L
        for (step in list) {
            if(step.editText.toLongOrNull() != null) {
                val data = step.editText.toLong()
                if(data >= 0 ) {
                    if(max < data) {
                        max = data
                    } else {
                        return false
                    }
                }
            }
        }

        return true
    }

    private fun checkInvalidAlert() {
        AlertUtil.alert(requireContext(), getString(R.string.save_step_exception_msg))
    }

    override fun onStop() {
        super.onStop()
        updateStepList()
    }

    private fun updateStepList() {
        val list = ArrayList<Long>()
        for(step in adapter.list as ArrayList<EditTextItem>) {
            if(step.editText.toLongOrNull() != null) {
                val data = step.editText.toLong()
                if(data >= 0 ) {
                    list.add(data)
                }
            }
        }

        item.updateStepList(list)
    }
}

