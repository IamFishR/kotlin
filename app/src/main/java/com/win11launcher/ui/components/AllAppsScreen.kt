package com.win11launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.win11launcher.data.AppRepository
import com.win11launcher.data.InstalledApp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.win11launcher.ui.layout.LayoutConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAppsScreen(
    modifier: Modifier = Modifier,
    appRepository: AppRepository,
    onBackClick: () -> Unit
) {
    val installedApps by appRepository.installedApps
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        appRepository.loadInstalledApps()
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isEmpty()) {
            installedApps
        } else {
            installedApps.filter { app ->
                app.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .heightIn(min = LayoutConstants.LAYOUT_HEIGHT_MEDIUM) // Increased minimum height for better usability
            .background(Color(0xFF232323).copy(alpha = 0.9f))
            .border(
                width = LayoutConstants.WINDOW_BORDER_WIDTH,
                color = Color.White.copy(alpha = 0.1f)
            )
            .padding(LayoutConstants.SPACING_EXTRA_LARGE) // Increased padding for better spacing
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(LayoutConstants.BUTTON_HEIGHT_EXTRA_LARGE)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(LayoutConstants.SPACING_MEDIUM))
            
            Text(
                text = "All apps",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_LARGE))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search for apps",
                    color = Color(0xFFCCCCCC)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFFCCCCCC)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0078D4),
                unfocusedBorderColor = Color(0xFF666666),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF0078D4)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_LARGE))

        // Apps count
        Text(
            text = "${filteredApps.size} apps",
            color = Color(0xFFCCCCCC),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = LayoutConstants.SPACING_MEDIUM, start = LayoutConstants.SPACING_LARGE, end = LayoutConstants.SPACING_LARGE)
        )

        // Apps grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            contentPadding = PaddingValues(horizontal = LayoutConstants.SPACING_LARGE, vertical = LayoutConstants.SPACING_MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_LARGE),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredApps) { app ->
                AppItem(
                    app = app,
                    onClick = { appRepository.launchApp(app) }
                )
            }
        }
    }
}

@Composable
private fun AppItem(
    app: InstalledApp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(LayoutConstants.SPACING_SMALL)
    ) {
        // App icon
        app.iconDrawable?.let { drawable ->
            Image(
                painter = rememberDrawablePainter(drawable),
                contentDescription = app.name,
                modifier = Modifier
                    .size(LayoutConstants.ICON_MASSIVE)
                    .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM))
            )
        } ?: Box(
            modifier = Modifier
                .size(LayoutConstants.ICON_MASSIVE)
                .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM))
                .background(Color(0xFF444444)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size(LayoutConstants.ICON_EXTRA_LARGE)
            )
        }
        
        Spacer(modifier = Modifier.height(LayoutConstants.SPACING_SMALL))
        
        // App name
        Text(
            text = app.name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(LayoutConstants.LAYOUT_WIDTH_SMALL)
        )
    }
}
