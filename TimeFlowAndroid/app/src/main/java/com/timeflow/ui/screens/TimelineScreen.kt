package com.timeflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.timeflow.R
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent
import com.timeflow.ui.components.EventTypeChip
import com.timeflow.ui.screens.EventDetailDialog
import com.timeflow.viewmodel.TimelineViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date


    /**
 * 时间轴屏幕，显示事件列表、搜索和过滤功能
     */
@OptIn(ExperimentalMaterial3Api::class)

@Composable


fun TimelineScreen(viewModel: TimelineViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    val events by viewModel.events.collectAsState(initial = emptyList())    
    val searchTerm by viewModel.searchTerm.collectAsState()
    val selectedEventType = viewModel.selectedEventType
    val editingEvent = viewModel.editingEvent

    var showEventDetailDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<TimelineEvent?>(null) }
    var showSettingDialog by remember { mutableStateOf(false) }
    var isDarkTheme by remember { mutableStateOf(false) }

    var showAddEventDialog by remember { mutableStateOf(false) }
    var showEditEventDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<TimelineEvent?>(null) }
    var searchExpanded by remember { mutableStateOf(false) } 
    
    // 当编辑事件变化时更新对话框状态
    LaunchedEffect(editingEvent) {
        showEditEventDialog = editingEvent != null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TimeFlow") },
                actions = { // 搜索按钮
                    IconButton(onClick = { showSettingDialog = !showSettingDialog }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "设置")
                    }
                    // 设置对话框
                    if (showSettingDialog) {
                        SettingDialog(onDismiss = { showSettingDialog = false }, onThemeChange = {isDarkTheme = it}, isDarkTheme = isDarkTheme)
                    }

                    // 搜索按钮
                    IconButton(onClick = {
                        searchExpanded = !searchExpanded
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    // 过滤按钮
                    var showFilterMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = {
                        showFilterMenu = !showFilterMenu }) {
                        Icon(Icons.Default.FilterList, contentDescription = "过滤")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加事件")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // 搜索栏
            if (searchExpanded) {
                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    query = searchTerm,
                    onQueryChange = { viewModel.setSearchTerm(it) },
                    onSearch = { searchExpanded = false },
                    active = true,
                    onActiveChange = { searchExpanded = it },
                    placeholder = { Text("搜索事件...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索图标") },
                    trailingIcon = {
                        if (searchTerm.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchTerm("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除搜索")
                            }
                        }
                    }
                ) {}
            }

            // 事件类型过滤器
            if (showFilterMenu) {
                FilterDropdownMenu(
                    selectedEventType = selectedEventType,
                    onEventTypeSelected = {
                        viewModel.setEventTypeFilter(it)
                        showFilterMenu = false
                    },
                    onDismiss = { showFilterMenu = false }
                )
            }
            
            
            // 时间轴列表 
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .width(1.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp)) {
                    drawLine(color = MaterialTheme.colorScheme.outline, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, size.height)) 
                } 
                LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)) {
                    val groupedEvents = events.groupBy { LocalDate.ofInstant(it.timestamp.toInstant(), ZoneId.systemDefault()) }
                    groupedEvents.forEach { (date, eventsInDay) ->
                        item(key = date.toString()) { DateHeader(date = date)}
                    }
                    groupedEvents.forEach { (_, eventsInDay) ->
                        items(eventsInDay, key = { it.id }) { event ->
                            TimelineItem( 
                            event = event,
                                onEventClick = {  selectedEvent = event
                                    showEventDetailDialog = true },
                                onEditClick = { viewModel.setEditingEvent(event) },

                                onDeleteClick = { eventToDelete = event
                                    showDeleteConfirmDialog = true }

                            )
                         }
                        }
                    )
                }
            }
            
            // 空状态提示
            if (events.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.empty),
                        contentDescription = "Empty Timeline",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchTerm.isNotEmpty() || selectedEventType != null) {
                            "没有找到匹配的事件"
                        } else {
                            "开始添加事件到您的时间轴"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
    }
    
    // 添加事件对话框
    if (showAddEventDialog) {
        EventDialog(
            event = null,
            onDismiss = { showAddEventDialog = false },
            onSave = { eventType, description, imageUrl, attachment, eventTimestamp ->
                viewModel.addEvent(eventType, description, imageUrl, attachment, eventTimestamp)
                showAddEventDialog = false
            }
        )
    }
    
    // 编辑事件对话框
    if (showEditEventDialog && editingEvent != null) {
        EventDialog(
            event = editingEvent,
            onDismiss = { 
                viewModel.setEditingEvent(null)
                        showEditEventDialog = false
            },
            onSave = { eventType, description, imageUrl, attachment, eventTimestamp ->
                val updatedEvent = editingEvent.copy(
                    eventType = eventType,
                    title = TimelineEvent.deriveTitle(description),
                    description = description,
                    imageUrl = imageUrl,
                    attachment = attachment,
                    timestamp = eventTimestamp
                )
                viewModel.updateEvent(updatedEvent)
                showEditEventDialog = false
            }
        )
    }
    
    // 删除确认对话框
    if (showDeleteConfirmDialog && eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false 
                eventToDelete = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除事件 '${eventToDelete?.title}' 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventToDelete?.id?.let { viewModel.deleteEvent(it) }
                        showDeleteConfirmDialog = false
                        eventToDelete = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmDialog = false 
                        eventToDelete = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 事件详情对话框
    if (showEventDetailDialog && selectedEvent != null) {
        EventDetailDialog(
            event = selectedEvent!!,
            onDismiss = {
                showEventDetailDialog = false
                selectedEvent = null

            }
        })
    }
}

