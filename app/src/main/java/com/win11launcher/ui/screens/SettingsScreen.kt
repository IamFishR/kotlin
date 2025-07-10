package com.win11launcher.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import com.win11launcher.data.entities.PermissionState
import com.win11launcher.utils.SystemStatusManager
import com.win11launcher.viewmodels.SettingsViewModel
import androidx.core.content.ContextCompat
import com.win11launcher.ui.layout.LayoutConstants
import com.win11launcher.services.AIService
import com.win11launcher.services.AIMemoryManager
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.Contexts

enum class SettingsTab(
    val title: String,
    val icon: ImageVector
) {
    PROFILE("My Computer", Icons.Default.Person),
    PERMISSIONS("Permissions", Icons.Default.Security),
    SYSTEM("System", Icons.Default.Settings),
    DISPLAY("Display", Icons.Default.DisplaySettings),
    NETWORK("Network", Icons.Default.Wifi),
    APPS("Apps", Icons.Default.Apps),
    AI("AI Assistant", Icons.Default.Psychology),
    ABOUT("About", Icons.Default.Info)
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    systemStatusManager: SystemStatusManager,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SettingsTab.PROFILE) }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionStates by viewModel.permissionStates.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val permissionAnalytics by viewModel.permissionAnalytics.collectAsStateWithLifecycle()

    // Launcher for requesting a single permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.refreshPermissions()
        }
    }

    // Launcher for requesting multiple permissions
    val requestMultiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.refreshPermissions()
        }
    }

    val onRequestPermission: (String) -> Unit = { permissionName ->
        when (permissionName) {
            Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                    context.startActivity(intent)
                }
                viewModel.refreshPermissions()
            }
            Manifest.permission.WRITE_SETTINGS -> {
                if (!Settings.System.canWrite(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.packageName))
                    context.startActivity(intent)
                }
                viewModel.refreshPermissions()
            }
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
                viewModel.refreshPermissions()
            }
            else -> {
                // For normal permissions, request directly
                if (ContextCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(permissionName)
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        // Top Bar with Back Button and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D))
                .padding(horizontal = LayoutConstants.SPACING_LARGE, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(LayoutConstants.ICON_HUGE)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(LayoutConstants.ICON_LARGE)
                )
            }
            
            Spacer(modifier = Modifier.width(LayoutConstants.SPACING_MEDIUM))
            
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Tabs for navigation
        ScrollableTabRow(
            selectedTabIndex = SettingsTab.values().indexOf(selectedTab),
            containerColor = Color(0xFF2D2D2D),
            contentColor = Color.White,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsTab.values().forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                    selectedContentColor = Color(0xFF0078D4),
                    unselectedContentColor = Color.White
                )
            }
        }
        
        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF232323))
        ) {
            when (selectedTab) {
                SettingsTab.PROFILE -> ProfileContent(
                    viewModel = viewModel
                )
                SettingsTab.PERMISSIONS -> PermissionsContent(
                    permissionStates = permissionStates,
                    permissionAnalytics = permissionAnalytics,
                    isLoading = uiState.isLoading,
                    onRefreshPermissions = { viewModel.refreshPermissions() },
                    onUpdateNotes = { permission, notes -> viewModel.updatePermissionNotes(permission, notes) },
                    onRequestPermission = onRequestPermission
                )
                SettingsTab.SYSTEM -> SystemContent(
                    appSettings = appSettings.filter { it.category == "system" },
                    onToggleAutoStart = { viewModel.toggleLauncherAutoStart() }
                )
                SettingsTab.DISPLAY -> DisplayContent(
                    appSettings = appSettings.filter { it.category == "appearance" },
                    onThemeChange = { theme -> viewModel.setThemeMode(theme) }
                )
                SettingsTab.NETWORK -> NetworkContent(systemStatusManager)
                SettingsTab.APPS -> AppsContent()
                SettingsTab.AI -> {
                    // Get AI services through EntryPoint since they're not directly injectable here
                    val context = LocalContext.current
                    val appContext = context.applicationContext
                    
                    // For now, create a simplified AI settings content
                    // In a production app, you'd properly inject these services
                    AISettingsContentSimplified(viewModel = viewModel)
                }
                SettingsTab.ABOUT -> AboutContent()
            }
        }
        
        // Show error snackbar if there's an error
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // You can show a snackbar here if needed
                viewModel.clearError()
            }
        }
    }
}



