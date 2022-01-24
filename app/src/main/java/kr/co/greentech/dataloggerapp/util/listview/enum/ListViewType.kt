package kr.co.greentech.dataloggerapp.util.listview.enum

import androidx.fragment.app.Fragment
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentFolderList
import kr.co.greentech.dataloggerapp.dialog.DialogFragmentStepSelect
import kr.co.greentech.dataloggerapp.fragment.channel.fragment.FragmentChannelList
import kr.co.greentech.dataloggerapp.fragment.datalog.fragment.FragmentDataLogSetting
import kr.co.greentech.dataloggerapp.fragment.menu.fragment.FragmentStartMenu
import kr.co.greentech.dataloggerapp.fragment.review.fragment.FragmentGraphReviewFileList
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentEqualIntervalSaveSetting
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentSaveSetting
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentStepSetting
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentStepSettingList
import kr.co.greentech.dataloggerapp.util.recyclerview.enum.RecyclerViewType

enum class ListViewType(val value: Int) {
    DATA_LOG(0),
    MENU(1),
    INTERVAL(2);

    companion object {
        fun getType(fragment: Fragment): ListViewType {
            return when (fragment) {
                is FragmentDataLogSetting -> DATA_LOG
                is FragmentStartMenu -> MENU
                is FragmentEqualIntervalSaveSetting -> INTERVAL
                is FragmentSaveSetting -> MENU
                else -> DATA_LOG
            }
        }
    }
}