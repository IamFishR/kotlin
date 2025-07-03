package com.win11launcher.services

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.win11launcher.models.SnapPosition
import com.win11launcher.models.WindowEvent
import com.win11launcher.models.WindowState
import com.win11launcher.models.WindowStateType
import com.win11launcher.navigation.WindowConfig
import com.win11launcher.navigation.WindowDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core window management service that handles all window lifecycle operations.
 * This service manages window state, focus, positioning, and provides the main
 * interface for window operations in the Windows-like system.
 */
@Singleton
class WindowManager @Inject constructor() {
    
    // Active windows state
    private val _windows = mutableStateMapOf<String, WindowState>()
    val windows: Map<String, WindowState> = _windows
    
    // Current focused window
    private val _focusedWindowId = MutableStateFlow<String?>(null)
    val focusedWindowId: StateFlow<String?> = _focusedWindowId.asStateFlow()
    
    // Window z-index counter for proper layering
    private var nextZIndex = 0
    
    // Event listeners
    private val eventListeners = mutableSetOf<(WindowEvent) -> Unit>()
    
    // Screen dimensions for snapping calculations
    private val _screenSize = mutableStateOf(DpSize(1920.dp, 1080.dp))
    val screenSize: DpSize get() = _screenSize.value
    
    /**
     * Creates a new window from a destination configuration
     */
    fun createWindow(destination: WindowDestination): String {
        val config = destination.toWindowConfig()
        return createWindow(config)
    }
    
    /**
     * Creates a new window from a configuration
     */
    fun createWindow(config: WindowConfig): String {
        val windowState = WindowState.fromConfig(config).copy(
            zIndex = nextZIndex++,
            hasFocus = true,
            isVisible = true
        )
        
        _windows[config.windowId] = windowState
        
        // Focus the new window
        focusWindow(config.windowId)
        
        // Notify listeners
        notifyListeners(WindowEvent.Focus(config.windowId))
        
        return config.windowId
    }
    
    /**
     * Gets a window by its ID
     */
    fun getWindow(windowId: String): WindowState? {
        return _windows[windowId]
    }
    
    /**
     * Gets all windows sorted by z-index (top to bottom)
     */
    fun getWindowsSortedByZIndex(): List<WindowState> {
        return _windows.values.sortedByDescending { it.zIndex }
    }
    
    /**
     * Gets all visible windows
     */
    fun getVisibleWindows(): List<WindowState> {
        return _windows.values.filter { it.isVisible && it.state != WindowStateType.MINIMIZED }
    }
    
    /**
     * Gets all windows shown in taskbar
     */
    fun getTaskbarWindows(): List<WindowState> {
        return _windows.values.filter { it.showInTaskbar }
    }
    
    /**
     * Focuses a window, bringing it to the front
     */
    fun focusWindow(windowId: String) {
        val window = _windows[windowId] ?: return
        
        // Update focus state for all windows
        _windows.forEach { (id, state) ->
            _windows[id] = state.withFocus(id == windowId)
        }
        
        // Update the focused window and bring to front
        _windows[windowId] = window.withFocus(true).withZIndex(nextZIndex++)
        _focusedWindowId.value = windowId
        
        notifyListeners(WindowEvent.BringToFront(windowId))
    }
    
    /**
     * Moves a window to a new position
     */
    fun moveWindow(windowId: String, position: Offset) {
        val window = _windows[windowId] ?: return
        
        if (!window.canMove()) return
        
        _windows[windowId] = window.withPosition(position)
        notifyListeners(WindowEvent.Move(windowId, position))
    }
    
    /**
     * Resizes a window to a new size
     */
    fun resizeWindow(windowId: String, size: DpSize) {
        val window = _windows[windowId] ?: return
        
        if (!window.canResize()) return
        
        _windows[windowId] = window.withSize(size)
        notifyListeners(WindowEvent.Resize(windowId, size))
    }
    
    /**
     * Minimizes a window
     */
    fun minimizeWindow(windowId: String) {
        val window = _windows[windowId] ?: return
        
        if (!window.isMinimizable) return
        
        _windows[windowId] = window.withState(WindowStateType.MINIMIZED)
        
        // Focus next available window
        focusNextAvailableWindow()
        
        notifyListeners(WindowEvent.Minimize(windowId))
    }
    
    /**
     * Maximizes a window
     */
    fun maximizeWindow(windowId: String) {
        val window = _windows[windowId] ?: return
        
        if (!window.isMaximizable) return
        
        _windows[windowId] = window.withState(WindowStateType.MAXIMIZED)
        notifyListeners(WindowEvent.Maximize(windowId))
    }
    
    /**
     * Restores a window from minimized or maximized state
     */
    fun restoreWindow(windowId: String) {
        val window = _windows[windowId] ?: return
        
        _windows[windowId] = window.withState(WindowStateType.NORMAL)
        
        // Focus the restored window
        focusWindow(windowId)
        
        notifyListeners(WindowEvent.Restore(windowId))
    }
    
    /**
     * Snaps a window to a specific position
     */
    fun snapWindow(windowId: String, snapPosition: SnapPosition) {
        val window = _windows[windowId] ?: return
        
        val snapSize = calculateSnapSize(snapPosition)
        val snapOffset = calculateSnapPosition(snapPosition)
        
        _windows[windowId] = window
            .withState(WindowStateType.SNAPPED, snapPosition)
            .withSize(snapSize)
            .withPosition(snapOffset)
        
        notifyListeners(WindowEvent.Snap(windowId, snapPosition))
    }
    
