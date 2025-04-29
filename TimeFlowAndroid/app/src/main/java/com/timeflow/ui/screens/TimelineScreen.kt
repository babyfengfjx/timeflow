package com.timeflow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.timeflow.R
import com.timeflow.model.TimelineEvent
import com.timeflow.viewmodel.TimelineViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * TimelineScreen composable function.
 * This function is responsible for displaying the timeline,
 * including the list of events, search, and filter functionalities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    // Load events when the screen is first launched.
    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    // Collect events from the ViewModel.
    val events by viewModel.events.collectAsState(initial = emptyList())
    // Collect the search term from the ViewModel.
    val searchTerm by viewModel.searchTerm.collectAsState()
    // Get the selected event type from the ViewModel.
    val selectedEventType = viewModel.selectedEventType
    // Get the event being edited from the ViewModel.
    val editingEvent = viewModel.editingEvent
    // State to control the visibility of the event detail dialog.
    var showEventDetailDialog by remember { mutableStateOf(false) }
    // State to store the currently selected event.
    var selectedEvent by remember { mutableStateOf<TimelineEvent?>(null) }
    // State to control the visibility of the settings dialog.
    var showSettingDialog by remember { mutableStateOf(false) }
    // State to control the visibility of the add event dialog.
    var showAddEventDialog by remember { mutableStateOf(false) }
    // State to control the visibility of the edit event dialog.
    var showEditEventDialog by remember { mutableStateOf(false) }
    // State to control the visibility of the delete confirmation dialog.
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    // State to store the event to be deleted.
    var eventToDelete by remember { mutableStateOf<TimelineEvent?>(null) }
    // State to control the search bar expansion.
    var searchExpanded by remember { mutableStateOf(false) }
    // State to control the visibility of the filter menu.
    var showFilterMenu by remember { mutableStateOf(false) }

    // State to manage the Snackbar.
    val snackbarHostState = remember { SnackbarHostState() }

    // State for drag and drop.
    var draggedItem by remember { mutableStateOf<TimelineEvent?>(null) }
    var draggedItemOffset by remember { mutableStateOf(0f) }
    // State of the LazyColumn
    val listState = rememberLazyListState()

    // Animation states
    val searchBarHeight by animateDpAsState(
        targetValue = if (searchExpanded) 56.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val filterMenuScale by animateFloatAsState(
        targetValue = if (showFilterMenu) 1f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    Scaffold(
        topBar = {
            // Top app bar of the screen.
            TopAppBar(
                title = { 
                    Text(
                        "TimeFlow",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    // Theme toggle button with animation
                    IconButton(
                        onClick = { onThemeChange(!isDarkTheme) }
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme"
                        )
                    }

                    // Settings button
                    IconButton(onClick = { showSettingDialog = true }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "设置")
                    }

                    // Search button with animation
                    IconButton(
                        onClick = { searchExpanded = !searchExpanded }
                    ) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }

                    // Filter button with animation
                    IconButton(
                        onClick = { showFilterMenu = !showFilterMenu }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "过滤",
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (showFilterMenu) 180f else 0f
                            }
                        )
                    }
                }
            )
        },
        // Floating action button to add a new event.
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Animated search bar
            AnimatedVisibility(
                visible = searchExpanded,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
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

            // Animated filter menu
            AnimatedVisibility(
                visible = showFilterMenu,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FilterDropdownMenu(
                    selectedEventType = selectedEventType,
                    onEventTypeSelected = {
                        viewModel.setEventTypeFilter(it)
                        showFilterMenu = false
                    },
                    onDismiss = { showFilterMenu = false }
                )
            }

            // Timeline
            Box(modifier = Modifier.fillMaxSize()) {
                //Vertical Line
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp)
                    .width(1.dp)) {
                    drawLine(color = MaterialTheme.colorScheme.outline, start = Offset(0f, 0f), end = Offset(0f, size.height))
                }
                //LazyColumn
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp)
                    .width(1.dp)
                ) { // Vertical Line
                    drawLine(
                        color = MaterialTheme.colorScheme.outline,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height)
                    )
                }   
                LazyColumn(modifier = Modifier
                    //Fill all size
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    //Detect when the user start dragging an item
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress { change, dragAmount ->
                            val key = change.position
                            draggedItem = events[events.indexOf(key)]
                            draggedItemOffset += dragAmount.y

                            val draggedItemIndex = events.indexOf(draggedItem)
                            if(draggedItemIndex == -1) return@detectDragGesturesAfterLongPress
                            val newPosition = (draggedItemIndex + (draggedItemOffset / 100f).roundToInt()).coerceIn(0, events.lastIndex)

                            //Check if the item is moved
                            if(draggedItemIndex != newPosition){
                                //Add the new item in the list
                                val newList = events.toMutableList()
                                newList.add(newPosition, newList.removeAt(draggedItemIndex))
                                events = newList
                                viewModel.updateEvents(newList)
                            }
                            //Reset the offset
                            draggedItemOffset = 0f
                            change.consumeAllChanges() 
                        }
                    }, state = listState) {
                    val groupedEvents = events.groupBy { LocalDate.ofInstant(it.timestamp.toInstant(), ZoneId.systemDefault()) }
                    groupedEvents.forEach { (date, _) -> 
                        item(key = date.toString()) { DateHeader(date = date) }
                    }
                    groupedEvents.forEach { (_, eventsInDay) -> 
                        itemsIndexed(eventsInDay, key = { _, it -> it.id }) { _, event ->
                            TimelineItem(event = event,
                                onEventClick = {
                                    selectedEvent = event
                                    showEventDetailDialog = true
                                },
                                onEditClick = { viewModel.setEditingEvent(event) },
                                onDeleteClick = {
                                    eventToDelete = event
                                    showDeleteConfirmDialog = true
                                },
                                modifier = Modifier
                                    // Change zIndex to show the dragged item above the other items
                                    .zIndex(if (draggedItem?.id == event.id) 1f else 0f)
                                    //Move the item in the y axis
                                    .offset(y = draggedItemOffset.roundToInt().dp))
                        }
                    }
                    }
                }

            // Show the empty state when the list is empty
            if (events.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Empty Image
                    Image(
                        painter = painterResource(id = R.drawable.empty),
                        contentDescription = "Empty Timeline",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Text to show in the empty state
                    Text(
                        text = "No events yet. Click the + button to add a new event.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Add event dialog.
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
    
    // Edit event dialog.
    if (showEditEventDialog && editingEvent != null) {
        // Edit event dialog.
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
    
    // Show delete confirmation dialog.
   if (showDeleteConfirmDialog && eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false 
                eventToDelete = null
            },
            title = { Text("确认删除") }, 
            text = { Text("确定要删除事件 '${eventToDelete?.title}' 吗？") },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        // Delete the event from the ViewModel.
                        eventToDelete?.id?.let { viewModel.deleteEvent(it) }
                    } catch (e: Exception) {
                        // Print stacktrace
                            e.printStackTrace()
                            coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                    "An error has occurred.", duration = SnackbarDuration.Short
                                )
                        eventToDelete?.id?.let { viewModel.deleteEvent(it) }
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
            onDismiss = { showEventDetailDialog = false }
        )
    }

    if (showSettingDialog) {
        SettingDialog(
            onDismiss = { showSettingDialog = false },
            onThemeChange = onThemeChange,
            isDarkTheme = isDarkTheme
        )
    }
}

