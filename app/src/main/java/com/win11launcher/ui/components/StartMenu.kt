package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.win11launcher.utils.AppLauncher
import com.win11launcher.utils.PinnedApp

data class AppItem(
    val name: String,
    val icon: ImageVector,
    val packageName: String = ""
)

@Composable
fun StartMenu(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onAllAppsClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF232323)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            PinnedAppsSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onAllAppsClick = onAllAppsClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BottomActions(
                modifier = Modifier.fillMaxWidth(),
                onPowerClick = onDismiss
            )
        }
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = "",
        onValueChange = { },
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search for apps, settings, and documents",
                color = Color(0xFF999999)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF999999)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0078D4),
            unfocusedBorderColor = Color(0xFF404040),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF0078D4)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun PinnedAppsSection(
    modifier: Modifier = Modifier,
    onAllAppsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appLauncher = remember { AppLauncher(context) }
    val pinnedApps = remember { appLauncher.getPinnedApps() }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pinned",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(
                onClick = onAllAppsClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF0078D4)
                )
            ) {
                Text(
                    text = "All apps",
                    fontSize = 14.sp
                )
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pinnedApps) { app ->
                PinnedAppIcon(
                    app = app,
                    onClick = { appLauncher.launchApp(app) }
                )
            }
        }
    }
}

@Composable
private fun AppIcon(
    app: AppItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFF323233),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun PinnedAppIcon(
    app: PinnedApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFF323233),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun BottomActions(
    modifier: Modifier = Modifier,
    onPowerClick: () -> Unit = {}
) {
    var showPowerMenu by remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "User",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
        
        Box {
            IconButton(
                onClick = { showPowerMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = Color.White
                )
            }
            
            PowerMenu(
                showMenu = showPowerMenu,
                onDismiss = { 
                    showPowerMenu = false
                    onPowerClick()
                }
            )
        }
    }
}