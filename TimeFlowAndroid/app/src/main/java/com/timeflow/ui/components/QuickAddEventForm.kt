package com.timeflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Date

/**
 * 快速添加事件表单组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddEventForm(
    onAddEvent: (EventType, String, Date, String?, TimelineEvent.Attachment?) -> Unit,
) {
    // 事件描述
    var description by remember { mutableStateOf("") }
    // 事件类型
    var selectedEventType by remember { mutableStateOf(EventType.NOTE) }
    // 选择的日期
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    // 是否显示日期选择器
    var showDatePicker by remember { mutableStateOf(false) }
    /**
     * Card Component
     * @param modifier The modifier to be applied to the card.
     * @param shape The shape of the card.
     * @param elevation The elevation of the card.
     * @param content The content of the card.
     */
    Card(
        modifier = Modifier.then(
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            /** Title Text */
            Text(
                text = "快速添加事件",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            /** Row to show the different `EventType` */
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
            /** Row to show the selected Date and the button to open the DatePicker */
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { showDatePicker = true }) {
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
            }

            /** Show the DatePicker */
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                    },
                ) {
                    val datePickerState = rememberDatePickerState()
                    DatePicker(state = datePickerState)
                    selectedDate =
                        LocalDate.ofEpochDay(datePickerState.selectedDateMillis!! / (24 * 60 * 60 * 1000))
                }
            }

            OutlinedTextField(
                /** Field to add the event description */
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入事件描述...") },
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 按钮
            /** Button to add a new Event */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            val eventTimestamp =
                                Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                            onAddEvent(selectedEventType, description, eventTimestamp, null, null,)
                            description = ""
                        }
                    },
                    enabled = description.isNotBlank(),
                ) {
                    /**
                     * Add icon and text
                     */
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