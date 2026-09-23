package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateHelper {
    private const val MILLIS_IN_DAY = 86400000L

    fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / MILLIS_IN_DAY
    }

    fun epochDayFromDate(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / MILLIS_IN_DAY
    }

    fun getCalendarForEpochDay(epochDay: Long): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochDay * MILLIS_IN_DAY
        return cal
    }

    fun formatEnglishHeaderDate(epochDay: Long): String {
        val cal = getCalendarForEpochDay(epochDay)
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Sunday"
        }
        val month = when (cal.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "Jan"
            Calendar.FEBRUARY -> "Feb"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Apr"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Aug"
            Calendar.SEPTEMBER -> "Sep"
            Calendar.OCTOBER -> "Oct"
            Calendar.NOVEMBER -> "Nov"
            else -> "Dec"
        }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "$dayOfWeek, $month $day"
    }

    fun formatPersianHeaderDate(epochDay: Long): String {
        val cal = getCalendarForEpochDay(epochDay)
        val dayOfWeekFa = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یک‌شنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            else -> "جمعه"
        }
        // Approximate Persian month for UI match (e.g., آبان)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "$dayOfWeekFa، $day آبان"
    }

    fun formatPersianNumber(number: Int): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val str = number.toString()
        val sb = StringBuilder()
        for (c in str) {
            if (c in '0'..'9') {
                sb.append(persianDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun formatPersianTime(hour: Int, minute: Int): String {
        val h = String.format("%02d", hour)
        val m = String.format("%02d", minute)
        return "${formatPersianNumber(h.toInt())}:${formatPersianNumber(m.toInt())}"
    }

    fun formatMinuteTime(minuteOfDay: Int, isPersian: Boolean): String {
        if (minuteOfDay < 0) return if (isPersian) "تمام روز" else "All Day"
        val h = minuteOfDay / 60
        val m = minuteOfDay % 60
        val timeStr = String.format("%02d:%02d", h, m)
        return if (isPersian) {
            val sb = StringBuilder()
            val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
            for (c in timeStr) {
                if (c in '0'..'9') sb.append(persianDigits[c - '0']) else sb.append(c)
            }
            sb.toString()
        } else {
            timeStr
        }
    }
}
