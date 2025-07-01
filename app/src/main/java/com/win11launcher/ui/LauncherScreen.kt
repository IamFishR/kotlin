package com.win11launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.win11launcher.ui.components.StartMenu
import com.win11launcher.ui.components.Taskbar

@Composable
fun LauncherScreen() {
    var showStartMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showStartMenu) {
            StartMenu(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 56.dp)
                    .width(600.dp)
                    .height(680.dp),
                onDismiss = { showStartMenu = false }
            )
        }
        
        Taskbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(48.dp),
            onStartClick = { showStartMenu = !showStartMenu }
        )
    }
}