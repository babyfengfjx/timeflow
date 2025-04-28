package com.timeflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.time.format.DateTimeFormatter
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent
import com.timeflow.ui.components.EventTypeChip

/**
 * 快速添加事件表单组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddEventForm(
    onAddEvent: (EventType, String, Date, String?, TimelineEvent.Attachment?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedEventType by remember { mutableStateOf(EventType.NOTE) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            Text(
                text = "快速添加事件",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 事件类型
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventTypeChip(
                    eventType = EventType.NOTE,
                    isSelected = selectedEventType == EventType.NOTE,
                    onClick = { selectedEventType = EventType.NOTE }
                )
                EventTypeChip(
                    eventType = EventType.TODO,
                    isSelected = selectedEventType == EventType.TODO,
                    onClick = { selectedEventType = EventType.TODO }
                )
                EventTypeChip(
                    eventType = EventType.SCHEDULE,
                    isSelected = selectedEventType == EventType.SCHEDULE,
                    onClick = { selectedEventType = EventType.SCHEDULE }
                )
                EventTypeChip(
                    eventType = EventType.MEMO,
                    isSelected = selectedEventType == EventType.MEMO,
                    onClick = { selectedEventType = EventType.MEMO }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 输入
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { showDatePicker = true }) {
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                    },
                ) {
                    val datePickerState = rememberDatePickerState()
                    DatePicker(state = datePickerState)
                    selectedDate = LocalDate.ofEpochDay(datePickerState.selectedDateMillis!! / (24 * 60 * 60 * 1000))
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入事件描述...") },
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            val eventTimestamp = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                            onAddEvent(selectedEventType, description, eventTimestamp, null, null)
                            description = ""
                        }
                    },
                    enabled = description.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,modifier = Modifier.size(18.dp)

                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加")
                }
            }
        }
    }