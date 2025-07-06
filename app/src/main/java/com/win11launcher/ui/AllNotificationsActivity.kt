package com.win11launcher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.win11launcher.ui.screens.AllNotificationsScreen
import com.win11launcher.ui.theme.Win11LauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllNotificationsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            Win11LauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AllNotificationsScreen(
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}