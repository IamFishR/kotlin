package com.win11launcher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StartMenu(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onAllAppsClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val appRepository = remember { AppRepository(context) }
    var searchQuery by remember { mutableStateOf("") }
    var showAllApps by remember { mutableStateOf(false) }
    
    // Get user profile data
    val userProfile by settingsViewModel.userProfile.collectAsStateWithLifecycle()
    val userCustomization by settingsViewModel.userCustomization.collectAsStateWithLifecycle()
    
    // Load installed apps when component is created
    LaunchedEffect(Unit) {
        appRepository.loadInstalledApps()
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
                    .padding(bottom = 16.dp),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
            
            // Show search results, all apps, or pinned apps
            if (searchQuery.isNotEmpty()) {
                SearchResultsSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    searchResults = filteredApps,
                    appRepository = appRepository
                )
            } else {
                AnimatedContent(
                    targetState = showAllApps,
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { if (targetState) it else -it }) with
                        slideOutHorizontally(targetOffsetX = { if (targetState) -it else it })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BottomActions(
                modifier = Modifier.fillMaxWidth(),
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
    
    Row(modifier = modifier) {
        // Main content area
        Column(modifier = Modifier.weight(1f)) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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
                            imageVector = Icons.Default.ArrowBack,
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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
            }
        }
        
        // Alphabet navigation sidebar
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
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
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Color(0xFF323233),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
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
                    color = Color(0xFF999999),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { app ->
                    SearchResultAppIcon(
                        app = app,
                        onClick = { appRepository.launchApp(app) }
                    )
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
            // Use a default icon for now since we can't easily convert Drawable to ImageVector
            Icon(
                imageVector = Icons.Default.Apps,
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
                    onClick = { 
                        when (app.launchAction) {
                            else -> appLauncher.launchApp(app)
                        }
                    }
                )
            }
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
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show profile picture if enabled and available
            if (userCustomization?.showUserPictureInStartMenu != false) {
                if (!userProfile?.profilePicturePath.isNullOrEmpty() && 
                    File(userProfile?.profilePicturePath ?: "").exists()) {
                    AsyncImage(
                        model = File(userProfile?.profilePicturePath ?: ""),
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
                    text = userProfile?.displayName?.takeIf { it.isNotEmpty() } 
                        ?: userProfile?.username ?: "User",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
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