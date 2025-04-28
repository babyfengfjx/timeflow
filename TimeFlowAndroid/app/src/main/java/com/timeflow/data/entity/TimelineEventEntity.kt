package com.timeflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.timeflow.model.EventType
import java.util.Date

@Entity(tableName = "timeline_events")
data class TimelineEventEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Date,
    val eventType: EventType,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val attachmentName: String?
) 