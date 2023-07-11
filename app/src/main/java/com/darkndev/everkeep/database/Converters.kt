package com.darkndev.everkeep.database

import androidx.room.TypeConverter
import java.time.ZonedDateTime

class Converters {

    @TypeConverter
    fun longToZonedDateTime(text: String): ZonedDateTime? {
        return if (text.isEmpty()) null else ZonedDateTime.parse(text)
    }

    @TypeConverter
    fun zonedDateTimeToLong(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.toString() ?: ""
    }
}