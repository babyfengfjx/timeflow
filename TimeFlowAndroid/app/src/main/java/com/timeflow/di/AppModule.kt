package com.timeflow.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.timeflow.data.AppPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.app.Application
import androidx.datastore.preferences.core.Preferences

import javax.inject.Singleton

/**
 * AppModule
 * This module provides dependencies at the application level using Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the DataStore instance.
     * @param context The application context.
     * @return A singleton instance of DataStore<Preferences>.
     */
    @Singleton
    @Provides
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("app_preferences") })
    }

    /**
     * Provides the AppPreferencesRepository instance.
     * @param dataStore The DataStore instance.
     * @return A singleton instance of AppPreferencesRepository.
     */
    @Singleton
    @Provides
    fun provideAppPreferencesRepository(
        dataStore: DataStore<Preferences>
    ): AppPreferencesRepository =
        AppPreferencesRepository(dataStore)

    /**
     * Provides the application context.
     * @param application The application instance.
     * @return The application context.
     */
    @Provides
    @ApplicationContext
    fun provideContext(application: Application): Context = application.applicationContext
}