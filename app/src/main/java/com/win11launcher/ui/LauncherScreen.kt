package com.win11launcher.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.win11launcher.data.AppRepository
import com.win11launcher.ui.components.AllAppsScreen
import com.win11launcher.ui.components.CommandPrompt
import com.win11launcher.ui.components.NotificationPanel
import com.win11launcher.ui.components.StartMenu
import com.win11launcher.ui.components.Taskbar
import com.win11launcher.ui.components.WallpaperBackground
import com.win11launcher.ui.screens.NotesHubScreen
import com.win11launcher.ui.screens.SettingsScreen
import com.win11launcher.utils.SystemStatusManager
import com.win11launcher.utils.rememberWallpaperManager

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    val appRepository = remember { AppRepository(context) }
    val systemStatusManager = remember { SystemStatusManager(context) }
    val wallpaperManager = rememberWallpaperManager()
    
    var showStartMenu by remember { mutableStateOf(false) }
    var showAllApps by remember { mutableStateOf(false) }
    var showNotificationPanel by remember { mutableStateOf(false) }
    var showNotesHub by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showCommandPrompt by remember { mutableStateOf(false) }
    
    // Start monitoring system status
    LaunchedEffect(Unit) {
        systemStatusManager.startMonitoring()
    }
    
    // Refresh wallpaper when permission might be granted
    LaunchedEffect(Unit) {
        if (wallpaperManager.hasPermission() && wallpaperManager.getWallpaper() == null) {
            wallpaperManager.refreshWallpaper()
        }
    }
    
    // Stop monitoring when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            systemStatusManager.stopMonitoring()
        }
    }
    
    val systemStatus by systemStatusManager.systemStatus
    
    WallpaperBackground(
        wallpaper = wallpaperManager.getWallpaper(),
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            showSettings -> {
                SettingsScreen(
                    systemStatusManager = systemStatusManager,
                    onNavigateBack = { showSettings = false }
                )
            }
            showNotesHub -> {
                NotesHubScreen(
                    onNavigateBack = { showNotesHub = false }
                )
            }
            showAllApps -> {
                AllAppsScreen(
                    appRepository = appRepository,
                    onBackClick = { 
                        showAllApps = false
                        showStartMenu = true
                    }
                )
            }
            showStartMenu -> {
                StartMenu(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 56.dp)
                        .width(600.dp)
                        .height(680.dp),
                    onDismiss = { showStartMenu = false },
                    onNotesHubClick = {
                        showStartMenu = false
                        showNotesHub = true
                    },
                )
            }
        }
        
        // Settings icon in top left corner
        if (!showSettings && !showNotesHub && !showAllApps) {
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Notification panel
        NotificationPanel(
            showPanel = showNotificationPanel,
            systemStatus = systemStatus,
            systemStatusManager = systemStatusManager,
            onDismiss = { showNotificationPanel = false }
        )
        
        // Command prompt
        CommandPrompt(
            isVisible = showCommandPrompt,
            onDismiss = { showCommandPrompt = false }
        )
        
        Taskbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp),
            systemStatus = systemStatus,
            onStartClick = { 
                showStartMenu = !showStartMenu
                showNotificationPanel = false
            },
            onCommandClick = {
                showCommandPrompt = true
                showStartMenu = false
                showNotificationPanel = false
            },
            onSystemTrayClick = { 
                showNotificationPanel = !showNotificationPanel
                showStartMenu = false
            }
        )
    }
}