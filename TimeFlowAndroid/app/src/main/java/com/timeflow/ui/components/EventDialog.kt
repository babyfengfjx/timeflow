package com.timeflow.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.timeflow.model.EventType
import com.timeflow.model.EventType.*
import com.timeflow.model.TimelineEvent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date


/**
 * 事件编辑对话框，用于添加和编辑事件
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDialog(
    event: TimelineEvent?,
    onDismiss: () -> Unit,
    onSave: (EventType, String, String?, TimelineEvent.Attachment?, Date) -> Unit
) {    
    // State variable to manage the selected date
    var selectedDate by remember { mutableStateOf(event?.timestamp?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()) }
    // State variable to manage the date picker visibility
    var showDatePicker by remember { mutableStateOf(false) }
    // Check if it is a new event
    val isNewEvent = event == null

    // State variable to manage the selected event type
    var selectedEventType by remember { mutableStateOf(event?.eventType ?: EventType.NOTE) }
    // State variable to manage the description
    var description by remember { mutableStateOf(event?.description ?: "") }
    // State variable to manage the image url
    var imageUrl by remember { mutableStateOf(event?.imageUrl) }
    // State variable to manage the attachment
    var attachment by remember { mutableStateOf(event?.attachment) }

    // Dialog to show the event
    Dialog(
        // Callback to dismiss the dialog
        onDismissRequest = onDismiss,
        // Properties of the dialog
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        // Surface of the dialog
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            // Content of the dialog
             Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title of the dialog
                Text(
                    text = if (isNewEvent) "添加新事件" else "编辑事件",
                    style = MaterialTheme.typography.headlineSmall
                )                

                Spacer(modifier = Modifier.height(16.dp))

                // Event Type
                Text(
                    text = "事件类型",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventTypeChip(
                        eventType = NOTE,
                        isSelected = selectedEventType == EventType.NOTE,
                        onClick = { selectedEventType = NOTE }
                      )
                    EventTypeChip(
                        eventType = TODO,
                        isSelected = selectedEventType == EventType.TODO,
                        onClick = { selectedEventType = TODO },
                    )
                    EventTypeChip(
                        eventType = SCHEDULE,
                        isSelected = selectedEventType == EventType.SCHEDULE,
                        onClick = { selectedEventType = SCHEDULE }
                    )
                    EventTypeChip(
                        eventType = MEMO,
                        isSelected = selectedEventType == EventType.SCHEDULE,
                        onClick = { selectedEventType = EventType.SCHEDULE }
                      )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // 描述输入
                Text(
                    text = "描述",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入事件描述...") },
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 图片预览和上传按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "图片",
                        style = MaterialTheme.typography.labelLarge
                    )
                    // 在实际应用中，这里应该有图片选择功能
                    IconButton(onClick = { /* 选择图片 */ }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "添加图片")
                    }
                }                

                // 图片预览（如果有）
                if (!imageUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        // 删除图片按钮
                        IconButton(
                            onClick = { imageUrl = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "删除图片",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date select
                Row() {
                    Button(onClick = { showDatePicker = true }) {
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }                    
                }
                
                // Date picker
                if (showDatePicker) {
                    DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { Button(onClick = { showDatePicker = false }) { Text(text = "OK") } },) {
                        DatePicker(onDateSelected = { selectedDate = it })                        
                    }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // 附件预览和上传按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "附件",
                        style = MaterialTheme.typography.labelLarge
                    )
                    // 在实际应用中，这里应该有文件选择功能
                    IconButton(onClick = { /* 选择附件 */ }) {                        
                       Icon(Icons.Default.AttachFile, contentDescription = "添加附件")
                    }
                }
                
                // 附件预览（如果有）
                attachment?.let { att ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = att.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        // 删除附件按钮
                        IconButton(
                            onClick = { attachment = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "删除附件",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                  TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(selectedEventType, description, imageUrl, attachment, Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())) },
                        enabled = description.isNotBlank()
                    ) {
                        // Text of the save button
                        Text("保存")
                    }
                }
            }
        }
    }
}

