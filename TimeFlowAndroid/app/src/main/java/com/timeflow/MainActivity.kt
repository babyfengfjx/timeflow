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
            val viewModel: TimelineViewModel = viewModel() // Gets the TimelineViewModel.
            var showOnboarding by remember { mutableStateOf(false) } // State to control the onboarding visibility.
            var isDarkTheme by remember { mutableStateOf(false) } // State to control the dark theme.
            // Collects the app preferences and updates the showOnboarding state.
            lifecycleScope.launch {
                appPreferencesRepository.appPreferencesFlow.collect { appPreferences ->
                    showOnboarding = appPreferences.showOnboarding
                }
            }
            // Applies the selected theme to the app.
            TimeFlowTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Shows the OnboardingScreen if showOnboarding is true, otherwise shows the TimelineScreen.
                    if (showOnboarding) {
                        OnboardingScreen(onGetStarted = { lifecycleScope.launch { appPreferencesRepository.updateShowOnboarding(false) } })
                    } else {
                        TimelineScreen(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it }
                        )
                    }

                }
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