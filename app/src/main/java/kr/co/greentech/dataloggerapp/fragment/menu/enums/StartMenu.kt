package kr.co.greentech.dataloggerapp.fragment.menu.enums

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import kr.co.greentech.dataloggerapp.R
import kr.co.greentech.dataloggerapp.application.DataLogApplication
import kr.co.greentech.dataloggerapp.fragment.measure.fragment.FragmentBluetoothDevices
import kr.co.greentech.dataloggerapp.fragment.channel.fragment.FragmentChannelList
import kr.co.greentech.dataloggerapp.fragment.datalog.fragment.FragmentDataLogSetting
import kr.co.greentech.dataloggerapp.fragment.review.fragment.FragmentGraphReviewFileList
import kr.co.greentech.dataloggerapp.fragment.savesetting.fragment.FragmentSaveSetting
import kr.co.greentech.dataloggerapp.fragment.setting.fragment.FragmentOtherSetting

enum class StartMenu(val value: Int) {
    MEASURE(0),
    GRAPH_REVIEW(1),
    CHANNEL_SETTING(2),
    DATA_LOG_SETTING(3),
    SAVE_SETTING(4),
    SETTING(5);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getIcon(): Drawable? {
        return when (this) {
            MEASURE -> DataLogApplication.getDrawable(R.drawable.ic_graph_white_24)
            GRAPH_REVIEW -> DataLogApplication.getDrawable(R.drawable.ic_visibility_white_24)
            CHANNEL_SETTING -> DataLogApplication.getDrawable(R.drawable.ic_settings_channel_white_24)
            DATA_LOG_SETTING -> DataLogApplication.getDrawable(R.drawable.ic_baseline_exposure_white_24)
            SAVE_SETTING -> DataLogApplication.getDrawable(R.drawable.ic_save_white_24)
            SETTING -> DataLogApplication.getDrawable(R.drawable.ic_settings_white_24)
        }
    }

    fun getTitle(context: Context): String {
        return when (this) {
            MEASURE -> context.getString(R.string.measure)
            GRAPH_REVIEW -> context.getString(R.string.graph_review)
            CHANNEL_SETTING -> context.getString(R.string.channel_setting)
            DATA_LOG_SETTING -> context.getString(R.string.data_log_setting)
            SAVE_SETTING -> context.getString(R.string.save_setting)
            SETTING -> context.getString(R.string.setting)
        }
    }

    fun getSubtitle(context: Context): String {
        return when (this) {
            MEASURE -> context.getString(R.string.measure_msg)
            GRAPH_REVIEW -> context.getString(R.string.graph_review_msg)
            CHANNEL_SETTING -> context.getString(R.string.channel_setting_msg)
            DATA_LOG_SETTING -> context.getString(R.string.data_log_setting_msg)
            SAVE_SETTING -> context.getString(R.string.save_setting_msg)
            SETTING -> context.getString(R.string.setting_msg)
        }
    }

    fun getFragment(): Fragment {
        return when (this) {
            MEASURE -> FragmentBluetoothDevices.newInstance()
            GRAPH_REVIEW -> FragmentGraphReviewFileList.newInstance()
            CHANNEL_SETTING -> FragmentChannelList.newInstance()
            DATA_LOG_SETTING -> FragmentDataLogSetting.newInstance()
            SAVE_SETTING -> FragmentSaveSetting.newInstance()
            SETTING -> FragmentOtherSetting.newInstance()
        }
    }

    fun getFragmentTag(): String {
        return when (this) {
            MEASURE -> "FragmentBluetoothDevices"
            GRAPH_REVIEW -> "FragmentGraphReview"
            CHANNEL_SETTING -> "FragmentChannelList"
            DATA_LOG_SETTING -> "FragmentDataLogSetting"
            SAVE_SETTING -> "FragmentSaveSetting"
            SETTING -> "FragmentOtherSetting"
        }
    }
}