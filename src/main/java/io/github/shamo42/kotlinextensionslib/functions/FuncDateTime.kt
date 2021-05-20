package io.github.shamo42.kotlinextensionslib.functions

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class FuncDateTime {


    fun getDate(unixTimeMs: Long, monthFirst: Boolean = true, isAmPm: Boolean = false): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getDateNew(unixTimeMs, monthFirst, isAmPm)
        } else {
            getDateOld(unixTimeMs, monthFirst, isAmPm)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateNew(unixTimeMs: Long, monthFirst: Boolean = true, isAmPm: Boolean = false): String {
        val date = if (monthFirst) "MMM dd" else "dd. MMM"
        val time = if (isAmPm) "hh:mm:ss aa" else "HH:mm:ss"
        return Instant.ofEpochSecond(unixTimeMs/1000)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("$date - $time"))
    }

    private fun getDateOld(unixTimeMs: Long, monthFirst: Boolean = true, isAmPm: Boolean = false): String {
        calendar.timeInMillis = unixTimeMs
        val df = DecimalFormat("00")

        val month = getMonthShort(calendar.get(Calendar.MONTH) + 1)
        val day = df.format(calendar.get(Calendar.DAY_OF_MONTH))
        val date = if (monthFirst) "$month $day" else "$day. $month"
        val hour = df.format(calendar.get(if (isAmPm) Calendar.HOUR else Calendar.HOUR_OF_DAY))
        val amPm by lazy { if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM" }
        return "$date - $hour: ${df.format(calendar.get(Calendar.MINUTE))}: ${df.format(calendar.get(Calendar.SECOND))}" +
                if (isAmPm) amPm else ""

    }

    private fun getMonthShort(monthOfYear: Int): String {
        return when (monthOfYear) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            else -> "Dec"
        }
    }

    companion object {
        private const val TAG = "FuncDateTime"

        private val calendar by lazy {
            GregorianCalendar()
        }

    }
}