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
import com.win11launcher.ui.components.ChatbotScreen
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
    var showChatbot by remember { mutableStateOf(false) }
    
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
    val taskbarHeight = 56.dp // Defined taskbar height

    WallpaperBackground(
        wallpaper = wallpaperManager.getWallpaper(),
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content area, above the taskbar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = taskbarHeight) // Content area is above taskbar
        ) {
            when {
                showSettings -> {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(), // Fill this content Box
                        systemStatusManager = systemStatusManager,
                        onNavigateBack = { showSettings = false }
                    )
                }
                showNotesHub -> {
                    NotesHubScreen(
                        modifier = Modifier.fillMaxSize(), // Fill this content Box
                        onNavigateBack = { showNotesHub = false }
                    )
                }
                showAllApps -> {
                    AllAppsScreen(
                        modifier = Modifier.fillMaxSize(), // Fill this content Box
                        appRepository = appRepository,
                        onBackClick = {
                            showAllApps = false
                            showStartMenu = true
                        }
                    )
                }
                showStartMenu -> {
                    StartMenu(
                        modifier = Modifier // No longer needs padding(bottom = 56.dp)
                            .align(Alignment.BottomStart) // Aligns to bottom of this Box
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

            // Settings icon in top left corner of the main content area
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

            if (showChatbot) {
                ChatbotScreen(modifier = Modifier.fillMaxSize()) // Fill this content Box
            }

            // Notification panel - typically an overlay, might need alignment if within this box
            // Or could be outside this main content box if it's meant to overlay taskbar too
            // For now, keeping it inside, assuming it aligns within the padded Box.
            NotificationPanel(
                modifier = Modifier.align(Alignment.TopEnd), // Example alignment
                showPanel = showNotificationPanel,
                systemStatus = systemStatus,
                systemStatusManager = systemStatusManager,
                onDismiss = { showNotificationPanel = false }
            )

            // Command prompt - also an overlay
            CommandPrompt(
                modifier = Modifier.fillMaxSize(), // Fill this content Box if shown here
                isVisible = showCommandPrompt,
                onDismiss = { showCommandPrompt = false }
            )
        } // End of main content Box

        Taskbar(
            modifier = Modifier // Taskbar is outside the main content Box, aligned to WallpaperBackground bottom
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
            onAiClick = {
                showChatbot = !showChatbot
            },
            onSystemTrayClick = { 
                showNotificationPanel = !showNotificationPanel
                showStartMenu = false
            }
        )
    }
}