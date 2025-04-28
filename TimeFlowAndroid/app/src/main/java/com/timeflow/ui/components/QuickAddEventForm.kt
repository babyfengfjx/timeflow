package com.timeflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent

/**
 * 快速添加事件表单组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddEventForm(
    onAddEvent: (EventType, String, String?, TimelineEvent.Attachment?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedEventType by remember { mutableStateOf(EventType.NOTE) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            Text(
                text = "快速添加事件",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 事件类型选择
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
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 描述输入
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入事件描述...") },
                minLines = 2,
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 添加按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            onAddEvent(selectedEventType, description, null, null)
                            description = ""
                        }
                    },
                    enabled = description.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加")
                }
            }
        }
    }
}