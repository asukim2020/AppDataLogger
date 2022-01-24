package kr.co.greentech.dataloggerapp

import org.junit.Test
import java.util.regex.Pattern
import kotlin.math.roundToInt

class TimeUtilTest {

    @Test
    fun floatPoint() {
        val a = 12.345678F

        var weight = 1
        val position = 0

        for (i in 0 until position) {
            weight *= 10
        }

        val b = weight.toFloat()
        println(weight)
        println(a)
        val data = (a * b).roundToInt() / b
        println(data)
        println(data.toInt())
        println(data.toInt().toFloat())
    }

    @Test
    fun pattern() {
        val pattern = Pattern.compile("[ !@#$%^&*(),.?\":{}|<>]")
        print(pattern.matcher("!").find())
    }

    @Test
    fun today() {
        val today = TimeUtil.getTodayDate()
        println("today: $today")
    }

    @Test
    fun tomorrowDate() {
        val today = TimeUtil.getTodayDate()
        val tomorrow = today.tomorrow
        val str = TimeUtil.toString(tomorrow)
        println("tomorrow: $str")
    }

    @Test
    fun yesterdayDate() {
        val today = TimeUtil.getTodayDate()
        val yesterday = today.yesterday
        val str = TimeUtil.toString(yesterday)
        println("yesterday: $str")
    }

    @Test
    fun startOfWeek() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.startOfWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun endOfWeek() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.endOfWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun nextWeek() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.startOfWeek.nextWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun prevWeek() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.startOfWeek.prevWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun startOfMonth() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.startOfMonth)
        println("startOfMonth: $str")
    }

    @Test
    fun endOfMonth() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.endOfMonth)
        println("endOfMonth: $str")
    }

    @Test
    fun nextMonth() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.startOfMonth.nextMonth)
        println("startOfWeek: $str")
    }

    @Test
    fun prevMonth() {
        val today = TimeUtil.getTodayDate()
        val str = TimeUtil.toString(today.startOfMonth.prevMonth)
        println("startOfWeek: $str")
    }
}