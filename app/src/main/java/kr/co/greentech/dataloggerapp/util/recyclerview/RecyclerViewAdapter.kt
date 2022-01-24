package kr.co.greentech.dataloggerapp.util.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentChangeAxisAndScale
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentStepSelect
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.BluetoothMeasureUIManager
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentStepSetting
import kr.co.greentech.dataloggerapp.realm.RealmChannel
import kr.co.greentech.dataloggerapp.util.extension.addSeparator
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.file.FileHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.enum.RecyclerViewType
import kr.co.greentech.dataloggerapp.util.recyclerview.enum.RecyclerViewType.*
import kr.co.greentech.dataloggerapp.util.recyclerview.helper.ItemTouchHelperCallback
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.AdjustCheckBox.AdjustCheckBoxHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.ChannelHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.OtherSettingHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.StepHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.edittext.EditTextHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.graphaxis.GraphAxisHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.SpinnerAndGraphAxisHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.checkbox.CheckBoxHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.edittext.EditTextItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel.ExcelHolder
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.excel.ExcelItem
import kr.co.greentech.dataloggerapp.util.recyclerview.viewholder.spinner.SpinnerItem
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapter(val fragment: Fragment, var list: ArrayList<Any>): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperCallback.ItemTouchHelperAdapter {

    companion object {
        fun settingDivider(context: Context, recyclerView: RecyclerView) {
            val dividerItemDecoration = DividerItemDecoration(
                context,
                LinearLayoutManager(context).orientation
            )
            dividerItemDecoration.setDrawable(
                DataLogApplication.getDrawable(R.drawable.divider)!!
            )
            recyclerView.addItemDecoration(dividerItemDecoration)
        }
    }

    private val type = RecyclerViewType.getType(fragment)

    override fun getItemViewType(position: Int): Int {
        return getType(position)
    }

    private fun getType(position: Int): Int {
        return when(type) {
            SPINNER_AND_GRAPH_AXIS -> {
                when (position) {
                    0, 1 -> 0
                    else -> 1
                }
            }

            OTHER_SETTING -> {
                when (list[position]) {
                    is SpinnerItem -> 0
                    is EditTextItem -> 1
                    else -> 2
                }
            }

            CHANNEL -> {
                when(position) {
                    0 -> 0
                    else -> 1
                }
            }

            XY_SPINNER_AND_GRAPH_AXIS -> {
                when (position) {
                    0 -> 0
                    else -> 1
                }
            }

            EXCEL -> {
                when(list[position]) {
                    is ExcelItem -> 0
                    else -> 1
                }
            }

            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return when(type) {
            FILE -> {
                val view = inflater.inflate(R.layout.list_item_file, parent, false)
                view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                FileHolder(context, view, this)
            }

            EDIT_TEXT -> {
                val view = inflater.inflate(R.layout.list_item_edit_text, parent, false)
                view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                EditTextHolder(fragment as FragmentStepSetting, view, this)
            }

            STEP -> {
                val view = inflater.inflate(R.layout.list_item_step_text, parent, false)
                StepHolder(fragment as DialogFragmentStepSelect, view, this)
            }

            GRAPH_AXIS -> {
                val view = inflater.inflate(R.layout.list_item_adjust_graph_axis, parent, false)
                GraphAxisHolder(context, view, this)
            }

            SPINNER -> {
                val view = inflater.inflate(R.layout.list_item_spinner, parent, false)
                view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                SpinnerHolder(context, view, this)
            }

            SPINNER_AND_GRAPH_AXIS, XY_SPINNER_AND_GRAPH_AXIS -> {
                val view = when(viewType) {
                    0 -> inflater.inflate(R.layout.list_item_spinner, parent, false)
                    else -> inflater.inflate(R.layout.list_item_adjust_graph_axis, parent, false)
                }
                SpinnerAndGraphAxisHolder(context, view, this)
            }

            OTHER_SETTING -> {
                val view = when(viewType) {
                    0 -> inflater.inflate(R.layout.list_item_spinner, parent, false)
                    1 -> inflater.inflate(R.layout.list_item_edit_text, parent, false)
                    else -> inflater.inflate(R.layout.list_item_text, parent, false)
                }

                view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                OtherSettingHolder(context, view, this)
            }

            CHANNEL -> {
                val view = when(viewType) {
                    0 -> inflater.inflate(R.layout.list_item_channel_header, parent, false)
                    else -> {
                        val view = inflater.inflate(R.layout.list_item_channel, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                        view
                    }
                }
                ChannelHolder(context, view, this)
            }

            CHECK_BOX -> {
                val copyChannelList = RealmChannel.getCopyChannelList()
                val view = inflater.inflate(R.layout.list_item_checkbox, parent, false)
                view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                CheckBoxHolder(view, this, copyChannelList)
            }

            EXCEL -> {
                val view = when(viewType) {
                    0 -> BluetoothMeasureUIManager.getLinearLayout(context)
                    else -> BluetoothMeasureUIManager.getStepHeaderLayout(context)
                }
                ExcelHolder(context, view, this)
            }

            ADJUST_CHECK_BOX -> {
                val copyChannelList = RealmChannel.getCopyChannelList()
                val view = inflater.inflate(R.layout.list_item_checkbox, parent, false)
                view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                AdjustCheckBoxHolder(view, this, copyChannelList)
            }
        }
    }

    fun changeAxisData() {
        if(fragment is DialogFragmentChangeAxisAndScale) {
            fragment.changeData()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(type) {
            FILE -> (holder as FileHolder).bind(position)
            EDIT_TEXT -> (holder as EditTextHolder).bind(position)
            STEP -> (holder as StepHolder).bind(position)
            GRAPH_AXIS -> (holder as GraphAxisHolder).bind(position)
            SPINNER -> (holder as SpinnerHolder).bind(position)
            SPINNER_AND_GRAPH_AXIS, XY_SPINNER_AND_GRAPH_AXIS -> (holder as SpinnerAndGraphAxisHolder).bind(position)
            OTHER_SETTING -> (holder as OtherSettingHolder).bind(position)
            CHANNEL -> (holder as ChannelHolder).bind(position)
            CHECK_BOX -> (holder as CheckBoxHolder).bind(position)
            EXCEL -> (holder as ExcelHolder).bind(position)
            ADJUST_CHECK_BOX -> (holder as AdjustCheckBoxHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        if(type == CHANNEL) {
            return list.size + 1
        }
        return list.size
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        when(type) {
            SPINNER, EDIT_TEXT -> {
                swapItems(fromPosition, toPosition)
            }
            else -> return
        }
    }

    private fun swapItems(positionFrom: Int, positionTo: Int) {
        val item = list.removeAt(positionFrom)
        list.add(positionTo, item)

        notifyItemMoved(positionFrom, positionTo)
        notifyItemChanged(positionFrom);
        notifyItemChanged(positionTo);
    }

    override fun onItemDismiss(position: Int) {
        when(type) {
            SPINNER, EDIT_TEXT -> {
                list.removeAt(position)
                notifyItemRemoved(position)
            }
            else -> return
        }
    }
}