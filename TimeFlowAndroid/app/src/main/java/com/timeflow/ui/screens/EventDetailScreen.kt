package com.timeflow.ui.components
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width // Import width for Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.dp
import java.time.LocalDate // Import LocalDate for date handling
import java.time.ZoneId // Import ZoneId for time zone conversion
import java.util.Date // Import Date for timestamp
import java.time.format.DateTimeFormatter // Import DateTimeFormatter for date formatting
import com.timeflow.model.EventType
import com.timeflow.model.TimelineEvent

/**
 * Composable function for the quick add event form.
 * Allows users to quickly add new events with minimal input.
 * @param onAddEvent Lambda function to be called when the user adds a new event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddEventForm(
    onAddEvent: (EventType, String, Date, String?, TimelineEvent.Attachment?) -> Unit
) {
    // State to hold the description of the event
    var description by remember { mutableStateOf("") }
    // State to hold the selected event type (default to NOTE)
    var selectedEventType by remember { mutableStateOf(EventType.NOTE) }
    // State to hold the selected date (default to today)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    // State to control the visibility of the DatePicker dialog
    var showDatePicker by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        // Main column for the form content
        Column(modifier = Modifier.padding(16.dp)) {
            // Title text
            Text(
                text = "快速添加事件",
                style = MaterialTheme.typography.titleMedium
            )
            // Vertical space
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
            // Vertical space
            Spacer(modifier = Modifier.height(12.dp))

            // Row for date button
            Row(modifier = Modifier.fillMaxWidth()) {
                // Button to open the date picker dialog
                Button(onClick = { showDatePicker = true }) {
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
            }
            // Date Picker dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                    },
                ) {
                    val datePickerState = rememberDatePickerState()
                    DatePicker(state = datePickerState)
                    // Update the selected date when the user picks a date
                    selectedDate = LocalDate.ofEpochDay(datePickerState.selectedDateMillis!! / (24 * 60 * 60 * 1000))
                }
            }
            // Input field for event description
            OutlinedTextField(
                value = description,// Value of the input field
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入事件描述...") },
                minLines = 2,
                maxLines = 4
            )

            // Vertical space
            Spacer(modifier = Modifier.height(12.dp))

            // Row for the add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Add event button
                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            // Create a Date object from the selected LocalDate
                            val eventTimestamp = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                            onAddEvent(selectedEventType, description, eventTimestamp, null, null)
                            description = ""
                        }
                    },
                    enabled = description.isNotBlank() // Disable button if description is empty
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