/**
* Reorder the item.
*/
private fun reorder(list: MutableState<MutableList<TimelineEvent>>, from: Int, to: Int) {
    val newList = list.value.toMutableList()
    newList.add(to, newList.removeAt(from))
}

/**
 * TimeLine item
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable  
fun TimelineItem(event: TimelineEvent, onEventClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit, modifier: Modifier) {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val localDateTime: LocalDateTime = event.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    //Dismiss state of the item
    val dismissState = rememberDismissState(initialValue = DismissValue.Default)
    
    if (dismissState.isDismissed(DismissDirection.EndToStart)) {
        onDeleteClick()
    }

    SwipeToDismiss(
        state = dismissState,
        modifier = Modifier
            .padding(vertical = 4.dp),
        directions = setOf(DismissDirection.EndToStart),
        dismissContent = {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                ) {
                }
            },
            background = {
    SwipeToDismiss(
        state = dismissState,
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color by animateColorAsState(
                label = ""
            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
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
                DismissDirection.EndToStart -> Icons.Filled.Trash
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
                    imageVector = icon,
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

/**
 * FilterDropdownMenu composable function.
 * This function is responsible for displaying a dropdown menu to filter the events.
 */
@Composable
fun FilterDropdownMenu(
    selectedEventType: EventType?,
    onEventTypeSelected: (EventType?) -> Unit,
    onDismiss: () -> Unit
) { // Filter dropdown menu.
    DropdownMenu(expanded = true, onDismissRequest = onDismiss) {
        DropdownMenuItem(text = { Text("全部") }, onClick = { onEventTypeSelected(null) }, leadingIcon = { Icon(imageVector = Icons.Default.List, contentDescription = null) })
        DropdownMenuItem(text = { Text("笔记") }, onClick = { onEventTypeSelected(EventType.NOTE) }, leadingIcon = { Icon(imageVector = Icons.Default.Note, contentDescription = null) })
        DropdownMenuItem(text = { Text("待办") }, onClick = { onEventTypeSelected(EventType.TODO) }, leadingIcon = { Icon(imageVector = Icons.Default.CheckBox, contentDescription = null) })
        DropdownMenuItem(text = { Text("日程") }, onClick = { onEventTypeSelected(EventType.SCHEDULE) }, leadingIcon = { Icon(imageVector = Icons.Default.Event, contentDescription = null) })
        DropdownMenuItem(text = { Text("备忘录") }, onClick = { onEventTypeSelected(EventType.MEMO) }, leadingIcon = { Icon(imageVector = Icons.Outlined.EditNote, contentDescription = null) })
    }

}

/**
 * Get the correct icon from the eventType.
 */
@Composable
fun getEventTypeIcon(eventType: EventType) = when (eventType) {
    EventType.NOTE -> Icons.Outlined.Note
    EventType.TODO -> Icons.Outlined.CheckBox 
    EventType.SCHEDULE -> Icons.Outlined.Event
    EventType.MEMO -> Icons.Outlined.EditNote
    EventType.SCHEDULE -> Icons.Outlined.Event
    EventType.MEMO -> Icons.Outlined.EditNote
}

/**
 * Get the color from the eventType.
 */
@Composable
fun getEventTypeColor(eventType: EventType) = when (eventType) {
    EventType.NOTE -> MaterialTheme.colorScheme.tertiary
    EventType.MEMO -> MaterialTheme.colorScheme.surfaceTint
    EventType.SCHEDULE -> MaterialTheme.colorScheme.secondary
    EventType.MEMO -> MaterialTheme.colorScheme.surfaceTint
}