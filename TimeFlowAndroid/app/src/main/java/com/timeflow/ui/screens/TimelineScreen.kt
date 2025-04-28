package com.timeflow.ui.screens
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.rememberDismissState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.FixedThreshold
import androidx.compose.material3.swipeable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import com.timeflow.model.TimelineEvent
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import com.timeflow.ui.components.*
import com.timeflow.viewmodel.TimelineViewModel
import com.timeflow.model.EventType
import com.timeflow.R
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

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
    val snackbarHostState = remember { SnackbarHostState() }
    var draggedItem by remember { mutableStateOf<TimelineEvent?>(null) }
    var draggedItemOffset by remember { mutableStateOf(0f) }

    val listState = rememberLazyListState()

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp).width(1.dp)) {
                    drawLine(
                        color = MaterialTheme.colorScheme.outline, 
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height)
                    )
                }
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp)
                    .width(1.dp)
                ) {
                    drawLine(
                        color = MaterialTheme.colorScheme.outline, 
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height)
                    )
                }   
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress { change, dragAmount ->
                            val key = change.position
                            draggedItem = events[events.indexOf(key)]
                            draggedItemOffset += dragAmount.y

                            val draggedItemIndex = events.indexOf(draggedItem)
                            if(draggedItemIndex == -1) return@detectDragGesturesAfterLongPress
                            val newPosition = (draggedItemIndex + (draggedItemOffset / 100f).roundToInt()).coerceIn(0, events.lastIndex)


                            if(draggedItemIndex != newPosition){

                                val newList = events.toMutableList()
                                newList.add(newPosition, newList.removeAt(draggedItemIndex))
                                viewModel.setEvents(newList.toList())

                            }
                            draggedItemOffset = 0f

                            change.consumeAllChanges()



                        }


                    }, state = listState) {
                    val groupedEvents = events.groupBy { LocalDate.ofInstant(it.timestamp.toInstant(), ZoneId.systemDefault()) }
                    groupedEvents.forEach { (date, _) ->
                        item(key = date.toString()) { DateHeader(date = date)}
                    }

                    val dragDropList = remember { mutableStateOf(events.toMutableList()) }
                    var overscrollJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) } 
                    var currentIndexOfDraggedItem by remember { mutableStateOf<Int?>(null) } 
                    var currentIndexOfDragOverItem by remember { mutableStateOf<Int?>(null) }
                    groupedEvents.forEach { (_, eventsInDay) ->
                        itemsIndexed(eventsInDay, key = { _, it -> it.id }) { index, event ->
                            var itemHeight by remember { mutableStateOf(0) }
                            var itemOffsetY by remember { mutableStateOf(0) }
                            val animatedOffsetY by animateDpAsState(
                                targetValue = if (currentIndexOfDraggedItem == index) itemOffsetY.toDp() else 0.dp
                            )


                            val haptic = LocalHapticFeedback.current

                            TimelineItem(
                                event = event, onEventClick = {
                                    selectedEvent = event
                                    showEventDetailDialog = true
                                }, onEditClick = { viewModel.setEditingEvent(event) },
                                onDeleteClick = {
                                    eventToDelete = event
                                    showDeleteConfirmDialog = true
                                }, modifier = Modifier
                                    .zIndex(if (draggedItem?.id == event.id) 1f else 0f)
                                    .offset(y = draggedItemOffset.roundToInt().dp)
                                    .offset { IntOffset(0, animatedOffsetY.roundToPx()) }
                                    .pointerInput(Unit) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { offset ->
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                currentIndexOfDraggedItem = index
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val draggedItem = dragDropList.value[index]
                                                val draggedItemIndex = dragDropList.value.indexOf(draggedItem)

                                                coroutineScope.launch {
                                                    val scrollOffset = 24f
                                                    if (dragAmount.y > 0 && lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 < groupedEvents.flatMap { it.value }.lastIndex) {
                                                        overscrollJob?.cancel()
                                                        overscrollJob = launch {
                                                            lazyListState.scrollBy(scrollOffset)
                                                        }
                                                    }
                                                    if (dragAmount.y < 0 && lazyListState.firstVisibleItemIndex > 0) {
                                                        overscrollJob?.cancel()
                                                        overscrollJob = launch {
                                                            lazyListState.scrollBy(-scrollOffset)
                                                        }
                                                    }
                                                }

                                                itemOffsetY += dragAmount.y.toInt()
                                                dragDropList.value.forEachIndexed { i, item ->
                                                    if (item != draggedItem && index != i) {
                                                        val draggedItemTop = itemOffsetY.coerceAtMost(itemHeight * (draggedItemIndex))
                                                        val draggedItemBottom = itemOffsetY.coerceAtLeast(itemHeight * (draggedItemIndex + 1))

                                                        if (draggedItemTop <= itemHeight * i && draggedItemBottom >= itemHeight * i) {
                                                            currentIndexOfDragOverItem = i
                                                            reorder(dragDropList, draggedItemIndex, i)
                                                        }
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                overscrollJob?.cancel()
                                                itemOffsetY = 0
                                                currentIndexOfDraggedItem = null
                                                currentIndexOfDragOverItem = null
                                            },
                                            onDragEnd = {
                                                overscrollJob?.cancel()
                                                itemOffsetY = 0
                                                if (currentIndexOfDragOverItem != null && currentIndexOfDraggedItem != null) {
                                                    reorder(dragDropList, currentIndexOfDraggedItem!!, currentIndexOfDragOverItem!!)
                                                }
                                                currentIndexOfDraggedItem = null
                                                currentIndexOfDragOverItem = null
                                            }
                                        )
                                    }
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
                try {

                    viewModel.addEvent(eventType, description, imageUrl, attachment, eventTimestamp)
                } catch (e: Exception) {
                    e.printStackTrace()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "An error has occurred.", duration = SnackbarDuration.Short
                        )
                    }
                }

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
                try {

                    viewModel.updateEvent(updatedEvent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "An error has occurred.", duration = SnackbarDuration.Short
                        )
                    }
                }

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
                        try {
                            eventToDelete?.id?.let { viewModel.deleteEvent(it) }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "An error has occurred.", duration = SnackbarDuration.Short
                                )
                            }
                        }

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


    if (showEventDetailDialog && selectedEvent != null) {
        EventDetailDialog(
            event = selectedEvent!!,
            onDismiss = { showEventDetailDialog = false

            }
        })
    }
}

