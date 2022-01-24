package kr.co.greentech.dataloggerapp

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {

    fun getTodayDate(): Date {
        val calendar: Calendar = Calendar.getInstance()
        return getStartOfDay(calendar.time, calendar)
    }


    private fun getStartOfDay(date: Date, calendar: Calendar): Date {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.time
    }

    fun getEndOfDay(date: Date, calendar: Calendar): Date {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, -1);
        return calendar.time
    }

    fun toString(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }
}