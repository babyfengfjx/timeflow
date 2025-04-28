package com.timeflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
/**
 * Onboarding Screen composable function
 *
 * This screen is displayed to new users to introduce them to the app.
 *
 * @param onGetStarted Lambda to be called when the "Get Started" button is clicked.
 */
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    // Column to center the content
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome message
        Text(text = "Welcome to TimeFlow!")
        // Get started button
        Button(
            // Callback when the button is clicked
            onClick = onGetStarted,
            // Add a padding
            modifier = Modifier.padding(top = 16.dp)
        ) {
            // Text of the button
            Text(text = "Get Started")
        }
    }