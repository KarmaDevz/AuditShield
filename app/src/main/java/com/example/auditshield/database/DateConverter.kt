package com.example.auditshield.database

import androidx.room.TypeConverter
import java.sql.Date

class DateConverter(value: Any) {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

}
