package com.timeflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.timeflow.data.dao.TimelineEventDao
import com.timeflow.data.entity.TimelineEventEntity
import com.timeflow.data.util.Converters

@Database(
    entities = [TimelineEventEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TimeFlowDatabase : RoomDatabase() {
    abstract fun timelineEventDao(): TimelineEventDao
    
    companion object {
        @Volatile
        private var INSTANCE: TimeFlowDatabase? = null
        
        fun getDatabase(context: Context): TimeFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeFlowDatabase::class.java,
                    "timeflow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 