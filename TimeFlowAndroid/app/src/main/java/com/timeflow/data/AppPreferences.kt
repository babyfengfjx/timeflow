package com.timeflow.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * Data class representing the application preferences.
 * @property showOnboarding Indicates whether the onboarding screen should be shown.
 */
data class AppPreferences(
    val showOnboarding: Boolean
) {
    /**
     * Companion object for [AppPreferences].
     */
    private companion object {
        /**
         * Returns the default preferences.
         * @return [AppPreferences] with showOnboarding set to true.
         */
        fun getDefaultInstance() = AppPreferences(true)
    }
}

/**
 * Repository class to manage application preferences using DataStore.
 * @param dataStore The DataStore instance for storing preferences.
 */
class AppPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    /**
     * Companion object for [AppPreferencesRepository].
     */
    private companion object {
        /**
         * The name of the DataStore file.
         */
        const val DATA_STORE_NAME = "app_preferences"
    }

    /**
     * Object holding the preference keys.
     */
    private object PreferencesKeys {
        /**
         * Key for the showOnboarding preference.
         */
        val SHOW_ONBOARDING = booleanPreferencesKey("show_onboarding")
    }

    /**
     * Flow of [AppPreferences] that emits whenever the preferences are updated.
     */
    val appPreferencesFlow: Flow<AppPreferences> = dataStore.data.map { preferences ->
        AppPreferences(showOnboarding = preferences[PreferencesKeys.SHOW_ONBOARDING] ?: true)
    }
    /**
     * Updates the showOnboarding preference.
     * @param showOnboarding The new value for the showOnboarding preference.
     */
    suspend fun updateShowOnboarding(showOnboarding: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ONBOARDING] = showOnboarding
        }
    }
}
