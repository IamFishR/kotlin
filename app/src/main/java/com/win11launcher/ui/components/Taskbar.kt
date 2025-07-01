package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.win11launcher.utils.SystemStatus

@Composable
fun Taskbar(
    modifier: Modifier = Modifier,
    systemStatus: SystemStatus,
    onStartClick: () -> Unit,
    onSystemTrayClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                Color(0xFF1F1F1F),
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StartButton(
                onClick = onStartClick,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            SearchButton(
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            SystemTray(
                systemStatus = systemStatus,
                onSystemTrayClick = onSystemTrayClick,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun StartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    Color(0xFF0078D4),
                    RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
private fun SearchButton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Transparent)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SystemTray(
    systemStatus: SystemStatus,
    onSystemTrayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExpandedTray by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // System status icons (hidden by default, shown when expanded)
        if (showExpandedTray) {
            SystemStatusIcons(systemStatus)
        }
        
        // Up arrow to expand/collapse system tray
        IconButton(
            onClick = { showExpandedTray = !showExpandedTray },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (showExpandedTray) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = "Expand system tray",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        
        // Always visible essential icons
        EssentialSystemIcons(systemStatus)
        
        // Date and time (clickable for notifications)
        DateTimeDisplay(
            systemStatus = systemStatus,
            onClick = onSystemTrayClick
        )
    }
}

@Composable
private fun SystemStatusIcons(systemStatus: SystemStatus) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Network icons
        if (systemStatus.wifiConnected) {
            NetworkIcon(
                icon = Icons.Default.Wifi,
                strength = systemStatus.wifiSignalStrength
            )
        } else if (systemStatus.mobileDataConnected) {
            NetworkIcon(
                icon = Icons.Default.SignalCellularAlt,
                strength = systemStatus.mobileSignalStrength
            )
        }
        
        // Additional system icons
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Volume",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EssentialSystemIcons(systemStatus: SystemStatus) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Battery icon with level
        BatteryIcon(
            level = systemStatus.batteryLevel,
            isCharging = systemStatus.isCharging
        )
        
        // Primary network icon (always visible)
        if (systemStatus.wifiConnected) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "WiFi",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else if (systemStatus.mobileDataConnected) {
            Icon(
                imageVector = Icons.Default.SignalCellularAlt,
                contentDescription = "Mobile Data",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun NetworkIcon(
    icon: ImageVector,
    strength: Int,
    modifier: Modifier = Modifier
) {
    val alpha = when (strength) {
        4 -> 1.0f
        3 -> 0.8f
        2 -> 0.6f
        1 -> 0.4f
        else -> 0.2f
    }
    
    Icon(
        imageVector = icon,
        contentDescription = "Network Signal",
        tint = Color.White.copy(alpha = alpha),
        modifier = modifier.size(16.dp)
    )
}

@Composable
private fun BatteryIcon(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val batteryIcon = when {
        isCharging -> Icons.Default.BatteryChargingFull
        level >= 90 -> Icons.Default.BatteryFull
        level >= 60 -> Icons.Default.BatteryFull
        level >= 50 -> Icons.Default.BatteryFull
        level >= 30 -> Icons.Default.BatteryFull
        level >= 20 -> Icons.Default.Battery2Bar
        level >= 10 -> Icons.Default.Battery1Bar
        else -> Icons.Default.BatteryAlert
    }
    
    val batteryColor = when {
        isCharging -> Color.Green
        level <= 15 -> Color.Red
        level <= 30 -> Color.Yellow
        else -> Color.White
    }
    
    Icon(
        imageVector = batteryIcon,
        contentDescription = "Battery $level%",
        tint = batteryColor,
        modifier = modifier.size(16.dp)
    )
}

@Composable
private fun DateTimeDisplay(
    systemStatus: SystemStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy((-2).dp)
    ) {
        Text(
            text = systemStatus.currentTime,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
        
        Text(
            text = systemStatus.currentDate,
            color = Color.White,
            fontSize = 9.sp,
            textAlign = TextAlign.End
        )
    }
}