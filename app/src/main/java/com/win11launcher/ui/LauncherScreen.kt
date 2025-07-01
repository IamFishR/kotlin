package com.win11launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.win11launcher.data.AppRepository
import com.win11launcher.ui.components.AllAppsScreen
import com.win11launcher.ui.components.NotificationPanel
import com.win11launcher.ui.components.StartMenu
import com.win11launcher.ui.components.Taskbar
import com.win11launcher.ui.components.WallpaperBackground
import com.win11launcher.ui.screens.NotesHubScreen
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
                    onAllAppsClick = {
                        showStartMenu = false
                        showAllApps = true
                    },
                    onNotesHubClick = {
                        showStartMenu = false
                        showNotesHub = true
                    }
                )
            }
        }
        
        // Notification panel
        NotificationPanel(
            showPanel = showNotificationPanel,
            systemStatus = systemStatus,
            onDismiss = { showNotificationPanel = false }
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
            onSystemTrayClick = { 
                showNotificationPanel = !showNotificationPanel
                showStartMenu = false
            }
        )
    }
}