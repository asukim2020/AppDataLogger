package kr.co.greentech.dataloggerapp.util.recyclerview.enum

import androidx.fragment.app.Fragment
import kr.co.greentech.dataloggerapp.dialog.*
import kr.co.greentech.dataloggerapp.fragment.channel.fragment.FragmentChannelList
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.FragmentBluetoothMeasure
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.FragmentMeasure
import kr.co.greentech.dataloggerapp.fragment.review.fragment.FragmentGraphReview
import kr.co.greentech.dataloggerapp.fragment.review.fragment.FragmentGraphReviewFileList
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentStepSetting
import kr.co.greentech.dataloggerapp.fragment.setting.fragment.FragmentOtherSetting
import kr.co.greentech.dataloggerapp.fragment.setting.fragment.FragmentSumAvgSet

enum class RecyclerViewType(val value: Int) {

    FILE(0),
    EDIT_TEXT(1),
    STEP(2),
    GRAPH_AXIS(3),
    SPINNER(4),
    SPINNER_AND_GRAPH_AXIS(5),
    OTHER_SETTING(6),
    CHANNEL(7),
    XY_SPINNER_AND_GRAPH_AXIS(7),
    CHECK_BOX(8),
    EXCEL(9),
    ADJUST_CHECK_BOX(10);

    companion object {
        fun getType(fragment: Fragment): RecyclerViewType {
            return when (fragment) {
                is FragmentGraphReviewFileList -> FILE
                is DialogFragmentFolderList -> FILE
                is FragmentStepSetting -> EDIT_TEXT
                is DialogFragmentStepSelect -> STEP
                is DialogFragmentChangeGraphXAxis -> GRAPH_AXIS
                is FragmentSumAvgSet -> SPINNER
                is DialogFragmentChangeAxisAndScale -> SPINNER_AND_GRAPH_AXIS
                is FragmentOtherSetting -> OTHER_SETTING
                is FragmentChannelList -> CHANNEL
                is DialogFragmentStepXYSetting -> XY_SPINNER_AND_GRAPH_AXIS
                is DialogFragmentChannelAllSetting -> CHECK_BOX
                is FragmentMeasure -> EXCEL
                is FragmentGraphReview -> EXCEL
                is DialogFragmentAdjustZero -> ADJUST_CHECK_BOX
                else -> FILE
            }
        }
    }
}