private fun reorder(list: MutableState<MutableList<TimelineEvent>>, from: Int, to: Int) {
    val newList = list.value.toMutableList()
    newList.add(to, newList.removeAt(from))
    list.value = newList
}

/**
 * TimeLine item
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable  
fun TimelineItem(event: TimelineEvent, onEventClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit, modifier: Modifier) {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val localDateTime: LocalDateTime = event.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf( 
        -100.dp.toPx() to -1,
        0f to 0, 
        100.dp.toPx() to 1
    )
    val offset = swipeableState.offset.value
val dismissState = rememberDismissState(
    confirmStateChange = {
        if (it == DismissValue.DismissedToStart) {
            onDeleteClick()
            true
        } else {
            false
        }
    }
    )
    SwipeToDismiss(
        state = dismissState,
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.LightGray
                    DismissValue.DismissedToEnd -> Color.Green
                    DismissValue.DismissedToStart -> Color.Red
                }, label = ""
            )
            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
            }
            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.Edit
                DismissDirection.EndToStart -> Icons.Default.Delete
            }
            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f, label = ""
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    icon,
                    contentDescription = "Localized description",
                    modifier = Modifier.scale(scale)
                    )
            }
        },
        dismissContent = {
            Box(modifier = modifier.then(Modifier.fillMaxWidth())) {
                Row(modifier = Modifier.fillMaxWidth().clickable {onEventClick()}) {
                    Canvas(modifier = Modifier.padding(top = 16.dp), onDraw = {
                        drawCircle(
                            color = MaterialTheme.colorScheme.primary, radius = 8.dp.toPx(), center = Offset(size.width / 2 - 40.dp.toPx(), 0f)
                        )
                        drawLine(color = MaterialTheme.colorScheme.outline, start = Offset(size.width / 2 - 40.dp.toPx(), 0f), end = Offset(size.width / 2 - 40.dp.toPx(), size.height))
                    })
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {onEventClick()}, shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = CenterVertically) {
                                Row(verticalAlignment = CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(getEventTypeColor(event.eventType).copy(alpha = 0.1f)), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                        Icon(imageVector = getEventTypeIcon(event.eventType), contentDescription = null, tint = getEventTypeColor(event.eventType), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = event.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(text = dateTimeFormatter.format(localDateTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = event.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                            if (!event.imageUrl.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)))
                            }
                            event.attachment?.let { attachment ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(12.dp), verticalAlignment = CenterVertically) {
                                    Icon(imageVector = Icons.Outlined.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = attachment.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        },
        directions = setOf(DismissDirection.EndToStart),
    )
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE")
    val formattedDate = date.format(dateFormatter)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically
    ) {
        Spacer(modifier = Modifier.width(80.dp))
        Text(text = formattedDate, style = MaterialTheme.typography.headlineSmall)
    }
}

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
                    // Image
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
                    // Attachment
                    event.attachment?.let { attachment ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier.fillMaxWidth()
                        , horizontalArrangement = Arrangement.End , verticalAlignment = Alignment.CenterVertically
                    ) {
                    }
                }
            }
        }
    }
        
        
    }
        
        
    }
}


@Composable
fun DateHeader(date: LocalDate) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE")
    val formattedDate = date.format(dateFormatter)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
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
        DropdownMenuItem(
            text = { Text("备忘录") },
            onClick = { onEventTypeSelected(EventType.MEMO) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.EditNote,
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
    EventType.TODO -> Icons.Outlined.CheckBox
    EventType.SCHEDULE -> Icons.Outlined.Event
    EventType.MEMO -> Icons.Outlined.EditNote
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