@Composable
private fun ProfileContent(
    viewModel: SettingsViewModel
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val userCustomization by viewModel.userCustomization.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Computer",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Customize your profile and personalize your experience",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Profile Picture Section
        item {
            ProfilePictureSection(
                profilePicturePath = userProfile?.profilePicturePath,
                onProfilePictureChange = { uri -> viewModel.updateProfilePicture(uri) }
            )
        }
        
        // Username Section
        item {
            UsernameSection(
                username = userProfile?.username ?: "User",
                onUsernameChange = { username -> viewModel.updateUsername(username) }
            )
        }
        
        // Display Name Section
        item {
            DisplayNameSection(
                displayName = userProfile?.displayName ?: "",
                onDisplayNameChange = { displayName -> viewModel.updateDisplayName(displayName) }
            )
        }
        
        // Bio Section
        item {
            BioSection(
                bio = userProfile?.bio ?: "",
                onBioChange = { bio -> viewModel.updateBio(bio) }
            )
        }
        
        // Theme Color Section
        item {
            ThemeColorSection(
                selectedColor = userProfile?.themeColor ?: "#0078D4",
                onColorChange = { color -> viewModel.updateThemeColor(color) }
            )
        }
    }
}

@Composable
private fun ProfilePictureSection(
    profilePicturePath: String?,
    onProfilePictureChange: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onProfilePictureChange(it) }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Profile Picture",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current profile picture or placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color(0xFF2D2D2D),
                            RoundedCornerShape(40.dp)
                        )
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (!profilePicturePath.isNullOrEmpty() && File(profilePicturePath).exists()) {
                        AsyncImage(
                            model = File(profilePicturePath),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(40.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Click to change your profile picture",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "Recommended: Square image, at least 256x256 pixels",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0078D4)
                    )
                ) {
                    Text("Change")
                }
            }
        }
    }
}

