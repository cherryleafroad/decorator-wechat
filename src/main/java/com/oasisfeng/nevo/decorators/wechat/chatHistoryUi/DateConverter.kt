package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import com.oasisfeng.nevo.decorators.wechat.R
import java.util.*


object DateConverter {
    private data class TimeData(
        val isToday: Boolean,
        val isYesterday: Boolean,
        val isCurrentWeel: Boolean,
        val targetDate: Date
    )

    private fun getTimeData(context: Context, timestamp: Long): TimeData {
        val dateFmt = DateFormat.getDateFormat(context)

        val targetDate = Date(timestamp)
        val calendar = dateFmt.calendar
        val targetCal = dateFmt.calendar
        targetCal.time = targetDate


        val day = calendar.get(Calendar.DAY_OF_YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
        val targetWeek = targetCal.get(Calendar.WEEK_OF_YEAR)
        val targetYear = targetCal.get(Calendar.YEAR)

        val isToday = year == targetYear && day == targetDay
        val isYesterday = year == targetYear && day-1 == targetDay
        val isCurrentWeek = week == targetWeek && year == targetYear

        return TimeData(
            isToday,
            isYesterday,
            isCurrentWeek,
            targetDate
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun toDateMessage(context: Context, timestamp: Long): String {
        val (isToday, isYesterday, isCurrentWeek, targetDate) = getTimeData(context, timestamp)

        //
        // Just like in WeChat, there are 4 different display modes ->
        // 1: Time is displayed today
        //        -> 1:20 AM
        // 2: Time is displayed yesterday
        //        -> Yesterday 5:20 AM
        // 3: Time is displayed this week
        //        -> Tue 6:16 AM
        // 4: Time is displayed at any point in the past
        //        -> 5/21/20 7:56 AM
        //

        val simpleDate = SimpleDateFormat.getDateTimeInstance() as SimpleDateFormat

        // 1:56 AM
        if (isToday) {
            simpleDate.applyLocalizedPattern("h:mm a")
            return simpleDate.format(targetDate)
        }

        // Yesterday 2:45 AM
        if (isYesterday) {
            simpleDate.applyLocalizedPattern("h:mm a")
            val time = simpleDate.format(targetDate)
            val yesterday = context.getString(R.string.yesterday)
            return "$yesterday $time"
        }

        // Tue 5:20 PM
        if (isCurrentWeek) {
            simpleDate.applyLocalizedPattern("EEE h:mm a")
            return simpleDate.format(targetDate)
        }

        // 3/5/20 5:56 PM
        simpleDate.applyLocalizedPattern("M/d/yy h:mm a")
        return simpleDate.format(targetDate)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun toDateUserlist(context: Context, timestamp: Long): String {
        val (isToday, isYesterday, isCurrentWeek, targetDate) = getTimeData(context, timestamp)

        val simpleDate = SimpleDateFormat.getDateTimeInstance() as SimpleDateFormat

        // 1:56 AM
        if (isToday) {
            simpleDate.applyLocalizedPattern("h:mm a")
            return simpleDate.format(targetDate)
        }

        // Yesterday
        if (isYesterday) {
            return context.getString(R.string.yesterday)
        }

        // Tue
        if (isCurrentWeek) {
            simpleDate.applyLocalizedPattern("EEE")
            return simpleDate.format(targetDate)
        }

        // 3/5/20
        simpleDate.applyLocalizedPattern("M/d/yy")
        return simpleDate.format(targetDate)
    }
}