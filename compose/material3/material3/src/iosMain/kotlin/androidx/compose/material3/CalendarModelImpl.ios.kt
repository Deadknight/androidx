/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material3

import androidx.annotation.RequiresApi
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.languageCode

/**
 * A [CalendarModel] implementation for API >= 26.
 *
 * @param locale a [CalendarLocale] to be used by this model
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
internal class CalendarModelImpl(locale: CalendarLocale) : CalendarModel(locale = locale) {

    override val today
        get(): CalendarDate {
            val systemLocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return CalendarDate(
                year = systemLocalDate.year,
                month = systemLocalDate.monthNumber,
                dayOfMonth = systemLocalDate.dayOfMonth,
                utcTimeMillis = systemLocalDate.toInstant(utcTimeZoneId).toEpochMilliseconds()
            )
        }

    override val firstDayOfWeek: Int = 0//Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek

    override val weekdayNames: List<Pair<String, String>> =
        // This will start with Monday as the first day, according to ISO-8601.
        with(locale) {
            DayOfWeek.values().map {
                it.name.toString() to it.name.toString()
                /*it.getDisplayName(
                    TextStyle.FULL,
                    /* locale = */ this
                ) to it.getDisplayName(
                    TextStyle.NARROW,
                    /* locale = */ this
                )*/
            }
        }

    override fun getDateInputFormat(locale: CalendarLocale): DateInputFormat {
        //TODO?
        return datePatternAsInputFormat(
            "dd/MM/yyyy"
        )
    }

    override fun getCanonicalDate(timeInMillis: Long): CalendarDate {
        val localDate =
            Instant.fromEpochMilliseconds(timeInMillis).toLocalDateTime(utcTimeZoneId)
        return CalendarDate(
            year = localDate.year,
            month = localDate.monthNumber,
            dayOfMonth = localDate.dayOfMonth,
            utcTimeMillis = localDate.toInstant(TimeZone.UTC).toEpochMilliseconds()
        )
    }

    override fun getMonth(timeInMillis: Long): CalendarMonth {
        val date = Instant.fromEpochMilliseconds(timeInMillis)
        .toLocalDateTime(utcTimeZoneId)

        return getMonth(
                LocalDate(date.year, date.month, 1)
        )
    }

    override fun getMonth(date: CalendarDate): CalendarMonth {
        return getMonth(LocalDate(date.year, date.month, 1))
    }

    override fun getMonth(year: Int, month: Int): CalendarMonth {
        return getMonth(LocalDate(year, month, 1))
    }

    override fun getDayOfWeek(date: CalendarDate): Int {
        return date.toLocalDate().dayOfWeek.ordinal
    }

    fun LocalDateTime.plus(value: Long, unit: DateTimeUnit.TimeBased): LocalDateTime {
        val timeZone = TimeZone.currentSystemDefault()
        return this.toInstant(timeZone)
            .plus(value, unit)
            .toLocalDateTime(timeZone)
    }

    fun LocalDateTime.minus(value: Long, unit: DateTimeUnit.TimeBased): LocalDateTime {
        val timeZone = TimeZone.currentSystemDefault()
        return this.toInstant(timeZone)
            .minus(value, unit)
            .toLocalDateTime(timeZone)
    }

    override fun plusMonths(from: CalendarMonth, addedMonthsCount: Int): CalendarMonth {
        if (addedMonthsCount <= 0) return from

        val firstDayLocalDate = from.toLocalDate()
        val laterMonth = firstDayLocalDate.plus(addedMonthsCount.toLong(), DateTimeUnit.MONTH)
        return getMonth(laterMonth)
    }

    override fun minusMonths(from: CalendarMonth, subtractedMonthsCount: Int): CalendarMonth {
        if (subtractedMonthsCount <= 0) return from

        val firstDayLocalDate = from.toLocalDate()
        val earlierMonth = firstDayLocalDate.minus(subtractedMonthsCount.toLong(), DateTimeUnit.MONTH)
        return getMonth(earlierMonth)
    }

    override fun formatWithPattern(
        utcTimeMillis: Long,
        pattern: String,
        locale: CalendarLocale
    ): String = formatWithPattern(utcTimeMillis, pattern, locale, formatterCache)

    override fun parse(date: String, pattern: String): CalendarDate? {
        val formatter = NSDateFormatter().also {
            it.dateFormat = pattern
        }

        return try {
            val nsdate = formatter.dateFromString(date)
            val components = NSCalendar.currentCalendar.components(0u, nsdate!!)
            CalendarDate(
                year = components.year.toInt(),
                month = components.month.toInt(),
                dayOfMonth = components.day.toInt(),
                utcTimeMillis = LocalDateTime(components.year.toInt(),
                    components.month.toInt(),
                    components.day.toInt(), 0, 0, 0, 0)
                    .toInstant(utcTimeZoneId).toEpochMilliseconds()
            )
        } catch (pe: Exception) {
            null
        }
    }

    override fun toString(): String {
        return "CalendarModel"
    }

    companion object {

        /**
         * Formats a UTC timestamp into a string with a given date format pattern.
         *
         * @param utcTimeMillis a UTC timestamp to format (milliseconds from epoch)
         * @param pattern a date format pattern
         * @param locale the [CalendarLocale] to use when formatting the given timestamp
         * @param cache a [MutableMap] for caching formatter related results for better performance
         */
        fun formatWithPattern(
            utcTimeMillis: Long,
            pattern: String,
            locale: CalendarLocale,
            cache: MutableMap<String, Any>
        ): String {
            val formatter = getCachedDateTimeFormatter(pattern, locale, cache)
            formatter.dateFormat = pattern
            val localDateTime = Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(utcTimeZoneId)
            var nsDate = fromComponents(localDateTime.toNSDateComponents())
            if(nsDate == null)
                nsDate = NSDate()
            return formatter.stringFromDate(nsDate)
        }

        internal val utcTimeZoneId: TimeZone = TimeZone.UTC

        private fun getComponents(date: NSDate) : NSDateComponents {
            return NSCalendar.currentCalendar.components(0U, date)
        }

        private fun fromComponents(components: NSDateComponents) : NSDate? {
            return NSCalendar.currentCalendar.dateFromComponents(components)
        }

        private fun getCachedDateTimeFormatter(
            pattern: String,
            locale: CalendarLocale,
            cache: MutableMap<String, Any>
        ): NSDateFormatter {
            // Prepend the pattern and language tag with a "P" to avoid cache collisions when the
            // called already cached a string as value when the pattern equals to the skeleton it
            // was created from.
            return cache.getOrPut(key = "P:$pattern${locale.languageCode}") {
                NSDateFormatter().also {
                    it.dateFormat = pattern
                }
            } as NSDateFormatter
        }
    }

    data class YearMonth(val year: Int, val month: Month) {
        fun atDay(day: Int): LocalDate {
            return LocalDate(year, month, day)
        }

        fun atEndOfMonth(): LocalDate {
            val lastDay = month.number.monthLength(isLeapYear(year))
            return LocalDate(year, month, lastDay)
        }

        fun monthLength(): Int {
            return month.number.monthLength(isLeapYear(year))
        }

        fun plusMonths(months: Int): YearMonth {
            val newMonth = month.number + months
            val newYear = year + (newMonth - 1) / 12
            val newMonthNumber = (newMonth - 1) % 12 + 1
            return YearMonth(newYear, newMonthNumber.toMonth())
        }

        fun minusMonths(months: Int): YearMonth {
            val totalMonths = year * 12 + (month.number - 1) - months
            val newYear = totalMonths / 12
            val newMonth = (totalMonths % 12) + 1 // Adding 1 because months are 1-based

            return YearMonth(newYear, newMonth.toMonth())
        }

        private fun Int.toMonth(): Month {
            return Month.values()[this - 1]
        }

        // the below 2 functions were taken from `java.time` package
        private fun isLeapYear(year: Int): Boolean {
            val prolepticYear: Long = year.toLong()
            return prolepticYear and 3 == 0L && (prolepticYear % 100 != 0L || prolepticYear % 400 == 0L)
        }

        private fun Int.monthLength(isLeapYear: Boolean): Int {
            return when (this) {
                2 -> if (isLeapYear) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
        }

        companion object {
            fun from(localDate: LocalDate): YearMonth {
                return YearMonth(localDate.year, localDate.month)
            }
        }
    }

    private fun getMonth(firstDayLocalDate: LocalDate): CalendarMonth {
        val difference = firstDayLocalDate.dayOfWeek.ordinal - firstDayOfWeek
        val daysFromStartOfWeekToFirstOfMonth = if (difference < 0) {
            difference + DaysInWeek
        } else {
            difference
        }
        val firstDayEpochMillis = LocalDateTime(firstDayLocalDate.year, firstDayLocalDate.monthNumber, firstDayLocalDate.dayOfMonth, 0, 0)
            .toInstant(utcTimeZoneId).toEpochMilliseconds()
        val ym = YearMonth(firstDayLocalDate.year, firstDayLocalDate.month)
        return CalendarMonth(
            year = firstDayLocalDate.year,
            month = firstDayLocalDate.monthNumber,
            numberOfDays = ym.monthLength(),
            daysFromStartOfWeekToFirstOfMonth = daysFromStartOfWeekToFirstOfMonth,
            startUtcTimeMillis = firstDayEpochMillis
        )
    }

    private fun CalendarMonth.toLocalDate(): LocalDate {
        val localDateTime = Instant.fromEpochMilliseconds(startUtcTimeMillis).toLocalDateTime(utcTimeZoneId)
        return LocalDate(localDateTime.year, localDateTime.month, localDateTime.dayOfMonth)
    }

    private fun CalendarDate.toLocalDate(): LocalDate {
        return LocalDate(
            this.year,
            this.month,
            this.dayOfMonth
        )
    }
}
