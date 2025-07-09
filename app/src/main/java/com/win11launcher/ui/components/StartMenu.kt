package com.win11launcher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.abs
import kotlin.math.sign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.win11launcher.ui.components.AppIcon
import com.win11launcher.ui.components.AppIconMedium
import com.win11launcher.ui.components.PowerMenu
import com.win11launcher.ui.layout.LayoutConstants
import com.win11launcher.utils.AppLauncher
import com.win11launcher.utils.PinnedApp
import com.win11launcher.data.AppRepository
import com.win11launcher.data.InstalledApp
import com.win11launcher.data.entities.UserProfile
import com.win11launcher.data.entities.UserCustomization
import com.win11launcher.viewmodels.SettingsViewModel
import java.io.File

data class AppItem(
    val name: String,
    val icon: ImageVector,
    val packageName: String = ""
)

enum class SwipeDirection {
    LEFT, RIGHT
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StartMenu(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onAllAppsClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val appRepository = remember { AppRepository(context) }
    var searchQuery by remember { mutableStateOf("") }
    var showAllApps by remember { mutableStateOf(false) }
    
    // Swipe state management
    var swipeOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Calculate swipe threshold based on screen density
    val swipeThreshold = with(density) { 120.dp.toPx() }
    val maxSwipeDistance = with(density) { 200.dp.toPx() }
    
    // Animated swipe progress (0f to 1f)
    val swipeProgress by animateFloatAsState(
        targetValue = when {
            isDragging -> (abs(swipeOffset) / swipeThreshold).coerceIn(0f, 1f)
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "swipe_progress"
    )
    
    // Reset swipe offset when not dragging
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            swipeOffset = 0f
        }
    }
    
    // Get user profile data
    val userProfile by settingsViewModel.userProfile.collectAsStateWithLifecycle()
    val userCustomization by settingsViewModel.userCustomization.collectAsStateWithLifecycle()
    
    // Load installed apps when component is created
    LaunchedEffect(Unit) {
        try {
            appRepository.loadInstalledApps()
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    val installedApps by appRepository.installedApps
    
    // Filter apps based on search query
    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            installedApps.filter { app ->
                app.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(LayoutConstants.WINDOW_CORNER_RADIUS))
            .border(
                width = LayoutConstants.WINDOW_BORDER_WIDTH,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(LayoutConstants.WINDOW_CORNER_RADIUS)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF232323).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
//                    .padding(bottom = 60.dp) // Space for bottom actions
            ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = LayoutConstants.SPACING_LARGE,
                        top = LayoutConstants.SPACING_LARGE,
                        start = LayoutConstants.SPACING_EXTRA_LARGE,
                        end = LayoutConstants.SPACING_EXTRA_LARGE
                    ),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
            
            // Show search results, all apps, or pinned apps
            if (searchQuery.isNotEmpty()) {
                SearchResultsSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    searchResults = filteredApps,
                    appRepository = appRepository
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    // Enhanced swipe gesture handling
                    AnimatedContent(
                        targetState = showAllApps,
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { if (targetState) it else -it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) with slideOutHorizontally(
                                targetOffsetX = { if (targetState) -it else it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .graphicsLayer {
                                // Apply real-time swipe transformation
                                translationX = when {
                                    isDragging -> swipeOffset.coerceIn(-maxSwipeDistance, maxSwipeDistance)
                                    else -> 0f
                                }
                                
                                // Subtle scale effect during swipe
                                val scaleEffect = 1f - (swipeProgress * 0.02f)
                                scaleX = scaleEffect
                                scaleY = scaleEffect
                                
                                // Subtle alpha effect during swipe
                                alpha = 1f - (swipeProgress * 0.1f)
                            }
                            .pointerInput(showAllApps) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDragging = true
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        
                                        // Determine if swipe should trigger transition
                                        val shouldTransition = abs(swipeOffset) > swipeThreshold
                                        
                                        if (shouldTransition) {
                                            // Swipe left (negative) to go to All Apps
                                            if (swipeOffset < 0 && !showAllApps) {
                                                showAllApps = true
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                            // Swipe right (positive) to go back to Pinned Apps
                                            else if (swipeOffset > 0 && showAllApps) {
                                                showAllApps = false
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                        
                                        // Reset swipe offset
                                        swipeOffset = 0f
                                    }
                                ) { change, dragAmount ->
                                    // Accumulate drag distance
                                    swipeOffset += dragAmount.x
                                    
                                    // Provide haptic feedback at threshold
                                    if (abs(swipeOffset) >= swipeThreshold && abs(swipeOffset - dragAmount.x) < swipeThreshold) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                    ) { isShowingAllApps ->
                        if (isShowingAllApps) {
                            AllAppsView(
                                modifier = Modifier.fillMaxSize(),
                                installedApps = installedApps,
                                appRepository = appRepository,
                                onBackClick = { showAllApps = false }
                            )
                        } else {
                            PinnedAppsSection(
                                modifier = Modifier.fillMaxSize(),
                                onAllAppsClick = { showAllApps = true },
                            )
                        }
                    }
                    
                    // Swipe direction indicator
                    if (isDragging && swipeProgress > 0.2f) {
                        SwipeIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            progress = swipeProgress,
                            direction = if (swipeOffset < 0) SwipeDirection.LEFT else SwipeDirection.RIGHT,
                            isShowingAllApps = showAllApps
                        )
                    }
                }
            }
            }
            
            // Bottom actions positioned at the bottom
            BottomActions(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                userProfile = userProfile,
                userCustomization = userCustomization,
                onPowerClick = onDismiss
            )
        }
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search for apps, settings",
                color = Color(0xFFCCCCCC)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFFCCCCCC)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0078D4),
            unfocusedBorderColor = Color(0xFF404040),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF0078D4)
        ),
        shape = RoundedCornerShape(LayoutConstants.SPACING_MEDIUM)
    )
}

