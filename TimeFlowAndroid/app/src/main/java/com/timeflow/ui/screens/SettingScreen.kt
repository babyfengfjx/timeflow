package com.timeflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Dialog for displaying and modifying app settings.
 */
@AndroidEntryPoint
@Composable
fun SettingDialog(
    // Lambda to be called when the dialog is dismissed.
    onDismiss: () -> Unit,
    // Lambda to be called when the theme changes. Receives a boolean indicating if dark theme is enabled.
    onThemeChange: (Boolean) -> Unit,
    // Boolean indicating if the dark theme is enabled.
    isDarkTheme: Boolean
) {
    // The AlertDialog composable displays a dialog to the user.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setting") }, // The title of the dialog.
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Dark Theme", modifier = Modifier.weight(1f))
                    // Switch to enable or disable dark theme.
                    Switch(
                        checked = isDarkTheme, // Current state of the switch.
                        onCheckedChange = { onThemeChange(it) } // Called when the switch state changes.
                    )
                }
            }
        },
        confirmButton = {
            // Button to close the dialog.
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}