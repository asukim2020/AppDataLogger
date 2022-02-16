package kr.co.greentech.dataloggerapp.fragment.measure.page

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.BluetoothMeasureUIManager
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.realm.copy.CopyChannel

class PagerRecyclerAdapter(var maxChannelCount: Int, var cellCount: Int, private val textList: ArrayList<TextView>, private val copyChannelList: List<CopyChannel>) : RecyclerView.Adapter<PagerViewHolder>() {

    private lateinit var secondPageHolder: PagerViewHolder
    private lateinit var thirdPageHolder: PagerViewHolder

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PagerViewHolder {
        val context = parent.context
        when(position) {
            0 -> {
                val holder = getViewHolder(context, position)
                secondPageHolder = getViewHolder(context, position + 1)
                thirdPageHolder = getViewHolder(context, position + 2)

                return holder
            }

            1 -> {
                return secondPageHolder
            }

            2 -> {
                return thirdPageHolder
            }

            else -> {
                return getViewHolder(context, position)
            }
        }
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int =
        if ((maxChannelCount / cellCount) == 0)
            1
        else
            (maxChannelCount / cellCount)

    private fun getViewHolder(context: Context, position: Int): PagerViewHolder {
        val isAsync = (position == 1 || position == 2)

        return PagerViewHolder(
            BluetoothMeasureUIManager.getTextChartMultiColumn(
                context,
                cellCount,
                copyChannelList,
                textList,
                when {
                    8 < cellCount -> 4
                    cellCount in 5..15 -> 2
                    else -> 1
                },
                position,
                isAsync
            )
        )
    }
}
