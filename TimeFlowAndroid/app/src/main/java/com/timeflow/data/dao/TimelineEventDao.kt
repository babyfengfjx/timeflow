package com.timeflow.data.dao

import androidx.room.*
import com.timeflow.data.entity.TimelineEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineEventDao {
    @Query("SELECT * FROM timeline_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<TimelineEventEntity>>
    
    @Query("SELECT * FROM timeline_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEventsByType(eventType: String): Flow<List<TimelineEventEntity>>
    
    @Query("SELECT * FROM timeline_events WHERE title LIKE '%' || :searchTerm || '%' OR description LIKE '%' || :searchTerm || '%' ORDER BY timestamp DESC")
    fun searchEvents(searchTerm: String): Flow<List<TimelineEventEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TimelineEventEntity)
    
    @Update
    suspend fun updateEvent(event: TimelineEventEntity)
    
    @Delete
    suspend fun deleteEvent(event: TimelineEventEntity)
    
    @Query("DELETE FROM timeline_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)
} 