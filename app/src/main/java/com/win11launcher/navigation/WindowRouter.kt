package com.win11launcher.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.win11launcher.models.SnapPosition
import com.win11launcher.models.WindowEvent
import com.win11launcher.models.WindowState
import com.win11launcher.models.WindowStateType
import com.win11launcher.services.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Router for window-based navigation that replaces traditional Android Navigation.
 * Manages window creation, navigation between windows, and window lifecycle.
 */
@Singleton
class WindowRouter @Inject constructor(
    private val windowManager: WindowManager
) {
    
    // Navigation state
    private val _currentRoute = MutableStateFlow<String?>(null)
    val currentRoute: StateFlow<String?> = _currentRoute.asStateFlow()
    
    // Window backstack for navigation
    private val _backStack = MutableStateFlow<List<String>>(emptyList())
    val backStack: StateFlow<List<String>> = _backStack.asStateFlow()
    
    // Route-to-destination mapping
    private val destinationMap = mutableMapOf<String, WindowDestination>()
    
    // Window content composers
    private val contentComposers = mutableMapOf<String, @Composable () -> Unit>()
    
    init {
        // Initialize default destinations
        registerDefaultDestinations()
    }
    
    /**
     * Navigates to a window destination
     */
    fun navigateTo(destination: WindowDestination, addToBackStack: Boolean = true) {
        val windowId = destination.windowId
        
        // Check if window already exists
        if (windowManager.hasWindow(windowId)) {
            // Just focus the existing window
            windowManager.focusWindow(windowId)
            _currentRoute.value = windowId
        } else {
            // Create new window
            val createdId = windowManager.createWindow(destination)
            _currentRoute.value = createdId
            
            // Add to backstack if requested
            if (addToBackStack) {
                addToBackStack(createdId)
            }
        }
    }
    
    /**
     * Navigates to a window by route string
     */
    fun navigateTo(route: String, addToBackStack: Boolean = true) {
        val destination = destinationMap[route]
        if (destination != null) {
            navigateTo(destination, addToBackStack)
        } else {
            // Try to find existing window
            if (windowManager.hasWindow(route)) {
                windowManager.focusWindow(route)
                _currentRoute.value = route
                if (addToBackStack) {
                    addToBackStack(route)
                }
            }
        }
    }
    
    /**
     * Navigates to a parameterized destination (like NoteDetail)
     */
    fun navigateToNoteDetail(noteId: String, noteTitle: String) {
        val destination = WindowDestination.NoteDetail(noteId, noteTitle)
        navigateTo(destination)
    }
    
    /**
     * Navigates to an external app
     */
    fun navigateToExternalApp(
        appId: String,
        appName: String,
        appIcon: androidx.compose.ui.graphics.vector.ImageVector,
        customConfig: WindowConfig? = null
    ) {
        val destination = WindowDestination.ExternalApp(
            appId = appId,
            appName = appName,
            appIcon = appIcon,
            customSize = customConfig?.initialSize
        )
        navigateTo(destination)
    }
    
    /**
     * Opens a modal dialog
     */
    fun openDialog(
        dialogId: String,
        dialogTitle: String,
        dialogIcon: androidx.compose.ui.graphics.vector.ImageVector,
        parentWindowId: String,
        size: DpSize = DpSize(400.dp, 300.dp)
    ) {
        val destination = WindowDestination.Dialog(
            dialogId = dialogId,
            dialogTitle = dialogTitle,
            dialogIcon = dialogIcon,
            parentWindowId = parentWindowId,
            customSize = size
        )
        navigateTo(destination, addToBackStack = false)
    }
    
    /**
     * Navigates back to the previous window
     */
    fun navigateBack(): Boolean {
        val currentBackStack = _backStack.value
        if (currentBackStack.size > 1) {
            val currentWindow = currentBackStack.last()
            val previousWindow = currentBackStack[currentBackStack.size - 2]
            
            // Close current window if it's closable
            val currentWindowState = windowManager.getWindow(currentWindow)
            if (currentWindowState?.isClosable == true) {
                windowManager.closeWindow(currentWindow)
            }
            
            // Focus previous window
            windowManager.focusWindow(previousWindow)
            _currentRoute.value = previousWindow
            
            // Update backstack
            _backStack.value = currentBackStack.dropLast(1)
            
            return true
        }
        return false
    }
    
    /**
     * Closes the current window and navigates back
     */
    fun closeCurrentWindow(): Boolean {
        val currentRoute = _currentRoute.value
        if (currentRoute != null) {
            windowManager.closeWindow(currentRoute)
            return navigateBack()
        }
        return false
    }
    
    /**
     * Registers a window destination with its route
     */
    fun registerDestination(route: String, destination: WindowDestination) {
        destinationMap[route] = destination
    }
    
    /**
     * Registers a content composer for a window
     */
    fun registerContent(windowId: String, content: @Composable () -> Unit) {
        contentComposers[windowId] = content
    }
    
    /**
     * Gets the content composer for a window
     */
    fun getContent(windowId: String): (@Composable () -> Unit)? {
        return contentComposers[windowId]
    }
    
    /**
     * Handles window events and updates navigation state
     */
    fun handleWindowEvent(event: WindowEvent) {
        when (event) {
            is WindowEvent.Close -> {
                // Remove from backstack if closed
                val currentBackStack = _backStack.value
                if (currentBackStack.contains(event.windowId)) {
                    _backStack.value = currentBackStack.filter { it != event.windowId }
                }
                
                // Update current route if this was the current window
                if (_currentRoute.value == event.windowId) {
                    val remainingWindows = _backStack.value
                    _currentRoute.value = remainingWindows.lastOrNull()
                }
            }
            
            is WindowEvent.Focus -> {
                _currentRoute.value = event.windowId
                addToBackStack(event.windowId)
            }
            
            else -> {
                // Let WindowManager handle other events
                windowManager.handleWindowEvent(event)
            }
        }
    }
    
    /**
     * Gets all open windows
     */
    fun getOpenWindows(): List<WindowState> {
        return windowManager.getVisibleWindows()
    }
    
    /**
     * Gets the currently focused window
     */
    fun getCurrentWindow(): WindowState? {
        return windowManager.getFocusedWindow()
    }
    
    /**
     * Checks if a specific window is open
     */
    fun isWindowOpen(windowId: String): Boolean {
        return windowManager.hasWindow(windowId)
    }
    
    /**
     * Minimizes all windows
     */
    fun minimizeAllWindows() {
        windowManager.windows.values.forEach { window ->
            if (window.isMinimizable) {
                windowManager.minimizeWindow(window.id)
            }
        }
    }
    
    /**
     * Restores all minimized windows
     */
    fun restoreAllWindows() {
        windowManager.windows.values
            .filter { it.state == WindowStateType.MINIMIZED }
            .forEach { window ->
                windowManager.restoreWindow(window.id)
            }
    }
    
    /**
     * Closes all closable windows
     */
    fun closeAllWindows() {
        windowManager.windows.values
            .filter { it.isClosable }
            .forEach { window ->
                windowManager.closeWindow(window.id)
            }
    }
    
    /**
     * Arranges windows in a cascade pattern
     */
    fun cascadeWindows() {
        val visibleWindows = windowManager.getVisibleWindows()
        var offsetX = 50f
        var offsetY = 50f
        
        visibleWindows.forEach { window ->
            if (window.canMove()) {
                windowManager.moveWindow(window.id, Offset(offsetX, offsetY))
                offsetX += 30f
                offsetY += 30f
            }
        }
    }
    
    /**
     * Arranges windows in a tile pattern
     */
    fun tileWindows() {
        val visibleWindows = windowManager.getVisibleWindows()
        if (visibleWindows.isEmpty()) return
        
        val screenSize = windowManager.screenSize
        val cols = kotlin.math.ceil(kotlin.math.sqrt(visibleWindows.size.toDouble())).toInt()
        val rows = kotlin.math.ceil(visibleWindows.size.toDouble() / cols).toInt()
        
        val windowWidth = screenSize.width / cols
        val windowHeight = screenSize.height / rows
        
        visibleWindows.forEachIndexed { index, window ->
            val col = index % cols
            val row = index / cols
            
            val x = col * windowWidth.value
            val y = row * windowHeight.value
            
            if (window.canMove() && window.canResize()) {
                windowManager.moveWindow(window.id, Offset(x, y))
                windowManager.resizeWindow(window.id, DpSize(windowWidth, windowHeight))
            }
        }
    }
    
    // Private helper methods
    
    private fun addToBackStack(windowId: String) {
        val currentBackStack = _backStack.value.toMutableList()
        
        // Remove if already exists to avoid duplicates
        currentBackStack.remove(windowId)
        
        // Add to end
        currentBackStack.add(windowId)
        
        // Limit backstack size
        if (currentBackStack.size > 10) {
            currentBackStack.removeAt(0)
        }
        
        _backStack.value = currentBackStack
    }
    
    private fun registerDefaultDestinations() {
        // Register built-in destinations
        registerDestination("home", WindowDestination.Home)
        registerDestination("notes_hub", WindowDestination.NotesHub)
        registerDestination("rule_wizard", WindowDestination.RuleWizard)
        registerDestination("file_manager", WindowDestination.FileManager)
        registerDestination("settings", WindowDestination.Settings)
        registerDestination("all_apps", WindowDestination.AllApps)
    }
}

/**
 * Composable function to handle window routing
 */
@Composable
fun WindowRouterHost(
    windowRouter: WindowRouter,
    windowManager: WindowManager,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val openWindows by remember { 
        derivedStateOf { windowManager.getVisibleWindows() }
    }
    
    // Render all open windows
    openWindows.forEach { windowState ->
        val content = windowRouter.getContent(windowState.id)
        
        if (content != null) {
            com.win11launcher.ui.window.WindowContainer(
                windowState = windowState,
                onEvent = { event -> windowRouter.handleWindowEvent(event) },
                modifier = modifier
            ) {
                content()
            }
        }
    }
}

/**
 * Hook for accessing the window router in composables
 */
@Composable
fun rememberWindowRouter(): WindowRouter {
    return remember { 
        // This should be injected via DI in a real app
        WindowRouter(WindowManager())
    }
}