package ir.arminniromandi.timekar.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.text.iterator

object DateHelper {
    private const val MILLIS_IN_DAY = 86400000L
    private val UTC_ZONE = TimeZone.getTimeZone("UTC")

    /**
     * شماره روز را مستقل از منطقه زمانی محلی بر اساس UTC برمی‌گرداند.
     */
    fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        // تاریخ روز محلی کاربر را به دست می‌آوریم
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return epochDayFromDate(y, m, d)
    }

    /**
     * محاسبه امن Epoch Day بدون وابستگی به Timezone دستگاه
     */
    fun epochDayFromDate(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance(UTC_ZONE)
        cal.clear()
        cal.set(year, month - 1, day, 0, 0, 0)
        return cal.timeInMillis / MILLIS_IN_DAY
    }

    fun getCalendarForEpochDay(epochDay: Long): Calendar {
        val cal = Calendar.getInstance(UTC_ZONE)
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

    /**
     * تبدیل دقیق به تاریخ شمسی به همراه نام روز و ماه صحیح
     */
    fun formatPersianHeaderDate(epochDay: Long): String {
        val cal = getCalendarForEpochDay(epochDay)
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)

        val persianDate = gregorianToPersian(gYear, gMonth, gDay)

        val dayOfWeekFa = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یک‌شنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            else -> "جمعه"
        }

        val persianMonths = arrayOf(
            "فروردین", "اردیبهشت", "خرداد",
            "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )

        val monthFa = persianMonths[persianDate.month - 1]
        val dayFa = formatPersianNumber(persianDate.day)

        return "$dayOfWeekFa، $dayFa $monthFa"
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


    // بررسی کبیسه بودن سال شمسی بر اساس الگوریتم دوره ۳۳ ساله خیامی
    fun isPersianLeapYear(year: Int): Boolean {
        val r = (year - 474) % 33
        val rem = if (r < 0) r + 33 else r
        return rem in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    // تعداد روزهای ماه شمسی
    fun getPersianMonthDays(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isPersianLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    // تبدیل تاریخ شمسی به Epoch Day (معکوس دقیق gregorianToPersian)
    fun persianToEpochDay(jy: Int, jm: Int, jd: Int): Long {
        val jyPrime = jy - 979
        val jDayNoInYear = if (jm <= 7) {
            (jm - 1) * 31 + (jd - 1)
        } else {
            186 + (jm - 7) * 30 + (jd - 1)
        }
        val jDayNo = 365L * jyPrime + (jyPrime / 33) * 8 + ((jyPrime % 33 + 3) / 4) + jDayNoInYear
        val gDayNo = jDayNo + 79
        return gDayNo - 135140L
    }

    // دریافت تاریخ شمسی امروز
    fun todayPersianDate(): PersianDate {
        val cal = Calendar.getInstance()
        return gregorianToPersian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    data class PersianDate(val year: Int, val month: Int, val day: Int)

    fun epochDayToPersianDirect(epochDay: Long): PersianDate {
        // تبدیل Epoch Day به Julian Day Number (مبدأ 1 ژانویه 1970 برابر با 2440588 در گاه‌شماری ژولینی است)
        val jdn = epochDay + 2440588L

        // تبدیل JDN به تاریخ جلالی
        val dep = jdn - 2121446L // انحراف از مبدأ جلالی
        val cycle = dep / 12053L
        val rem = dep % 12053L

        var c = rem / 1461L
        var cRem = rem % 1461L

        if (c == 8L) {
            c = 7L
            cRem += 1461L
        }

        var yearIn4 = cRem / 365L
        var dayInYear = cRem % 365L

        if (yearIn4 == 4L) {
            yearIn4 = 3L
            dayInYear += 365L
        }

        val year = (cycle * 33L + c * 4L + yearIn4 + 475L).toInt()

        val month: Int
        val day: Int
        if (dayInYear < 186L) {
            month = (dayInYear / 31L).toInt() + 1
            day = (dayInYear % 31L).toInt() + 1
        } else {
            val remDays = dayInYear - 186L
            month = (remDays / 30L).toInt() + 7
            day = (remDays % 30L).toInt() + 1
        }

        return PersianDate(year, month, day)
    }


    /**
     * الگوریتم استاندارد تبدیل میلادی به شمسی
     */
    fun gregorianToPersian(gYear: Int, gMonth: Int, gDay: Int): PersianDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val isLeapG = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)
        if (isLeapG) gDaysInMonth[1] = 29

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365L * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += ((jDayNo - 1) / 365).toInt()
            jDayNo = (jDayNo - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (jDayNo < 186) {
            jm = 1 + (jDayNo / 31).toInt()
            jd = 1 + (jDayNo % 31).toInt()
        } else {
            jm = 7 + ((jDayNo - 186) / 30).toInt()
            jd = 1 + ((jDayNo - 186) % 30).toInt()
        }

        return PersianDate(jy.toInt(), jm, jd)
    }
}