/**
 * 时间轴项目组件
 */
@Composable 
fun TimelineItem(event: TimelineEvent, onEventClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) { 

    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val localDateTime: LocalDateTime =
        event.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    Row(modifier = Modifier.fillMaxWidth()) {
        Canvas(
        modifier = Modifier
                .clickable {
                    onEventClick() 
                }
                .padding(top = 16.dp), onDraw = {
            drawCircle(
                color = MaterialTheme.colorScheme.primary,
                radius = 8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    size.width / 2 - 40.dp.toPx(), 0f
                )
            )
            drawLine(color = MaterialTheme.colorScheme.outline, start = androidx.compose.ui.geometry.Offset(size.width / 2 - 40.dp.toPx(), 0f), end = androidx.compose.ui.geometry.Offset(size.width / 2 - 40.dp.toPx(), size.height)) 
        })

        Card(

            modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp).clickable { onEventClick() },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 头部：类型图标、标题和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 事件类型图标
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(getEventTypeColor(event.eventType).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getEventTypeIcon(event.eventType),
                            contentDescription = null,
                            tint = getEventTypeColor(event.eventType),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // 标题
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 时间
                Text(
                    text = dateTimeFormatter.format(localDateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 描述
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            // 图片（如果有）
            if (!event.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            
            // 附件（如果有）
            event.attachment?.let { attachment ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = attachment.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // 编辑按钮
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 删除按钮
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
fun DateHeader(date: LocalDate) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE")
    val formattedDate = date.format(dateFormatter)
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(80.dp))
        Text( 
            text = formattedDate,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

/**
 * 过滤下拉菜单
 */
@Composable
fun FilterDropdownMenu(
    selectedEventType: EventType?,
    onEventTypeSelected: (EventType?) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("全部") },
            onClick = { onEventTypeSelected(null) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text("笔记") },
            onClick = { onEventTypeSelected(EventType.NOTE) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Note,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text("待办") },
            onClick = { onEventTypeSelected(EventType.TODO) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CheckBox,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text("日程") },
            onClick = { onEventTypeSelected(EventType.SCHEDULE) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null
                )
            }
        )
    }
}

/**
 * Get the correct icon from the eventType
 */
@Composable
fun getEventTypeIcon(eventType: EventType) = when (eventType) {
    EventType.NOTE -> Icons.Outlined.Note
    TODO -> Icons.Outlined.CheckBox
    SCHEDULE -> Icons.Outlined.Event
    MEMO -> Icons.Outlined.EditNote
}

/**
 * 获取事件类型对应的颜色
 */
@Composable
fun getEventTypeColor(eventType: EventType) = when (eventType) {
    EventType.NOTE -> MaterialTheme.colorScheme.tertiary
    EventType.TODO -> MaterialTheme.colorScheme.primary
    EventType.SCHEDULE -> MaterialTheme.colorScheme.secondary
    EventType.MEMO -> MaterialTheme.colorScheme.surfaceTint
}