@Composable
private fun AllAppsView(
    modifier: Modifier = Modifier,
    installedApps: List<InstalledApp>,
    appRepository: AppRepository,
    onBackClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Alphabetically grouped apps
    val groupedApps = remember(installedApps) {
        installedApps
            .sortedBy { it.name }
            .groupBy { it.name.first().uppercaseChar() }
            .toSortedMap()
    }
    
    // Create a map of letter to item index for quick scrolling
    val letterToIndex = remember(groupedApps) {
        var index = 0
        groupedApps.keys.associateWith { letter ->
            val currentIndex = index
            index += 1 + (groupedApps[letter]?.size ?: 0) // +1 for header
            currentIndex
        }
    }
    
    Row(
        modifier = modifier.fillMaxSize() // Ensure full space utilization
    ) {
        // Main content area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Text(
                        text = "All apps",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Apps list with scrollbar
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(), // Ensure the LazyColumn fills available space
                verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_MEDIUM),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                groupedApps.forEach { (letter, apps) ->
                    item {
                        // Letter header
                        Text(
                            text = letter.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(apps) { app ->
                        AllAppsItem(
                            app = app,
                            onClick = { appRepository.launchApp(app) }
                        )
                    }
                }
                
                // Add a spacer at the very end to push content to the bottom
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        
        // Alphabet navigation sidebar
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
        ) {
            groupedApps.keys.forEach { letter ->
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0078D4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            letterToIndex[letter]?.let { index ->
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                        }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AllAppsItem(
    app: InstalledApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM))
            .clickable { onClick() }
            .padding(horizontal = LayoutConstants.SPACING_MEDIUM, vertical = LayoutConstants.SPACING_MEDIUM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon using real app icon component
        AppIcon(
            app = app,
            size = 40.dp,
            iconSize = 20.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // App name
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun SearchResultsSection(
    modifier: Modifier = Modifier,
    searchResults: List<InstalledApp>,
    appRepository: AppRepository
) {
    Column(modifier = modifier) {
        Text(
            text = "Apps (${searchResults.size})",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No apps found",
                    color = Color(0xFFCCCCCC),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // Regular Grid layout instead of LazyVerticalGrid to avoid infinite height constraints
            val searchRows = (searchResults.size + 5) / 6 // Calculate rows needed for 6 columns
            Column(
                verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_MEDIUM),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                repeat(searchRows) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_MEDIUM)
                    ) {
                        repeat(6) { columnIndex ->
                            val appIndex = rowIndex * 6 + columnIndex
                            if (appIndex < searchResults.size) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SearchResultAppIcon(
                                        app = searchResults[appIndex],
                                        onClick = { appRepository.launchApp(searchResults[appIndex]) }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultAppIcon(
    app: InstalledApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM))
            .clickable { onClick() }
            .padding(LayoutConstants.SPACING_MEDIUM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App icon using real app icon component
        AppIconMedium(
            app = app
        )
        
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
private fun PinnedAppsSection(
    modifier: Modifier = Modifier,
    onAllAppsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appLauncher = remember { AppLauncher(context) }
    val pinnedApps = remember { appLauncher.getPinnedApps() }
    val recommendedApps = remember { appLauncher.getRecommendedApps() }
    
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        // Pinned Apps Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
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
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Regular Grid layout instead of LazyVerticalGrid to avoid infinite height constraints
        val rows = (pinnedApps.size + 3) / 4 // Calculate rows needed for 4 columns
        Column(
            verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_MEDIUM),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .heightIn(min = 200.dp) // Minimum height for pinned apps section
        ) {
            repeat(rows) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_MEDIUM)
                ) {
                    repeat(4) { columnIndex ->
                        val appIndex = rowIndex * 4 + columnIndex
                        if (appIndex < pinnedApps.size) {
                            Box(modifier = Modifier.weight(1f)) {
                                PinnedAppIcon(
                                    app = pinnedApps[appIndex],
                                    onClick = { 
                                        when (pinnedApps[appIndex].launchAction) {
                                            else -> appLauncher.launchApp(pinnedApps[appIndex])
                                        }
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recommended Apps Section
        Text(
            text = "Recommended",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
        )
        
        // Regular Grid layout instead of LazyVerticalGrid to avoid infinite height constraints
        val recommendedRows = (recommendedApps.size + 1) / 2 // Calculate rows needed for 2 columns
        Column(
            verticalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_MEDIUM),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .heightIn(min = 120.dp) // Minimum height for recommended section
        ) {
            repeat(recommendedRows) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LayoutConstants.SPACING_MEDIUM)
                ) {
                    repeat(2) { columnIndex ->
                        val appIndex = rowIndex * 2 + columnIndex
                        if (appIndex < recommendedApps.size) {
                            Box(modifier = Modifier.weight(1f)) {
                                RecommendedAppItem(
                                    app = recommendedApps[appIndex],
                                    onClick = { 
                                        when (recommendedApps[appIndex].launchAction) {
                                            else -> appLauncher.launchApp(recommendedApps[appIndex])
                                        }
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedAppItem(
    app: PinnedApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM))
            .clickable { onClick() }
            .padding(LayoutConstants.SPACING_MEDIUM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    Color(0xFF323233),
                    RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app.icon != null) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = app.name,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else if (app.iconRes != null) {
                Icon(
                    painter = painterResource(app.iconRes),
                    contentDescription = app.name,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = when (app.name) {
                    "Mail" -> "1m ago"
                    "Calendar" -> "5m ago" 
                    "Music" -> "2h ago"
                    "Videos" -> "3m ago"
                    else -> "Now"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCCCCCC),
                fontSize = 11.sp
            )
        }
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
            .clip(RoundedCornerShape(LayoutConstants.SPACING_MEDIUM))
            .clickable { onClick() }
            .padding(LayoutConstants.SPACING_MEDIUM),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFF323233),
                    RoundedCornerShape(LayoutConstants.SPACING_MEDIUM)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app.icon != null) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = app.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else if (app.iconRes != null) {
                Icon(
                    painter = painterResource(app.iconRes),
                    contentDescription = app.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
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
    userProfile: UserProfile?,
    userCustomization: UserCustomization?,
    onPowerClick: () -> Unit = {}
) {
    var showPowerMenu by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .background(
                Color(0xFF1A1A1A).copy(alpha = 0.8f),
                RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show profile picture if enabled and available
            if (userCustomization?.showUserPictureInStartMenu != false) {
                val profilePicturePath = userProfile?.profilePicturePath
                if (!profilePicturePath.isNullOrEmpty() && 
                    try { File(profilePicturePath).exists() } catch (e: Exception) { false }) {
                    AsyncImage(
                        model = File(profilePicturePath),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Show username if enabled
            if (userCustomization?.showUsernameInStartMenu != false) {
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = userProfile?.displayName?.takeIf { !it.isNullOrEmpty() } 
                        ?: userProfile?.username?.takeIf { !it.isNullOrEmpty() } ?: "User",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
        
        // Bottom actions section - outside scrollable content
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

@Composable
private fun SwipeIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    direction: SwipeDirection,
    isShowingAllApps: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 100),
        label = "indicator_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = 0.8f + (progress * 0.2f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicator_scale"
    )
    
    Box(
        modifier = modifier
            .size(80.dp)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .background(
                Color.Black.copy(alpha = 0.6f),
                RoundedCornerShape(40.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (direction) {
                SwipeDirection.LEFT -> if (isShowingAllApps) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward
                SwipeDirection.RIGHT -> if (isShowingAllApps) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward
            },
            contentDescription = when (direction) {
                SwipeDirection.LEFT -> if (isShowingAllApps) "Back to Pinned" else "Go to All Apps"
                SwipeDirection.RIGHT -> if (isShowingAllApps) "Back to Pinned" else "Go to All Apps"
            },
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp)
        )
    }
}