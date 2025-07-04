package com.win11launcher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Win11LauncherApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}