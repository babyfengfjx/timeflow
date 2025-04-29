package com.timeflow

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timeflow.data.AppPreferencesRepository
import com.timeflow.ui.screens.TimelineScreen
import com.timeflow.ui.screens.OnboardingScreen
import com.timeflow.ui.theme.TimeFlowTheme
import com.timeflow.viewmodel.TimelineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme

/**
 * Main activity of the application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Injects the repository for app preferences.
    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    /**
     * Called when the activity is starting.
     * Initializes the UI and sets up the app's initial state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme()) }
            
            TimeFlowTheme(darkTheme = isDarkTheme) {
                TimelineScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }
}

/**
 * Default preview for the app.
 */
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TimeFlowTheme() {
        val viewModel: TimelineViewModel = viewModel()
        var isDarkTheme by remember { mutableStateOf(false) }
        TimelineScreen(viewModel = viewModel, isDarkTheme = isDarkTheme, onThemeChange = { isDarkTheme = it })
    }
}