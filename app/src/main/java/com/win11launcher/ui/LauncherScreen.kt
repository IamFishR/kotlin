package com.win11launcher.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.win11launcher.data.AppRepository
import com.win11launcher.ui.components.AllAppsScreen
import com.win11launcher.ui.components.CommandPrompt
import com.win11launcher.ui.components.NotificationPanel
import com.win11launcher.ui.components.StartMenu
import com.win11launcher.ui.components.Taskbar
import com.win11launcher.ui.components.WallpaperBackground
import com.win11launcher.ui.layout.*
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
    var showSettings by remember { mutableStateOf(false) }
    var showCommandPrompt by remember { mutableStateOf(false) }
    var showNotificationPanel by remember { mutableStateOf(false) }
    
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
    val density = LocalDensity.current

    WallpaperBackground(
        wallpaper = wallpaperManager.getWallpaper(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Reset any temporary state if needed
                        }
                    ) { change, dragAmount ->
                        // Only detect swipe down from the top area of the screen
                        if (change.position.y < with(density) { 200.dp.toPx() } && 
                            dragAmount.y > with(density) { 150.dp.toPx() } && 
                            !showStartMenu && !showAllApps && !showSettings && !showCommandPrompt) {
                            showNotificationPanel = true
                        }
                    }
                }
        ) {
            // Main content area - simple padding from bottom for taskbar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = LayoutConstants.WORKING_AREA_PADDING_TOP,
                        start = LayoutConstants.WORKING_AREA_PADDING_HORIZONTAL,
                        end = LayoutConstants.WORKING_AREA_PADDING_HORIZONTAL,
                        bottom = LayoutConstants.TASKBAR_HEIGHT + LayoutConstants.WORKING_AREA_PADDING_BOTTOM
                    )
            ) {
                when {
                    showSettings -> {
                        SettingsScreen(
                            modifier = Modifier.fillMaxSize(),
                            systemStatusManager = systemStatusManager,
                            onNavigateBack = { showSettings = false }
                        )
                    }
                    showAllApps -> {
                        AllAppsScreen(
                            modifier = Modifier.fillMaxSize(),
                            appRepository = appRepository,
                            onBackClick = {
                                showAllApps = false
                                showStartMenu = true
                            }
                        )
                    }
                }

                // Settings icon in top left corner
                if (!showSettings && !showAllApps) {
                    IconButton(
                        onClick = { 
                            showSettings = true
                            showNotificationPanel = false
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(LayoutConstants.SPACING_LARGE)
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

                // Command prompt overlay
                CommandPrompt(
                    modifier = Modifier.fillMaxSize(),
                    isVisible = showCommandPrompt,
                    onDismiss = { showCommandPrompt = false }
                )
            }

            // Taskbar - positioned at bottom, full width
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(LayoutConstants.TASKBAR_HEIGHT)
            ) {
                Taskbar(
                    modifier = Modifier.fillMaxSize(),
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
                        showStartMenu = false
                        showNotificationPanel = false
                    },
                    onTaskViewClick = {
                        showStartMenu = false
                        showNotificationPanel = false
                    }
                )
            }
            
            // StartMenu overlay - positioned above taskbar
            if (showStartMenu) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            bottom = LayoutConstants.TASKBAR_HEIGHT + LayoutConstants.START_MENU_MARGIN_BOTTOM
                        )
                ) {
                    StartMenu(
                        modifier = Modifier
                            .width(LayoutConstants.START_MENU_WIDTH)
                            .heightIn(max = LayoutConstants.START_MENU_MAX_HEIGHT),
                        onDismiss = { 
                            showStartMenu = false
                            showNotificationPanel = false
                        }
                    )
                }
            }
            
            // Notification panel - positioned at the top
            NotificationPanel(
                isVisible = showNotificationPanel,
                onDismiss = { showNotificationPanel = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = LayoutConstants.WORKING_AREA_PADDING_TOP)
            )
        }
    }
}