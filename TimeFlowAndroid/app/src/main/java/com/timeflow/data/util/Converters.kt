package com.timeflow.data.util

import androidx.room.TypeConverter
import com.timeflow.model.EventType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromEventType(value: EventType): String {
        return value.name
    }
    
    @TypeConverter
    fun toEventType(value: String): EventType {
        return EventType.valueOf(value)
    }
} 