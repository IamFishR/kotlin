package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            painter = painterResource(com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_search_24_regular),
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
                painter = painterResource(if (showExpandedTray) com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_chevron_down_24_regular else com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_chevron_up_24_regular),
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
                iconRes = com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_wifi_1_24_regular,
                strength = systemStatus.wifiSignalStrength
            )
        } else if (systemStatus.mobileDataConnected) {
            NetworkIcon(
                iconRes = com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_cellular_data_1_24_regular,
                strength = systemStatus.mobileSignalStrength
            )
        }
        
        // Additional system icons
        Icon(
            painter = painterResource(com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_speaker_2_24_regular),
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
                painter = painterResource(com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_wifi_1_24_regular),
                contentDescription = "WiFi",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else if (systemStatus.mobileDataConnected) {
            Icon(
                painter = painterResource(com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_cellular_data_1_24_regular),
                contentDescription = "Mobile Data",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun NetworkIcon(
    iconRes: Int,
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
        painter = painterResource(iconRes),
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
    val batteryIconRes = when {
        isCharging -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_charge_24_regular
        level >= 90 -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_10_24_regular
        level >= 60 -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_7_24_regular
        level >= 50 -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_6_24_regular
        level >= 30 -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_4_24_regular
        level >= 20 -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_2_24_regular
        level >= 10 -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_1_24_regular
        else -> com.microsoft.fluent.mobile.icons.R.drawable.ic_fluent_battery_warning_24_regular
    }
    
    val batteryColor = when {
        isCharging -> Color.Green
        level <= 15 -> Color.Red
        level <= 30 -> Color.Yellow
        else -> Color.White
    }
    
    Icon(
        painter = painterResource(batteryIconRes),
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