    /**
     * Toggles always on top state for a window
     */
    fun toggleAlwaysOnTop(windowId: String) {
        val window = _windows[windowId] ?: return
        
        val newState = if (window.isAlwaysOnTop) {
            window.copy(isAlwaysOnTop = false, state = WindowStateType.NORMAL)
        } else {
            window.copy(isAlwaysOnTop = true, state = WindowStateType.FLOATING)
        }
        
        _windows[windowId] = newState
        notifyListeners(WindowEvent.ToggleAlwaysOnTop(windowId))
    }
    
    /**
     * Closes a window
     */
    fun closeWindow(windowId: String) {
        val window = _windows[windowId] ?: return
        
        if (!window.isClosable) return
        
        _windows.remove(windowId)
        
        // Focus next available window if this was the focused window
        if (_focusedWindowId.value == windowId) {
            focusNextAvailableWindow()
        }
        
        notifyListeners(WindowEvent.Close(windowId))
    }
    
    /**
     * Updates a window's title
     */
    fun updateWindowTitle(windowId: String, title: String) {
        val window = _windows[windowId] ?: return
        
        _windows[windowId] = window.withTitle(title)
        notifyListeners(WindowEvent.UpdateTitle(windowId, title))
    }
    
    /**
     * Processes window events
     */
    fun handleWindowEvent(event: WindowEvent) {
        when (event) {
            is WindowEvent.Focus -> focusWindow(event.windowId)
            is WindowEvent.Move -> moveWindow(event.windowId, event.position)
            is WindowEvent.Resize -> resizeWindow(event.windowId, event.size)
            is WindowEvent.Minimize -> minimizeWindow(event.windowId)
            is WindowEvent.Maximize -> maximizeWindow(event.windowId)
            is WindowEvent.Restore -> restoreWindow(event.windowId)
            is WindowEvent.Close -> closeWindow(event.windowId)
            is WindowEvent.UpdateTitle -> updateWindowTitle(event.windowId, event.title)
            is WindowEvent.BringToFront -> focusWindow(event.windowId)
            is WindowEvent.Snap -> snapWindow(event.windowId, event.snapPosition)
            is WindowEvent.ToggleAlwaysOnTop -> toggleAlwaysOnTop(event.windowId)
        }
    }
    
    /**
     * Adds an event listener
     */
    fun addEventListener(listener: (WindowEvent) -> Unit) {
        eventListeners.add(listener)
    }
    
    /**
     * Removes an event listener
     */
    fun removeEventListener(listener: (WindowEvent) -> Unit) {
        eventListeners.remove(listener)
    }
    
    /**
     * Updates screen size for layout calculations
     */
    fun updateScreenSize(size: DpSize) {
        _screenSize.value = size
    }
    
    /**
     * Checks if a window exists
     */
    fun hasWindow(windowId: String): Boolean {
        return _windows.containsKey(windowId)
    }
    
    /**
     * Gets the currently focused window
     */
    fun getFocusedWindow(): WindowState? {
        return _focusedWindowId.value?.let { _windows[it] }
    }
    
    /**
     * Clears all windows (for testing or reset)
     */
    fun clearAllWindows() {
        _windows.clear()
        _focusedWindowId.value = null
        nextZIndex = 0
    }
    
    // Private helper methods
    
    private fun focusNextAvailableWindow() {
        val availableWindows = _windows.values
            .filter { it.isVisible && it.state != WindowStateType.MINIMIZED }
            .sortedByDescending { it.lastFocusTime }
        
        if (availableWindows.isNotEmpty()) {
            focusWindow(availableWindows.first().id)
        } else {
            _focusedWindowId.value = null
        }
    }
    
    private fun calculateSnapSize(snapPosition: SnapPosition): DpSize {
        val screenWidth = screenSize.width
        val screenHeight = screenSize.height
        
        return when (snapPosition) {
            SnapPosition.LEFT_HALF, SnapPosition.RIGHT_HALF -> 
                DpSize(screenWidth / 2, screenHeight)
            SnapPosition.TOP_LEFT_QUARTER, SnapPosition.TOP_RIGHT_QUARTER,
            SnapPosition.BOTTOM_LEFT_QUARTER, SnapPosition.BOTTOM_RIGHT_QUARTER ->
                DpSize(screenWidth / 2, screenHeight / 2)
        }
    }
    
    private fun calculateSnapPosition(snapPosition: SnapPosition): Offset {
        val screenWidth = screenSize.width.value
        val screenHeight = screenSize.height.value
        
        return when (snapPosition) {
            SnapPosition.LEFT_HALF -> Offset(0f, 0f)
            SnapPosition.RIGHT_HALF -> Offset(screenWidth / 2, 0f)
            SnapPosition.TOP_LEFT_QUARTER -> Offset(0f, 0f)
            SnapPosition.TOP_RIGHT_QUARTER -> Offset(screenWidth / 2, 0f)
            SnapPosition.BOTTOM_LEFT_QUARTER -> Offset(0f, screenHeight / 2)
            SnapPosition.BOTTOM_RIGHT_QUARTER -> Offset(screenWidth / 2, screenHeight / 2)
        }
    }
    
    private fun notifyListeners(event: WindowEvent) {
        eventListeners.forEach { it(event) }
    }
}