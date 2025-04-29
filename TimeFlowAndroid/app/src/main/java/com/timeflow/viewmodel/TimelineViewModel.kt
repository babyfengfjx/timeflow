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
    val events: List<TimelineEvent> get() = _events.value
    
    /**
    * The search term used to filter events.
    */
    private val _searchTerm = mutableStateOf("")
    /**
    * The search term used to filter events.
    */
    val searchTerm get() = _searchTerm.value
    
    /**
    * The selected event type used to filter events.
    */
    private val _selectedEventType = mutableStateOf<EventType?>(null)
    /**
    * The selected event type used to filter events.
    */
    val selectedEventType get() = _selectedEventType.value
    
    /**
    * The event that is currently being edited.
    */
    private val _editingEvent = mutableStateOf<TimelineEvent?>(null)
    /**
    * The event that is currently being edited.
    */
    val editingEvent get() = _editingEvent.value

    private val _isLoading = mutableStateOf(false)
    val isLoading get() = _isLoading.value

    private val _error = mutableStateOf<String?>(null)
    val error get() = _error.value

    private val _cache = mutableStateOf<Map<String, TimelineEvent>>(emptyMap())
    
    init {
        // 加载数据
        loadEvents()
    }

    /** 
     * 加载事件数据
     */
    private fun loadEvents() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _events.value.clear()
                
                val flow = when {
                    _searchTerm.value.isEmpty() && _selectedEventType.value == null -> {
                        timelineEventDao.getAllEvents()
                    }
                    _searchTerm.value.isNotEmpty() && _selectedEventType.value == null -> {
                        timelineEventDao.searchEvents(_searchTerm.value)
                    }
                    _searchTerm.value.isEmpty() && _selectedEventType.value != null -> {
                        timelineEventDao.getEventsByType(_selectedEventType.value!!.name)
                    }
                    else -> {
                        timelineEventDao.searchEventsByType(_selectedEventType.value!!.name, _searchTerm.value)
                    }
                }
                
                flow.collectLatest { entities ->
                    val newEvents = entities.map { it.toTimelineEvent() }
                    _events.value.addAll(newEvents)
                    // Update cache
                    _cache.value = _cache.value + newEvents.associateBy { it.id }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "加载事件失败"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /** 
     * 添加新事件
    **/
    fun addEvent(eventType: EventType, description: String, imageUrl: String? = null, attachment: TimelineEvent.Attachment? = null, eventTimestamp: Date = Date()) {
        viewModelScope.launch {
            try {
                _error.value = null
                val title = TimelineEvent.deriveTitle(description)
                val newEvent = TimelineEvent(
                    eventType = eventType,
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    attachment = attachment,
                    timestamp = eventTimestamp
                )
                timelineEventDao.insertEvent(newEvent.toEntity())
                // Update cache
                _cache.value = _cache.value + (newEvent.id to newEvent)
            } catch (e: Exception) {
                _error.value = e.message ?: "添加事件失败"
                e.printStackTrace()
            }
        }
    }
    
    /** 
     * 更新事件
     */
    fun updateEvent(updatedEvent: TimelineEvent) {
        viewModelScope.launch {
            try {
                _error.value = null
                timelineEventDao.updateEvent(updatedEvent.toEntity())
                // Update cache
                _cache.value = _cache.value + (updatedEvent.id to updatedEvent)
            } catch (e: Exception) {
                _error.value = e.message ?: "更新事件失败"
                e.printStackTrace()
            }
            _editingEvent.value = null // 清除编辑状态
        }
    }
    
    /** 
     * 删除事件
    */
    fun deleteEvent(id: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                timelineEventDao.deleteEventById(id)
                // Remove from cache
                _cache.value = _cache.value - id
            } catch (e: Exception) {
                _error.value = e.message ?: "删除事件失败"
                e.printStackTrace()
            }
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
        // Update cache
        _cache.value = events.associateBy { it.id }
    }

    fun clearError() {
        _error.value = null
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