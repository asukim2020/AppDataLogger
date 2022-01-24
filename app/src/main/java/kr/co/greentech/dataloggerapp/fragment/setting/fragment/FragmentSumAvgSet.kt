package kr.co.greentech.dataloggerapp.fragment.setting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.activity.ActivityStart
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.util.objects.PreferenceKey
import kr.co.greentech.dataloggerapp.util.objects.PreferenceManager
import kr.co.greentech.dataloggerapp.util.recyclerview.RecyclerViewAdapter
import kr.co.greentech.dataloggerapp.util.recyclerview.helper.ItemTouchHelperCallback
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem

class FragmentSumAvgSet: Fragment() {

    // true: sum, false: avg
    var sumAvgFlag: Boolean = false
    lateinit var adapter: RecyclerViewAdapter

    companion object {
        fun newInstance(sumAvgFlag: Boolean): FragmentSumAvgSet {
            val f = FragmentSumAvgSet()
            val args = Bundle()
            args.putBoolean("sumAvgFlag", sumAvgFlag)
            f.arguments = args
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        sumAvgFlag = args.getBoolean("sumAvgFlag", false)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview_and_floatting_button, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val floatingBtn = view.findViewById<FloatingActionButton>(R.id.btn_float)
        floatingBtn.visibility = View.VISIBLE

        val title = view.findViewById<TextView>(R.id.tv_header)
        if (sumAvgFlag) {
            title.text = getString(R.string.sum_header)
        } else {
            title.text = getString(R.string.avg_header)
        }

        val itemList = ArrayList<String>()
        val maxChannelCount = PreferenceManager.getInt(PreferenceKey.CHANNEL_COUNT)
        val channelList = RealmChannel.getCopyChannelList().subList(0, maxChannelCount)
        
        for(idx in channelList.indices) {
            itemList.add("CH${idx + 1}")
        }
        
        val list = ArrayList<SpinnerItem>()

        val data = if (sumAvgFlag) {
            PreferenceManager.getString(PreferenceKey.SUM_SET)
        } else {
            PreferenceManager.getString(PreferenceKey.AVERAGE_SET)
        }

        val dataList = ArrayList<String>(data.split(","))

        var i = 0
        while(true) {
            if (i < dataList.size) {
                if (dataList[i].toFloatOrNull() == null) {
                    dataList.removeAt(i)
                    continue
                }

                val value = dataList[i].toFloat()
                if (value < maxChannelCount) {
                    i++
                } else {
                    dataList.removeAt(i)
                }
            } else {
                break
            }
        }

        if (dataList.isEmpty() || data == PreferenceManager.DEFAULT_VALUE_STRING) {
            list.add(
                    SpinnerItem(
                            (list.size + 1).toString(),
                            0,
                            itemList
                    )
            )
        } else {
            for (item in dataList) {
                if (item.toIntOrNull() != null) {
                    list.add(
                            SpinnerItem(
                                    "",
                                    item.toInt(),
                                    itemList
                            )
                    )
                }
            }
        }

        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        floatingBtn.setOnClickListener {
            if(list.size + 1 > channelList.size) {
                val str = getString(R.string.max_count_limit_msg)
                val text = String.format(str, "${channelList.size}")
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            adapter.list.add(
                    SpinnerItem(
                            "",
                            0,
                            itemList
                    )
            )

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (sumAvgFlag) {
            (activity as ActivityStart?)!!.setTitle("Sum Set")
        } else {
            (activity as ActivityStart?)!!.setTitle("Average Set")
        }
    }

    override fun onStop() {
        super.onStop()

        val list = adapter.list as ArrayList<SpinnerItem>

        var str = ""
        for (item in list) {
            val positionString = ",${item.selectItemPosition}"
            if (!str.contains(positionString)) {
                str += positionString
            }
        }

        if (sumAvgFlag) {
            PreferenceManager.setString(PreferenceKey.SUM_SET, str)
        } else {
            PreferenceManager.setString(PreferenceKey.AVERAGE_SET, str)
        }
    }
}