package com.timeflow.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timeflow.data.TimeFlowDatabase
import com.timeflow.data.entity.TimelineEventEntity
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

/**
 * 时间轴视图模型，负责管理时间轴数据和事件操作
 */
class TimelineViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TimeFlowDatabase.getDatabase(application)
    private val timelineEventDao = database.timelineEventDao()
    
    // 所有事件列表
    private val _events = mutableStateListOf<TimelineEvent>()
    val events: List<TimelineEvent> get() = _events
    
    // 搜索关键词
    private val _searchTerm = mutableStateOf("")
    val searchTerm get() = _searchTerm.value
    
    // 当前选中的事件类型过滤器
    private val _selectedEventType = mutableStateOf<EventType?>(null)
    val selectedEventType get() = _selectedEventType.value
    
    // 正在编辑的事件
    private val _editingEvent = mutableStateOf<TimelineEvent?>(null)
    val editingEvent get() = _editingEvent.value
    
    init {
        // 加载数据
        loadEvents()
    }
    
    /**
     * 加载事件数据
     */
    private fun loadEvents() {
        viewModelScope.launch {
            timelineEventDao.getAllEvents().collectLatest { entities ->
                _events.clear()
                _events.addAll(entities.map { it.toTimelineEvent() })
            }
        }
    }
    
    /**
     * 添加新事件
     */
    fun addEvent(eventType: EventType, description: String, imageUrl: String? = null, attachment: TimelineEvent.Attachment? = null) {
        val title = TimelineEvent.deriveTitle(description)
        val newEvent = TimelineEvent(
            eventType = eventType,
            title = title,
            description = description,
            imageUrl = imageUrl,
            attachment = attachment
        )
        
        viewModelScope.launch {
            timelineEventDao.insertEvent(newEvent.toEntity())
        }
    }
    
    /**
     * 更新事件
     */
    fun updateEvent(updatedEvent: TimelineEvent) {
        viewModelScope.launch {
            timelineEventDao.updateEvent(updatedEvent.toEntity())
        }
        _editingEvent.value = null // 清除编辑状态
    }
    
    /**
     * 删除事件
     */
    fun deleteEvent(id: String) {
        viewModelScope.launch {
            timelineEventDao.deleteEventById(id)
        }
    }
    
    /**
     * 设置搜索关键词
     */
    fun setSearchTerm(term: String) {
        _searchTerm.value = term
        if (term.isNotEmpty()) {
            viewModelScope.launch {
                timelineEventDao.searchEvents(term).collectLatest { entities ->
                    _events.clear()
                    _events.addAll(entities.map { it.toTimelineEvent() })
                }
            }
        } else {
            loadEvents()
        }
    }
    
    /**
     * 设置事件类型过滤器
     */
    fun setEventTypeFilter(eventType: EventType?) {
        _selectedEventType.value = eventType
        if (eventType != null) {
            viewModelScope.launch {
                timelineEventDao.getEventsByType(eventType.name).collectLatest { entities ->
                    _events.clear()
                    _events.addAll(entities.map { it.toTimelineEvent() })
                }
            }
        } else {
            loadEvents()
        }
    }
    
    /**
     * 设置正在编辑的事件
     */
    fun setEditingEvent(event: TimelineEvent?) {
        _editingEvent.value = event
    }
    
    /**
     * 获取过滤后的事件列表
     */
    fun getFilteredEvents(): List<TimelineEvent> {
        return events
    }
    
    /**
     * 将TimelineEvent转换为TimelineEventEntity
     */
    private fun TimelineEvent.toEntity(): TimelineEventEntity {
        return TimelineEventEntity(
            id = id,
            timestamp = timestamp,
            eventType = eventType,
            title = title,
            description = description,
            imageUrl = imageUrl,
            attachmentName = attachment?.name
        )
    }
    
    /**
     * 将TimelineEventEntity转换为TimelineEvent
     */
    private fun TimelineEventEntity.toTimelineEvent(): TimelineEvent {
        return TimelineEvent(
            id = id,
            timestamp = timestamp,
            eventType = eventType,
            title = title,
            description = description,
            imageUrl = imageUrl,
            attachment = attachmentName?.let { TimelineEvent.Attachment(it) }
        )
    }
}