@Composable
private fun UsernameSection(
    username: String,
    onUsernameChange: (String) -> Unit
) {
    var localUsername by remember(username) { mutableStateOf(username) }
    var isEditing by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Username",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(
                    onClick = { 
                        if (isEditing) {
                            onUsernameChange(localUsername)
                        }
                        isEditing = !isEditing 
                    }
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Save" else "Edit",
                        tint = Color(0xFF0078D4)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = localUsername,
                    onValueChange = { localUsername = it },
                    placeholder = {
                        Text(
                            text = "Enter your username",
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0078D4),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Text(
                    text = localUsername,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Text(
                text = "This will be displayed in the Start Menu and other areas",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun DisplayNameSection(
    displayName: String,
    onDisplayNameChange: (String) -> Unit
) {
    var localDisplayName by remember(displayName) { mutableStateOf(displayName) }
    var isEditing by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Display Name",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(
                    onClick = { 
                        if (isEditing) {
                            onDisplayNameChange(localDisplayName)
                        }
                        isEditing = !isEditing 
                    }
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Save" else "Edit",
                        tint = Color(0xFF0078D4)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = localDisplayName,
                    onValueChange = { localDisplayName = it },
                    placeholder = {
                        Text(
                            text = "Enter your full name (optional)",
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0078D4),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Text(
                    text = if (localDisplayName.isNotEmpty()) localDisplayName else "Not set",
                    color = if (localDisplayName.isNotEmpty()) Color.White else Color.Gray,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Text(
                text = "Optional: Your full name or preferred display name",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun BioSection(
    bio: String,
    onBioChange: (String) -> Unit
) {
    var localBio by remember(bio) { mutableStateOf(bio) }
    var isEditing by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bio",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(
                    onClick = { 
                        if (isEditing) {
                            onBioChange(localBio)
                        }
                        isEditing = !isEditing 
                    }
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Save" else "Edit",
                        tint = Color(0xFF0078D4)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = localBio,
                    onValueChange = { localBio = it },
                    placeholder = {
                        Text(
                            text = "Tell us about yourself...",
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0078D4),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )
            } else {
                Text(
                    text = if (localBio.isNotEmpty()) localBio else "No bio added yet",
                    color = if (localBio.isNotEmpty()) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Text(
                text = "Optional: A short description about yourself",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ThemeColorSection(
    selectedColor: String,
    onColorChange: (String) -> Unit
) {
    
    val themeColors = listOf(
        "#0078D4" to "Windows Blue",
        "#E74C3C" to "Red",
        "#2ECC71" to "Green",
        "#F39C12" to "Orange",
        "#9B59B6" to "Purple",
        "#1ABC9C" to "Teal",
        "#34495E" to "Dark Gray",
        "#E67E22" to "Carrot"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Accent Color",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(themeColors) { (colorHex, colorName) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex)),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onColorChange(colorHex) }
                            .let {
                                if (selectedColor == colorHex) {
                                    it.background(
                                        Color.White.copy(alpha = 0.3f),
                                        RoundedCornerShape(20.dp)
                                    )
                                } else it
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == colorHex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "Choose your preferred accent color for the interface",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun PermissionsContent(
    permissionStates: List<PermissionState>,
    permissionAnalytics: com.win11launcher.data.repositories.PermissionAnalytics?,
    isLoading: Boolean,
    onRefreshPermissions: () -> Unit,
    onUpdateNotes: (String, String) -> Unit,
    onRequestPermission: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Permissions",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Manage app permissions to enable full functionality",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                IconButton(
                    onClick = onRefreshPermissions,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF0078D4)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh permissions",
                            tint = Color(0xFF0078D4)
                        )
                    }
                }
            }
        }
        
        // Permission analytics
        permissionAnalytics?.let { analytics ->
            item {
                PermissionAnalyticsCard(analytics = analytics)
            }
        }
        
        // Permission cards
        items(permissionStates) { permission ->
            DatabasePermissionCard(
                permission = permission,
                onUpdateNotes = onUpdateNotes,
                onRequestPermission = onRequestPermission
            )
        }
        
        if (permissionStates.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF404040)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No permission data available",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = onRefreshPermissions,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0078D4)
                                )
                            ) {
                                Text("Load Permissions")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionAnalyticsCard(analytics: com.win11launcher.data.repositories.PermissionAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Permission Overview",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnalyticsItem(
                    title = "Total",
                    value = analytics.totalPermissions.toString(),
                    color = Color(0xFF2196F3)
                )
                AnalyticsItem(
                    title = "Granted",
                    value = analytics.grantedPermissions.toString(),
                    color = Color(0xFF4CAF50)
                )
                AnalyticsItem(
                    title = "Missing",
                    value = analytics.missingRequiredPermissions.toString(),
                    color = Color(0xFFFF5722)
                )
                AnalyticsItem(
                    title = "Completion",
                    value = "${analytics.permissionCompletionRate.toInt()}%",
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DatabasePermissionCard(
    permission: PermissionState,
    onUpdateNotes: (String, String) -> Unit,
    onRequestPermission: (String) -> Unit // New parameter
) {
    var showNotesDialog by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(permission.userNotes) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Permission icon
                Icon(
                    imageVector = getPermissionIcon(permission.permissionName),
                    contentDescription = permission.permissionName,
                    tint = if (permission.isGranted) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getPermissionDisplayName(permission.permissionName),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        if (permission.isRequired) {
                            Text(
                                text = "Required",
                                color = Color(0xFFFF5722),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFF5722).copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        } else {
                            Text(
                                text = "Optional",
                                color = Color(0xFF2196F3),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .background(
                                        Color(0xFF2196F3).copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = getPermissionDescription(permission.permissionName),
                        color = Color.Gray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Status and stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                
                                Text(
                                    text = if (permission.isGranted) "Granted" else "Not granted",
                                    color = if (permission.isGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            if (permission.requestCount > 0) {
                                Text(
                                    text = "Requested ${permission.requestCount} times",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        // Action buttons (Notes or Grant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!permission.isGranted) {
                                TextButton(
                                    onClick = { onRequestPermission(permission.permissionName) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF0078D4))
                                ) {
                                    Text("Grant")
                                }
                            }
                            
                            if (permission.userNotes.isNotEmpty()) {
                                IconButton(
                                    onClick = { showNotesDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Note,
                                        contentDescription = "View notes",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { showNotesDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NoteAdd,
                                        contentDescription = "Add notes",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Checkmark or warning icon
                Icon(
                    imageVector = if (permission.isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = if (permission.isGranted) "Granted" else "Not granted",
                    tint = if (permission.isGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Notes dialog
    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = {
                Text(
                    text = "Permission Notes",
                    color = Color.White
                )
            },
            text = {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = {
                        Text(
                            text = "Add your notes about this permission...",
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0078D4),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateNotes(permission.permissionName, notesText)
                        showNotesDialog = false
                    }
                ) {
                    Text(
                        text = "Save",
                        color = Color(0xFF0078D4)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        notesText = permission.userNotes
                        showNotesDialog = false 
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.Gray
                    )
                }
            },
            containerColor = Color(0xFF2D2D2D)
        )
    }
}

@Composable
private fun PermissionCard(permission: PermissionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF404040)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Permission icon
            Icon(
                imageVector = permission.icon,
                contentDescription = permission.name,
                tint = if (permission.isGranted) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = permission.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (permission.isRequired) {
                        Text(
                            text = "Required",
                            color = Color(0xFFFF5722),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(
                                    Color(0xFFFF5722).copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Text(
                            text = "Optional",
                            color = Color(0xFF2196F3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(
                                    Color(0xFF2196F3).copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = permission.description,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    
                    Text(
                        text = if (permission.isGranted) "Granted" else "Not granted",
                        color = if (permission.isGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Checkmark or warning icon
            Icon(
                imageVector = if (permission.isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (permission.isGranted) "Granted" else "Not granted",
                tint = if (permission.isGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Helper functions for permission display
private fun getPermissionIcon(permissionName: String): ImageVector {
    return when {
        permissionName.contains("WIFI") -> Icons.Default.Wifi
        permissionName.contains("BLUETOOTH") -> Icons.Default.Bluetooth
        permissionName.contains("LOCATION") -> Icons.Default.LocationOn
        permissionName.contains("CAMERA") -> Icons.Default.Camera
        permissionName.contains("PHONE") -> Icons.Default.Phone
        permissionName.contains("NOTIFICATION") -> Icons.Default.Notifications
        permissionName.contains("ALERT_WINDOW") -> Icons.Default.Window
        permissionName.contains("WRITE_SETTINGS") -> Icons.Default.Settings
        permissionName.contains("BATTERY") -> Icons.Default.BatteryFull
        else -> Icons.Default.Security
    }
}

private fun getPermissionDisplayName(permissionName: String): String {
    return when (permissionName) {
        "android.permission.ACCESS_WIFI_STATE" -> "WiFi Status"
        "android.permission.CHANGE_WIFI_STATE" -> "WiFi Control"
        "android.permission.ACCESS_NETWORK_STATE" -> "Network State"
        "android.permission.BLUETOOTH" -> "Bluetooth (Legacy)"
        "android.permission.BLUETOOTH_ADMIN" -> "Bluetooth Admin (Legacy)"
        "android.permission.BLUETOOTH_CONNECT" -> "Bluetooth Connect"
        "android.permission.BLUETOOTH_SCAN" -> "Bluetooth Scan"
        "android.permission.ACCESS_FINE_LOCATION" -> "Precise Location"
        "android.permission.ACCESS_COARSE_LOCATION" -> "Approximate Location"
        "android.permission.SYSTEM_ALERT_WINDOW" -> "Display over other apps"
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" -> "Notification Access"
        "android.permission.READ_PHONE_STATE" -> "Phone State"
        "android.permission.CAMERA" -> "Camera"
        "android.permission.WRITE_SETTINGS" -> "Modify System Settings"
        else -> permissionName.substringAfterLast(".")
    }
}

private fun getPermissionDescription(permissionName: String): String {
    return when (permissionName) {
        "android.permission.ACCESS_WIFI_STATE" -> "Allows the app to view WiFi connection information and status. Required for WiFi status monitoring."
        "android.permission.CHANGE_WIFI_STATE" -> "Enables WiFi toggle functionality and network management. Required for WiFi control features."
        "android.permission.ACCESS_NETWORK_STATE" -> "Provides access to network connectivity information for connection status monitoring."
        "android.permission.BLUETOOTH" -> "Legacy Bluetooth permission for Android 11 and below. Enables basic Bluetooth functionality."
        "android.permission.BLUETOOTH_ADMIN" -> "Legacy Bluetooth admin permission for managing Bluetooth adapter state on older Android versions."
        "android.permission.BLUETOOTH_CONNECT" -> "Modern Bluetooth permission for Android 12+. Required for connecting to Bluetooth devices."
        "android.permission.BLUETOOTH_SCAN" -> "Modern Bluetooth permission for scanning and discovering nearby Bluetooth devices."
        "android.permission.ACCESS_FINE_LOCATION" -> "Provides precise location access using GPS. Used for location-based features and accurate positioning."
        "android.permission.ACCESS_COARSE_LOCATION" -> "Provides approximate location access using network-based positioning."
        "android.permission.SYSTEM_ALERT_WINDOW" -> "Allows the app to display overlay windows on top of other applications. Required for launcher functionality."
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" -> "Enables access to system notifications for the notification panel feature."
        "android.permission.READ_PHONE_STATE" -> "Provides access to phone and network information for signal strength monitoring."
        "android.permission.CAMERA" -> "Enables camera access for photo and video capture features."
        "android.permission.WRITE_SETTINGS" -> "Allows modification of system settings like brightness, volume, and other system configurations."
        else -> "Permission required for app functionality."
    }
}

@Composable
private fun SystemContent(
    appSettings: List<com.win11launcher.data.entities.AppSetting>,
    onToggleAutoStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "System",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "System settings and configuration",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun DisplayContent(
    appSettings: List<com.win11launcher.data.entities.AppSetting>,
    onThemeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Display",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Display settings and appearance",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun NetworkContent(systemStatusManager: SystemStatusManager) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Network",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Network and connectivity settings",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AppsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Apps",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "App management and settings",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AboutContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "About",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "App information and version details",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

data class PermissionItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val isRequired: Boolean
)

private fun getPermissionItems(
    context: android.content.Context,
    systemStatusManager: SystemStatusManager
): List<PermissionItem> {
    val packageManager = context.packageManager
    
    return listOf(
        PermissionItem(
            name = "WiFi Control",
            description = "Allows the app to view and control WiFi connections. Required for WiFi toggle functionality and network status monitoring.",
            icon = Icons.Default.Wifi,
            isGranted = true, // These are automatically granted
            isRequired = true
        ),
        PermissionItem(
            name = "Bluetooth",
            description = "Enables Bluetooth device management, connection status monitoring, and toggle functionality.",
            icon = Icons.Default.Bluetooth,
            isGranted = systemStatusManager.getBluetoothManager().bluetoothInfo.value.isSupported,
            isRequired = false
        ),
        PermissionItem(
            name = "Location Services",
            description = "Provides access to device location for location-based features and GPS status monitoring.",
            icon = Icons.Default.LocationOn,
            isGranted = systemStatusManager.getLocationManager().locationInfo.value.hasPermission,
            isRequired = false
        ),
        PermissionItem(
            name = "Notification Access",
            description = "Required to display and manage system notifications in the notification panel.",
            icon = Icons.Default.Notifications,
            isGranted = try {
                packageManager.checkPermission(
                    android.Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                    context.packageName
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) { false },
            isRequired = true
        ),
        PermissionItem(
            name = "System Alert Window",
            description = "Allows the app to display over other apps. Used for launcher overlay functionality.",
            icon = Icons.Default.Window,
            isGranted = try {
                packageManager.checkPermission(
                    android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                    context.packageName
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) { false },
            isRequired = false
        ),
        PermissionItem(
            name = "Device Administrator",
            description = "Required for advanced system functions like device power management and screen lock.",
            icon = Icons.Default.AdminPanelSettings,
            isGranted = false, // This requires manual activation
            isRequired = false
        ),
        PermissionItem(
            name = "Phone State",
            description = "Provides access to phone and network information for mobile signal strength monitoring.",
            icon = Icons.Default.Phone,
            isGranted = try {
                packageManager.checkPermission(
                    android.Manifest.permission.READ_PHONE_STATE,
                    context.packageName
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) { false },
            isRequired = false
        ),
        PermissionItem(
            name = "Battery Optimization",
            description = "Prevents the system from putting the launcher to sleep, ensuring consistent performance.",
            icon = Icons.Default.BatteryFull,
            isGranted = true, // Assume granted for now
            isRequired = false
        )
    )
}