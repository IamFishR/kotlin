package com.win11launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.win11launcher.utils.SystemStatusManager
import com.win11launcher.viewmodels.SettingsViewModel
import com.win11launcher.ui.screens.CommandsReferenceScreen

// Simplified settings tabs for Windows-like experience
enum class SettingsTabNew(
    val title: String,
    val icon: ImageVector,
    val subtitle: String = ""
) {
    SYSTEM("System", Icons.Default.Settings, "Display, sound, power"),
    DEVICES("Devices", Icons.Default.Devices, "Bluetooth, printers, mouse"),
    NETWORK("Network", Icons.Default.Wifi, "Wi-Fi, airplane mode, VPN"),
    PERSONALIZATION("Personalization", Icons.Default.Palette, "Background, colors, themes"),
    APPS("Apps", Icons.Default.Apps, "Uninstall, defaults, features"),
    ACCOUNTS("Accounts", Icons.Default.Person, "Your accounts, family, work"),
    PRIVACY("Privacy", Icons.Default.Security, "Location, camera, microphone"),
    UPDATE("Update", Icons.Default.Update, "Windows Update, delivery optimization"),
    COMMANDS("Commands", Icons.Default.Terminal, "Command line reference")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenNew(
    systemStatusManager: SystemStatusManager,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(SettingsTabNew.SYSTEM) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        // Top Bar with back button and title
        TopAppBar(
            title = { 
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2D2D2D)
            )
        )
        
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left side - Category tiles (Windows-like)
            LazyColumn(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .background(Color(0xFF232323))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SettingsTabNew.values()) { tab ->
                    SettingsCategoryTile(
                        tab = tab,
                        isSelected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }
            
            // Right side - Settings content
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E1E1E))
                    .padding(24.dp)
            ) {
                when (selectedTab) {
                    SettingsTabNew.SYSTEM -> SystemSettingsContent()
                    SettingsTabNew.DEVICES -> DevicesSettingsContent()
                    SettingsTabNew.NETWORK -> NetworkSettingsContent(systemStatusManager)
                    SettingsTabNew.PERSONALIZATION -> PersonalizationSettingsContent()
                    SettingsTabNew.APPS -> AppsSettingsContent()
                    SettingsTabNew.ACCOUNTS -> AccountsSettingsContent()
                    SettingsTabNew.PRIVACY -> PrivacySettingsContent()
                    SettingsTabNew.UPDATE -> UpdateSettingsContent()
                    SettingsTabNew.COMMANDS -> CommandsReferenceScreen()
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryTile(
    tab: SettingsTabNew,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0078D4) else Color(0xFF2D2D2D)
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = if (isSelected) Color.White else Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tab.title,
                    color = if (isSelected) Color.White else Color(0xFFE5E7EB),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (tab.subtitle.isNotEmpty()) {
                    Text(
                        text = tab.subtitle,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemSettingsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "System",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "Display",
                description = "Change the size of text, apps, and other items"
            ) {
                SettingsItem(
                    title = "Display resolution",
                    description = "1920 x 1080 (Recommended)",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Display orientation",
                    description = "Landscape",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Multiple displays",
                    description = "Extend desktop to this display",
                    onClick = { }
                )
            }
        }
        
        item {
            SettingsGroup(
                title = "Sound",
                description = "Manage audio devices and sound settings"
            ) {
                SettingsItem(
                    title = "System sounds",
                    description = "Choose which sounds play for system events",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Volume mixer",
                    description = "Set volume levels for individual apps",
                    onClick = { }
                )
            }
        }
        
        item {
            SettingsGroup(
                title = "Power",
                description = "Sleep and power options"
            ) {
                SettingsItem(
                    title = "Power mode",
                    description = "Balanced",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Screen timeout",
                    description = "Never",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun DevicesSettingsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Devices",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "Bluetooth & other devices",
                description = "Manage Bluetooth and other connected devices"
            ) {
                SettingsItem(
                    title = "Bluetooth",
                    description = "Add Bluetooth or other devices",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Connected devices",
                    description = "Manage connected devices",
                    onClick = { }
                )
            }
        }
        
        item {
            SettingsGroup(
                title = "Printers & scanners",
                description = "Add printers and scanners"
            ) {
                SettingsItem(
                    title = "Add printer or scanner",
                    description = "Search for available devices",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun NetworkSettingsContent(systemStatusManager: SystemStatusManager) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Network & Internet",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "Wi-Fi",
                description = "Manage Wi-Fi networks and settings"
            ) {
                SettingsItem(
                    title = "Wi-Fi network",
                    description = "Connected to network",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Manage known networks",
                    description = "View and modify saved networks",
                    onClick = { }
                )
            }
        }
        
        item {
            SettingsGroup(
                title = "Mobile hotspot",
                description = "Share your internet connection"
            ) {
                SettingsItem(
                    title = "Mobile hotspot",
                    description = "Share your internet connection with other devices",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun PersonalizationSettingsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Personalization",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "Background",
                description = "Choose your desktop background"
            ) {
                SettingsItem(
                    title = "Background image",
                    description = "Picture",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Choose your picture",
                    description = "Browse for background images",
                    onClick = { }
                )
            }
        }
        
        item {
            SettingsGroup(
                title = "Colors",
                description = "Choose accent colors and themes"
            ) {
                SettingsItem(
                    title = "Color mode",
                    description = "Dark",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Accent color",
                    description = "Windows blue",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun AppsSettingsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Apps",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "Apps & features",
                description = "Manage installed apps and features"
            ) {
                SettingsItem(
                    title = "Installed apps",
                    description = "View and manage installed applications",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Default apps",
                    description = "Choose default applications",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun AccountsSettingsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Accounts",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "Your info",
                description = "Manage your account information"
            ) {
                SettingsItem(
                    title = "Sign-in options",
                    description = "Manage how you sign in",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Your account",
                    description = "Manage your Microsoft account",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun PrivacySettingsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Privacy & security",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "App permissions",
                description = "Control which apps can access your data"
            ) {
                SettingsItem(
                    title = "Camera",
                    description = "Let apps use your camera",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Location",
                    description = "Allow apps to access your location",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Microphone",
                    description = "Allow apps to use your microphone",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun UpdateSettingsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Windows Update",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            SettingsGroup(
                title = "Update status",
                description = "Check for and install updates"
            ) {
                SettingsItem(
                    title = "Check for updates",
                    description = "You're up to date",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "Update history",
                    description = "View recently installed updates",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = description,
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = description,
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
    }
}