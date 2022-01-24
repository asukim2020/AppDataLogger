package kr.co.greentech.dataloggerapp.fragment.savesetting.enum

import android.content.Context
import kr.co.greentech.dataloggerapp.R

enum class EqualIntervalSaveSettingType(val value: Int) {
    INTERVAL(0);

    fun getTitle(context: Context): String {
        return when(this) {
            INTERVAL -> context.getString(R.string.input_save_interval)
        }
    }
}