package com.timeflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * [TimeFlowApplication] class.
 * This class extends [Application] and is used to initialize Hilt in the application.
 */
@HiltAndroidApp
class TimeFlowApplication : Application()
