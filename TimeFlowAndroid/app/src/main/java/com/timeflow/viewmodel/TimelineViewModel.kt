package com.timeflow.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.timeflow.data.TimeFlowDatabase
import com.timeflow.data.entity.TimelineEventEntity
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.util.*

/**
 * ViewModel for managing the timeline data and event operations.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    

    private val database = TimeFlowDatabase.getDatabase(application)
    // DAO for database operations
    private val timelineEventDao = database.timelineEventDao()
    
    /**
    * All events list
    */
    private val _events = mutableStateOf<MutableList<TimelineEvent>>(mutableStateListOf())
    /**
    * All events list.
    */
    val events: List<TimelineEvent> get() =_events
    
    /**
    * The search term used to filter events.
    */
    private val _searchTerm = mutableStateOf("")
    /**
    * The search term used to filter events.
    */
    val searchTerm get() =_searchTerm.value
    
    /**
    * The selected event type used to filter events.
    */
    private val _selectedEventType = mutableStateOf<EventType?>(null)
    /**
    * The selected event type used to filter events.
    */
    val selectedEventType get() =_selectedEventType.value
    
    /**
    * The event that is currently being edited.
    */
    private val _editingEvent = mutableStateOf<TimelineEvent?>(null)
    /**
    * The event that is currently being edited.
    */
    val editingEvent get() =_editingEvent.value
    
    init {
        // 加载数据
        loadEvents()
    }

    /** 
     * 加载事件数据
     */
    private fun loadEvents() {
        try {
            viewModelScope.launch {
                _events.value.clear()
                if (_searchTerm.value.isEmpty() && _selectedEventType.value == null) {
                    timelineEventDao.getAllEvents().collectLatest { entities ->
                        _events.value.addAll(entities.map { it.toTimelineEvent() })
                    }
                } else if (_searchTerm.value.isNotEmpty() && _selectedEventType.value == null) {
                    timelineEventDao.searchEvents(_searchTerm.value).collectLatest { entities ->
                        _events.value.addAll(entities.map { it.toTimelineEvent() })
                    }
                } else if (_searchTerm.value.isEmpty() && _selectedEventType.value != null) {
                    timelineEventDao.getEventsByType(_selectedEventType.value).collectLatest { entities ->
                        _events.value.addAll(entities.map { it.toTimelineEvent() })
                    }
                } else {
                    timelineEventDao.searchEventsByType(_selectedEventType.value!!.name, _searchTerm.value).collectLatest { entities ->
                        _events.value.addAll(entities.map { it.toTimelineEvent() })
                    }.apply { }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _events.clear()
        }
    }
    
    /** 
     * 添加新事件
    **/
    fun addEvent(eventType: EventType, description: String, imageUrl: String? = null, attachment: TimelineEvent.Attachment? = null, eventTimestamp: Date = Date()) {
        val title = TimelineEvent.deriveTitle(description)
        val newEvent = TimelineEvent(
            eventType = eventType,
            title = title,
            description = description,
            imageUrl = imageUrl,
            attachment = attachment,
            timestamp = eventTimestamp
        )
        try {
            viewModelScope.launch {
                timelineEventDao.insertEvent(newEvent.toEntity())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /** 
     * 更新事件
     */
    fun updateEvent(updatedEvent: TimelineEvent) {
        try {
            viewModelScope.launch {
                timelineEventDao.updateEvent(updatedEvent.toEntity())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _editingEvent.value = null // 清除编辑状态
    }
    
    /** 
     * 删除事件
    */
    fun deleteEvent(id: String) {
        try {
            viewModelScope.launch {
                timelineEventDao.deleteEventById(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /** 
    * Sets the search term.
    **/
    fun setSearchTerm(term: String) {
        _searchTerm.value = term
        loadEvents()
    }

    /** 
    * Sets the event type filter.
    **/
    fun setEventTypeFilter(eventType: EventType?) {
        _selectedEventType.value = eventType
        loadEvents()
    }

    /** 
    * Sets the event being edited.
    **/
    fun setEditingEvent(event: TimelineEvent?) {
        _editingEvent.value = event
    }

    /** 
     * Returns the filtered list of events.
     */
    fun getFilteredEvents(): List<TimelineEvent> {
        return _events.value
    }
    
  /**
     * 将TimelineEvent转换为TimelineEventEntity
     * @return The converted TimelineEventEntity.
     **/
    private fun TimelineEvent.toEntity(): TimelineEventEntity {
        return TimelineEventEntity(
            id = id,
            timestamp = timestamp,
            eventType = eventType,
            title = title,
            description = description,
            imageUrl = imageUrl,
            attachmentName = attachment?.name,    
            timestamp = timestamp
           )
    }
    
    /** 
     * 更新事件列表
     * @param events The new list of events.
     */
    fun updateEvents(events: List<TimelineEvent>) {
        _events.value = events.toMutableList()
    }
}
   /**
    * 将TimelineEventEntity转换为TimelineEvent
     * @return The converted TimelineEvent.
    **/
    private fun TimelineEventEntity.toTimelineEvent(): TimelineEvent {
        return TimelineEvent(
            id = id,
            timestamp = timestamp,
            eventType = eventType,
            title = title,
            description = description,
            imageUrl = imageUrl,
            attachment = attachmentName?.let { TimelineEvent.Attachment(it) },            
            timestamp = timestamp
        )
    }