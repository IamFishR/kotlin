package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import com.win11launcher.R
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.win11launcher.ui.layout.LayoutConstants
import com.win11launcher.utils.SystemStatus

@Composable
fun Taskbar(
    modifier: Modifier = Modifier,
    systemStatus: SystemStatus,
    onStartClick: () -> Unit,
    onCommandClick: () -> Unit,
    onSystemTrayClick: () -> Unit,
    onTaskViewClick: () -> Unit = {},
    onAIClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1F1F1F).copy(alpha = 0.9f))
            .padding(horizontal = LayoutConstants.SPACING_MEDIUM)
    ) {
        // Top border only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF404040))
                .align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = LayoutConstants.SPACING_MEDIUM)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_SMALL)
            ) {
                StartButton(
                    onClick = onStartClick
                )
                
                TaskViewButton(
                    onClick = onTaskViewClick
                )
                
                CommandButton(
                    onClick = onCommandClick
                )
                
                AIButton(
                    onClick = onAIClick
                )
            }
            
            // Right side items
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = LayoutConstants.SPACING_SMALL),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_SMALL)
            ) {
                DateTimeDisplay(
                    systemStatus = systemStatus,
                    onClick = onSystemTrayClick
                )
                
                NotificationButton(
                    onClick = onSystemTrayClick
                )
            }
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
            .clip(RoundedCornerShape(LayoutConstants.SPACING_SMALL))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Custom four-box Windows 11 style start icon
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_SMALL)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_SMALL)
                ) {
                    // Top-left box - Blue
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                Color(0xFF0078D4),
                                RoundedCornerShape(1.dp)
                            )
                    )
                    // Bottom-left box - Green
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                Color(0xFF107C10),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_SMALL)
                ) {
                    // Top-right box - Orange
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                Color(0xFFFF8C00),
                                RoundedCornerShape(1.dp)
                            )
                    )
                    // Bottom-right box - Red
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                Color(0xFFD13438),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskViewButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(LayoutConstants.SPACING_SMALL))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = "Task View",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CommandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(LayoutConstants.SPACING_SMALL))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "Command Prompt",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AIButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(LayoutConstants.SPACING_SMALL))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "AI Assistant",
            tint = Color(0xFF00D4FF), // Distinctive AI blue color
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun NotificationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(LayoutConstants.SPACING_SMALL))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SystemTrayButton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(LayoutConstants.SPACING_SMALL))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ExpandLess,
            contentDescription = "System Tray",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
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
    
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Up arrow to expand/collapse system tray
            IconButton(
                onClick = { showExpandedTray = !showExpandedTray },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (showExpandedTray) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
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
        
        // Popup for system status icons
        if (showExpandedTray) {
            Popup(
                alignment = Alignment.BottomEnd,
                offset = IntOffset(-16, -200),
                onDismissRequest = { showExpandedTray = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                SystemTrayTooltip(systemStatus)
            }
        }
    }
}

@Composable
private fun SystemTrayTooltip(systemStatus: SystemStatus) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // System icons in grid format
            items(getSystemIcons(systemStatus)) { iconData ->
                TooltipIcon(
                    icon = iconData.icon,
                    contentDescription = iconData.description,
                    tint = iconData.tint
                )
            }
        }
    }
}

@Composable
private fun TooltipIcon(
    icon: ImageVector,
    contentDescription: String,
    tint: Color = Color.White
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(LayoutConstants.SPACING_SMALL))
            .background(Color.Transparent)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

private data class SystemIconData(
    val icon: ImageVector,
    val description: String,
    val tint: Color = Color.White
)

private fun getSystemIcons(systemStatus: SystemStatus): List<SystemIconData> {
    return listOf(
        SystemIconData(Icons.AutoMirrored.Filled.VolumeUp, "Volume"),
        SystemIconData(Icons.Default.Brightness6, "Brightness"),
        SystemIconData(Icons.Default.Bluetooth, "Bluetooth"),
        SystemIconData(Icons.Default.LocationOn, "Location"),
        SystemIconData(Icons.Default.AirplanemodeActive, "Airplane Mode"),
        SystemIconData(Icons.Default.Wifi, "WiFi Hotspot"),
        SystemIconData(Icons.Default.DataUsage, "Data Usage"),
        SystemIconData(Icons.Default.DoNotDisturb, "Do Not Disturb"),
        SystemIconData(Icons.Default.FlashOn, "Flashlight"),
        SystemIconData(Icons.Default.ScreenRotation, "Screen Rotation"),
        SystemIconData(Icons.Default.Cast, "Cast"),
        SystemIconData(Icons.Default.NearMe, "Nearby Share")
    )
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
                isWifi = true,
                strength = systemStatus.wifiSignalStrength
            )
        } else if (systemStatus.mobileDataConnected) {
            NetworkIcon(
                isWifi = false,
                strength = systemStatus.mobileSignalStrength
            )
        }
        
        // Additional system icons
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
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
    isWifi: Boolean,
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
        imageVector = if (isWifi) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
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
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Custom horizontal battery icon
        HorizontalBatteryIcon(
            level = level,
            isCharging = isCharging
        )
        
        // Battery percentage text
        Text(
            text = "$level%",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HorizontalBatteryIcon(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        isCharging -> Color(0xFF10B981) // Green when charging
        level <= 15 -> Color(0xFFEF4444) // Red when critical
        level <= 30 -> Color(0xFFF59E0B) // Orange when low
        else -> Color.White
    }
    
    val fillPercentage = level / 100f
    
    Box(
        modifier = modifier.size(width = 20.dp, height = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Battery body (main rectangle)
        Box(
            modifier = Modifier
                .size(width = 17.dp, height = 10.dp)
                .border(
                    width = 1.dp,
                    color = batteryColor,
                    shape = RoundedCornerShape(1.dp)
                )
        ) {
            // Battery fill based on percentage
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillPercentage)
                    .background(
                        color = batteryColor,
                        shape = RoundedCornerShape(
                            topStart = 1.dp,
                            bottomStart = 1.dp,
                            topEnd = if (fillPercentage >= 0.95f) 1.dp else 0.dp,
                            bottomEnd = if (fillPercentage >= 0.95f) 1.dp else 0.dp
                        )
                    )
            )
        }
        
        // Battery tip (small rectangle on the right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 2.dp, height = 6.dp)
                .background(
                    color = batteryColor,
                    shape = RoundedCornerShape(
                        topEnd = 1.dp,
                        bottomEnd = 1.dp
                    )
                )
        )
        
        // Charging indicator (lightning bolt icon)
        if (isCharging) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Charging",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(8.dp)
            )
        }
    }
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
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy((-4).dp)
    ) {
        Text(
            text = systemStatus.currentTime,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
        
        Text(
            text = systemStatus.currentDate,
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.End
        )
    }
}