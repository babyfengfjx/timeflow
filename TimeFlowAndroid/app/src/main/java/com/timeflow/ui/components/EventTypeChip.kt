package com.timeflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event // Import Icons.Outlined.Event
import androidx.compose.material.icons.outlined.Note // Import Icons.Outlined.Note
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically // Import CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import com.timeflow.model.EventType
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 事件类型选择芯片
 */
@Composable
fun EventTypeChip(
    eventType: EventType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    /**
     * 根据事件类型获取对应的图标
     *
     * @param eventType 事件类型
     * @return 图标
     */
    val icon = when (eventType) {
        EventType.NOTE -> Icons.Outlined.Note
        EventType.TODO -> Icons.Outlined.CheckBox
        EventType.SCHEDULE -> Icons.Outlined.Event
        EventType.MEMO -> Icons.Outlined.EditNote
    }

    /**
     * 根据事件类型获取对应的标签
     *
     * @param eventType 事件类型
     * @return 标签
     */
    val label = when (eventType) {
        EventType.NOTE -> "笔记"
        EventType.TODO -> "待办"
        EventType.SCHEDULE -> "日程"
        EventType.MEMO -> "备忘录"
    }

    /**
     * 根据事件类型和是否选中来决定背景颜色
     *
     * @param eventType 事件类型
     * @return 背景颜色
     */
    val containerColor = if (isSelected) {
        when (eventType) {
            EventType.NOTE -> MaterialTheme.colorScheme.tertiary
            EventType.TODO -> MaterialTheme.colorScheme.primary
            EventType.SCHEDULE -> MaterialTheme.colorScheme.secondary,
            EventType.MEMO -> MaterialTheme.colorScheme.surfaceTint,
        },
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    /**
     * 根据是否选中来决定文本颜色
     *
     * @param isSelected 是否选中
     * @return 文本颜色
     */
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        // 设置组件大小和点击事件
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onClick) ,
        // 设置组件形状
        shape = RoundedCornerShape(20.dp),
        // 设置组件背景颜色
        color = containerColor,
        // 设置组件文本颜色
        contentColor = contentColor,
    ) {
        // 水平布局
        Row(
            // 设置内边距
            modifier = Modifier.padding(horizontal = 12.dp) ,
            // 设置垂直居中
            verticalAlignment = CenterVertically ,
            // 设置水平居中
            horizontalArrangement = Arrangement.Center ,
        ) {
            // 图标
            Icon(
                // 图标
                imageVector = icon,
                // 无内容描述
                contentDescription = null,
                // 设置大小
                modifier = Modifier.size(16.dp) ,
            ) ,
            // 间隔
            Spacer(modifier = Modifier.width(4.dp))
            // 文本
            Text(
                // 文本内容
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
```

```kotlin
package com.timeflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent

/**
 * 事件编辑对话框，用于添加和编辑事件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDialog(
    event: TimelineEvent?,
    onDismiss: () -> Unit,
    onSave: (EventType, String, String?, TimelineEvent.Attachment?) -> Unit
) {
    val isNewEvent = event == null

    var selectedEventType by remember { mutableStateOf(event?.eventType ?: EventType.NOTE) }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var imageUrl by remember { mutableStateOf(event?.imageUrl) }
    var attachment by remember { mutableStateOf(event?.attachment) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 标题
                Text(
                    text = if (isNewEvent) "添加新事件" else "编辑事件",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 事件类型选择
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
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    CircleShape
                                )
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
                        onClick = { onSave(selectedEventType, description, imageUrl, attachment) },
                        enabled = description.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}
```

```kotlin
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