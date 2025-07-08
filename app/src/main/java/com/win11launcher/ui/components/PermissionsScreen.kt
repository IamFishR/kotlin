package com.win11launcher.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.win11launcher.ui.layout.LayoutConstants

data class Permission(
    val name: String,
    val manifestName: String,
    val description: String,
    val isRequired: Boolean = true,
    val requiresSpecialHandling: Boolean = false
)

@Composable
fun PermissionsScreen(
    onAllPermissionsGranted: () -> Unit,
    onRequestPermissions: (List<String>) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    
    val permissions = remember {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Permission(
                "Media Images Access",
                Manifest.permission.READ_MEDIA_IMAGES,
                "Required to display your wallpaper"
            )
        } else {
            Permission(
                "Storage Access",
                Manifest.permission.READ_EXTERNAL_STORAGE,
                "Required to display your wallpaper"
            )
        }
        
        val permissions = mutableListOf(
            storagePermission,
            Permission(
                "Phone State",
                Manifest.permission.READ_PHONE_STATE,
                "Required to show network and battery status"
            ),
            Permission(
                "System Alert Window",
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                "Required to show launcher overlay",
                requiresSpecialHandling = true
            ),
            Permission(
                "Write Settings",
                Manifest.permission.WRITE_SETTINGS,
                "Required to modify system settings",
                requiresSpecialHandling = true
            ),
            Permission(
                "Camera",
                Manifest.permission.CAMERA,
                "Required for camera quick access",
                isRequired = false
            )
        )
        
        // Add All Files Access for Android 11+ (required for AI model access)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.add(
                Permission(
                    "All Files Access",
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    "Required to access AI model files and full storage",
                    requiresSpecialHandling = true
                )
            )
        }
        
        permissions
    }
    
    var permissionStatuses by remember { mutableStateOf(emptyMap<String, Boolean>()) }
    
    // Check permissions
    LaunchedEffect(Unit) {
        val statuses = permissions.associate { permission ->
            permission.manifestName to when {
                permission.requiresSpecialHandling -> {
                    when (permission.manifestName) {
                        Manifest.permission.SYSTEM_ALERT_WINDOW -> 
                            Settings.canDrawOverlays(context)
                        Manifest.permission.WRITE_SETTINGS -> 
                            Settings.System.canWrite(context)
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                Environment.isExternalStorageManager()
                            } else {
                                true // Not needed on older Android versions
                            }
                        }
                        else -> false
                    }
                }
                else -> {
                    ContextCompat.checkSelfPermission(context, permission.manifestName) == 
                        PackageManager.PERMISSION_GRANTED
                }
            }
        }
        permissionStatuses = statuses
    }
    
    val allRequiredPermissionsGranted = permissions
        .filter { it.isRequired }
        .all { permissionStatuses[it.manifestName] == true }
    
    LaunchedEffect(allRequiredPermissionsGranted) {
        if (allRequiredPermissionsGranted) {
            onAllPermissionsGranted()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LayoutConstants.SPACING_EXTRA_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(LayoutConstants.PERMISSIONS_SCREEN_TOP_SPACING))
        
        Text(
            text = "Win11 Launcher Setup",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_MEDIUM))
        
        Text(
            text = "Please grant the following permissions to use all features",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_EXTRA_LARGE + LayoutConstants.SPACING_MEDIUM))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(permissions) { permission ->
                PermissionItem(
                    permission = permission,
                    isGranted = permissionStatuses[permission.manifestName] ?: false,
                    onRequestPermission = { 
                        if (permission.requiresSpecialHandling) {
                            when (permission.manifestName) {
                                Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    intent.data = Uri.parse("package:${context.packageName}")
                                    context.startActivity(intent)
                                }
                                Manifest.permission.WRITE_SETTINGS -> {
                                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                    intent.data = Uri.parse("package:${context.packageName}")
                                    context.startActivity(intent)
                                }
                                Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                        intent.data = Uri.parse("package:${context.packageName}")
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        } else {
                            onRequestPermissions(listOf(permission.manifestName))
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_EXTRA_LARGE))
        
        if (allRequiredPermissionsGranted) {
            Button(
                onClick = onAllPermissionsGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LayoutConstants.BUTTON_HEIGHT_HUGE),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0078D4)
                )
            ) {
                Text(
                    text = "Continue to Launcher",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Text(
                text = "Grant all required permissions to continue",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_LARGE))
        
        TextButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(LayoutConstants.ICON_MEDIUM)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open App Settings")
        }
        
        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_LARGE))
    }
}

@Composable
private fun PermissionItem(
    permission: Permission,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) 
                Color(0xFF0078D4).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(LayoutConstants.PERMISSIONS_SCREEN_CARD_RADIUS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LayoutConstants.SPACING_LARGE),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF0078D4) else Color(0xFFF7630C),
                modifier = Modifier.size(LayoutConstants.ICON_EXTRA_LARGE)
            )
            
            Spacer(modifier = Modifier.width(LayoutConstants.SPACING_LARGE))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = permission.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (!permission.isRequired) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optional",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    RoundedCornerShape(LayoutConstants.PERMISSIONS_SCREEN_BADGE_RADIUS)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = permission.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (!isGranted) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0078D4)
                    ),
                    modifier = Modifier.height(LayoutConstants.BUTTON_HEIGHT_LARGE)
                ) {
                    Text(
                        text = "Grant",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}