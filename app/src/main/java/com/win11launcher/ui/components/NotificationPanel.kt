package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.win11launcher.models.AppNotification
import com.win11launcher.utils.NotificationManager
import com.win11launcher.utils.SystemStatus
import com.win11launcher.utils.SystemStatusManager
import com.win11launcher.utils.WiFiManager
import com.win11launcher.utils.BluetoothManager
import com.win11launcher.utils.LocationManager
import com.win11launcher.utils.rememberNotifications
import java.text.SimpleDateFormat
import java.util.*

data class QuickAction(
    val name: String,
    val icon: ImageVector,
    val isEnabled: Boolean = true,
    val subtitle: String = "",
    val onClick: () -> Unit = {}
)

data class NotificationItem(
    val title: String,
    val content: String,
    val time: String,
    val appName: String,
    val icon: ImageVector = Icons.Default.Notifications
)

@Composable
fun NotificationPanel(
    showPanel: Boolean,
    systemStatus: SystemStatus,
    systemStatusManager: SystemStatusManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val notificationManager = remember { NotificationManager(context) }
    
    // Get system managers from SystemStatusManager
    val wifiManager = remember { systemStatusManager.getWiFiManager() }
    val bluetoothManager = remember { systemStatusManager.getBluetoothManager() }
    val locationManager = remember { systemStatusManager.getLocationManager() }
    
    val realNotifications = rememberNotifications(notificationManager)
    if (showPanel) {
        Popup(
            alignment = Alignment.BottomEnd,
            offset = androidx.compose.ui.unit.IntOffset(-16, -72), // Position above taskbar
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .width(400.dp)
                    .height(600.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header with date and time
                    DateTimeHeader(systemStatus)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notifications at the top
                    NotificationsSection(
                        notifications = realNotifications,
                        notificationManager = notificationManager,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Light border separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF404040))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // System toggles at the bottom
                    QuickActionsSection(
                        systemStatus = systemStatus,
                        wifiManager = wifiManager,
                        bluetoothManager = bluetoothManager,
                        locationManager = locationManager
                    )
                }
            }
        }
    }
}

@Composable
private fun DateTimeHeader(systemStatus: SystemStatus) {
    Column {
        Text(
            text = systemStatus.currentTime,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = getCurrentFullDate(),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun QuickActionsSection(
    systemStatus: SystemStatus,
    wifiManager: WiFiManager,
    bluetoothManager: BluetoothManager,
    locationManager: LocationManager
) {
    val quickActions = remember(
        systemStatus.wifiEnabled, 
        systemStatus.wifiConnected, 
        systemStatus.wifiSsid,
        systemStatus.bluetoothEnabled,
        systemStatus.bluetoothConnected,
        systemStatus.bluetoothConnectedDevicesCount,
        systemStatus.locationEnabled,
        systemStatus.locationHasPermission,
        systemStatus.batteryLevel
    ) {
        listOf(
            QuickAction(
                name = "Wi-Fi",
                icon = Icons.Default.Wifi,
                isEnabled = systemStatus.wifiEnabled,
                subtitle = when {
                    systemStatus.wifiConnected && systemStatus.wifiSsid.isNotEmpty() -> systemStatus.wifiSsid
                    else -> ""
                },
                onClick = { wifiManager.toggleWiFi() }
            ),
            QuickAction(
                name = "Bluetooth",
                icon = Icons.Default.Bluetooth,
                isEnabled = systemStatus.bluetoothEnabled,
                subtitle = when {
                    !systemStatus.bluetoothSupported -> "Not supported"
                    systemStatus.bluetoothConnectedDevicesCount == 1 -> "1 device"
                    systemStatus.bluetoothConnectedDevicesCount > 1 -> "${systemStatus.bluetoothConnectedDevicesCount} devices"
                    else -> ""
                },
                onClick = { bluetoothManager.toggleBluetooth() }
            ),
            QuickAction(
                name = "Location",
                icon = Icons.Default.LocationOn,
                isEnabled = systemStatus.locationEnabled,
                subtitle = when {
                    !systemStatus.locationHasPermission && systemStatus.locationEnabled -> "Permission required"
                    else -> ""
                },
                onClick = { locationManager.toggleLocation() }
            ),
            QuickAction(
                name = "Airplane",
                icon = Icons.Default.AirplanemodeActive,
                isEnabled = false,
                subtitle = "",
                onClick = { /* TODO: Implement airplane mode */ }
            ),
            QuickAction(
                name = "Focus",
                icon = Icons.Default.DoNotDisturb,
                isEnabled = false,
                subtitle = "",
                onClick = { /* TODO: Implement focus mode */ }
            )
        )
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            quickActions.take(3).forEach { action ->
                QuickActionButton(
                    action = action,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            quickActions.drop(3).forEach { action ->
                QuickActionButton(
                    action = action,
                    modifier = Modifier.weight(1f)
                )
            }
            // Add spacer for third empty slot to maintain layout
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionButton(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { action.onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (action.isEnabled) Color(0xFF0078D4) else Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = action.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (action.subtitle.isNotEmpty()) {
                Text(
                    text = action.subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun NotificationsSection(
    notifications: List<AppNotification>,
    notificationManager: NotificationManager,
    modifier: Modifier = Modifier
) {
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(
                onClick = { notificationManager.dismissAllNotifications() }
            ) {
                Text(
                    text = "Clear all",
                    color = Color(0xFF0078D4),
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (!notificationManager.isNotificationAccessEnabled()) {
            // Show permission request UI
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = "Notification access disabled",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Notification access required",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Enable notification access to see your notifications here",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { notificationManager.openNotificationAccessSettings() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0078D4)
                        )
                    ) {
                        Text("Grant Access", color = Color.White)
                    }
                    
                    // Add background permissions button if needed
                    if (!notificationManager.isBackgroundLaunchAllowed()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { notificationManager.checkAndRequestBackgroundPermissions() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0078D4).copy(alpha = 0.7f)
                            )
                        ) {
                            Text("Enable Background Launch", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "No notifications",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "No new notifications",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column {
                // Show background permissions warning if needed
                if (!notificationManager.isBackgroundLaunchAllowed()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF404040)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Background launch restricted",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Grant additional permissions for notification clicks",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            
                            TextButton(
                                onClick = { notificationManager.checkAndRequestBackgroundPermissions() }
                            ) {
                                Text(
                                    text = "Fix",
                                    color = Color(0xFF0078D4),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        RealNotificationCard(
                            notification = notification,
                            onDismiss = { notificationManager.dismissNotification(notification.id) },
                            onClick = { notificationManager.handleNotificationClick(notification) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RealNotificationCard(
    notification: AppNotification,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Notifications, // We'll enhance this later with app icons
                contentDescription = notification.appName,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title.ifEmpty { notification.appName },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (notification.content.isNotEmpty()) {
                    Text(
                        text = notification.content,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
                
                Text(
                    text = "${notification.appName} • ${notification.time}",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            
            if (notification.isClearable) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle notification click */ },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = notification.icon,
                contentDescription = notification.appName,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = notification.content,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 2
                )
                
                Text(
                    text = notification.time,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun getCurrentFullDate(): String {
    val format = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return format.format(Date())
}