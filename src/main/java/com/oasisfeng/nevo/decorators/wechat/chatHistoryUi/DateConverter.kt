package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.*

object DateConverter {
    // percentage of variation to allow - it will reduce the new avg by x percent
    private const val FUZZ_FACTOR = 6
    // the factor to multiply by to bring average diff to normal comparison
    // this brings super close messages together such as .2 to a respectable and comparable number
    private const val MULT_FACTOR = 6
    // cutoff after this minute marker and insert a date header anyways
    // regardless of fuzzing or leniency
    private const val CUTOFF = 6.0
    // cutoff for date header compared to current
    private const val CUTOFF_DATE_HEADER = 9.0
    // if there's more than this amount of variation in AVG gap, cut it
    private const val AVG_GAP_CUTOFF = 3.5
    // cutoff for only a single message
    private const val CUTOFF_SINGLE = 4.5


    private data class TimeData(
        val isToday: Boolean,
        val isYesterday: Boolean,
        val isCurrentWeel: Boolean,
        val targetDate: Date
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimeData(timestamp: Long): TimeData {
        val now = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)
        val target = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()

        val isToday = now.isEqual(target)
        val isYesterday = yesterday.isEqual(target)

        val nowWeek = now.get(WeekFields.of(Locale.getDefault()).weekOfYear())
        val targetWeek = target.get(WeekFields.of(Locale.getDefault()).weekOfYear())
        val isCurrentWeek = nowWeek == targetWeek && now.year == target.year

        return TimeData(
            isToday,
            isYesterday,
            isCurrentWeek,
            Date(timestamp)
        )
    }

    // Deermine whether it's time to insert a date header
    // We get a list of all timestamps AFTER the last date header
    fun shouldInsertDateHeader(timestamps: List<Timestamp>, nowTimestamp: Long): Boolean {
        timestamps.size.let {
            when {
                it >= 3 -> {
                    // check that last time header was inserted wasn't more than cutoff (prevents against successive
                    // messages causing date header to not insert
                    val lastHeaderDiff = (nowTimestamp - timestamps[0].timestamp).toDouble() / 1000 / 60
                    if (lastHeaderDiff >= CUTOFF_DATE_HEADER) {
                        return true
                    }

                    var currDiff = (nowTimestamp - timestamps[timestamps.lastIndex].timestamp).toDouble() / 1000 / 60
                    // check for difference of more than CUTOFF. Anything past that needs a date header
                    if (currDiff >= CUTOFF) {
                        return true
                    }

                    // messages before the CUTOFF can be a little tricky to determine
                    // get the average
                    var avgDiff = 0.0
                    var howMany = 0
                    for (i in 1 until timestamps.size-1) {
                        val one = timestamps[i].timestamp
                        val two = timestamps[i+1].timestamp
                        avgDiff += (two-one).toDouble() / 1000 / 60
                        howMany += 1
                    }

                    // the average message sending frequency
                    avgDiff /= howMany

                    // average gaps are too high, probably should just do a date header
                    if (avgDiff >= AVG_GAP_CUTOFF) {
                        return true
                    }

                    // it's likely that avgdiff is a low number here (although not certain
                    // even if it's not low though, it will trigger the system then

                    // bring low average numbers to comparable
                    avgDiff *= MULT_FACTOR
                    // mult factor made it too high
                    if (currDiff >= avgDiff) {
                        return true
                    }

                    // fuzz the current diff
                    currDiff -= currDiff * (FUZZ_FACTOR / 100)

                    if (currDiff >= avgDiff) {
                        return true
                    }
                    return false
                }

                it <= 2 -> {
                    // 1 message (header + message), we don't have any way to compare this to get context because 3 are not available!
                    // static minute comparison then..
                    // get last message index
                    //val index = if (timestamps.size == 1) 0 else 1
                    val diff = (nowTimestamp - timestamps[0].timestamp).toDouble() / 1000 / 60

                    if (diff >= CUTOFF_SINGLE) {
                        return true
                    }
                    return false
                }

                else -> {
                    Log.d(TAG, "shouldInsertDateHeader() unreachable code!")
                    return false
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDateMessage(context: Context, timestamp: Long): String {
        val (isToday, isYesterday, isCurrentWeek, targetDate) = getTimeData(timestamp)

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDateUserlist(context: Context, timestamp: Long): String {
        val (isToday, isYesterday, isCurrentWeek, targetDate) = getTimeData(timestamp)

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