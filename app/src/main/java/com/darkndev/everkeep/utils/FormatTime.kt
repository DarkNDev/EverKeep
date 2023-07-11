package com.darkndev.everkeep.utils

import android.app.AlarmManager
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

const val TODAY = "Today"
const val TOMORROW = "Tomorrow"
const val SELECT_DATE = "Select Date"

const val MORNING = "Morning (8:00 AM)"
const val AFTERNOON = "Afternoon (13:00 PM)"
const val EVENING = "Evening (18:00 PM)"
const val NIGHT = "Night (22:00 PM)"
const val SELECT_TIME = "Select Time"

const val DOES_NOT_REPEAT = "Does not repeat"
const val HOURLY = "Hourly"
const val DAILY = "Daily"
const val WEEKLY = "Weekly"
const val MONTHLY = "Monthly"
const val YEARLY = "Yearly"

fun getFormatTime(modifiedMillis: Long): String {
    val modified =
        ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(modifiedMillis),
            ZoneId.systemDefault()
        )
    val now = ZonedDateTime.now()

    return "Edited: " + if (modified.year != now.year) {
        modified.year.toString()
    } else if (modified.dayOfYear != now.dayOfYear) {
        DateTimeFormatter
            .ofPattern("MMM d", Locale.US)
            .format(modified)
    } else {
        DateTimeFormatter
            .ofPattern("h:mm a", Locale.US)
            .format(modified)
    }
}

fun getDatePickerDate(localDate: LocalDate?): String? {
    val now = LocalDate.now()

    return if (localDate == null) {
        null
    } else if (localDate == now) {
        TODAY
    } else if (localDate == now.plusDays(1)) {
        TOMORROW
    } else if (localDate.year != now.year) {
        DateTimeFormatter
            .ofPattern("MMM d yyyy", Locale.US)
            .format(localDate)
    } else {
        DateTimeFormatter
            .ofPattern("MMM d", Locale.US)
            .format(localDate)
    }
}

fun getTimePickerTime(time: LocalTime?): String? = when (time) {
    null -> null

    LocalTime.of(8, 0) -> {
        MORNING
    }

    LocalTime.of(13, 0) -> {
        AFTERNOON
    }

    LocalTime.of(18, 0) -> {
        EVENING
    }

    LocalTime.of(22, 0) -> {
        NIGHT
    }

    else -> {
        DateTimeFormatter
            .ofPattern("h:mm a", Locale.US)
            .format(time)
    }
}

fun getDuration(repeat: Long): String = when (repeat) {
    0L -> DOES_NOT_REPEAT
    AlarmManager.INTERVAL_HOUR -> HOURLY
    AlarmManager.INTERVAL_DAY -> DAILY
    AlarmManager.INTERVAL_DAY * 7 -> WEEKLY
    AlarmManager.INTERVAL_DAY * 30 -> MONTHLY
    AlarmManager.INTERVAL_DAY * 365 -> YEARLY
    else -> DOES_NOT_REPEAT
}
