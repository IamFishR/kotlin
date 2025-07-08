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
import com.win11launcher.ui.layout.WorkingAreaCalculator
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
    
    // Working area calculator for layout constraints
    private var _workingAreaCalculator: WorkingAreaCalculator? = null
    val workingAreaCalculator: WorkingAreaCalculator? get() = _workingAreaCalculator
    
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
        
        // Constrain window to working area if available
        val constrainedState = _workingAreaCalculator?.let { calculator ->
            val bounds = calculator.calculateBounds()
            val constrainedPosition = Offset(
                windowState.position.x.coerceIn(bounds.workingArea.left, bounds.workingArea.right - windowState.size.width.value),
                windowState.position.y.coerceIn(bounds.workingArea.top, bounds.workingArea.bottom - windowState.size.height.value)
            )
            windowState.withPosition(constrainedPosition)
        } ?: windowState
        
        _windows[config.windowId] = constrainedState
        
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
        
        // Constrain position to working area if available
        val constrainedPosition = _workingAreaCalculator?.let { calculator ->
            val bounds = calculator.calculateBounds()
            Offset(
                position.x.coerceIn(bounds.workingArea.left, bounds.workingArea.right - window.size.width.value),
                position.y.coerceIn(bounds.workingArea.top, bounds.workingArea.bottom - window.size.height.value)
            )
        } ?: position
        
        _windows[windowId] = window.withPosition(constrainedPosition)
        notifyListeners(WindowEvent.Move(windowId, constrainedPosition))
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
        
        // Maximize to working area bounds if available
        val maximizedWindow = _workingAreaCalculator?.let { calculator ->
            val bounds = calculator.calculateBounds()
            window.withState(WindowStateType.MAXIMIZED)
                .withPosition(Offset(bounds.workingArea.left, bounds.workingArea.top))
                .withSize(DpSize(bounds.availableWidth, bounds.availableHeight))
        } ?: window.withState(WindowStateType.MAXIMIZED)
        
        _windows[windowId] = maximizedWindow
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
     * Updates working area calculator for layout constraints
     */
    fun updateWorkingAreaCalculator(calculator: WorkingAreaCalculator) {
        _workingAreaCalculator = calculator
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
        // Use working area bounds if available, otherwise use screen size
        val bounds = _workingAreaCalculator?.calculateBounds()
        val availableWidth = bounds?.availableWidth ?: screenSize.width
        val availableHeight = bounds?.availableHeight ?: screenSize.height
        
        return when (snapPosition) {
            SnapPosition.LEFT_HALF, SnapPosition.RIGHT_HALF -> 
                DpSize(availableWidth / 2, availableHeight)
            SnapPosition.TOP_LEFT_QUARTER, SnapPosition.TOP_RIGHT_QUARTER,
            SnapPosition.BOTTOM_LEFT_QUARTER, SnapPosition.BOTTOM_RIGHT_QUARTER ->
                DpSize(availableWidth / 2, availableHeight / 2)
        }
    }
    
    private fun calculateSnapPosition(snapPosition: SnapPosition): Offset {
        // Use working area bounds if available, otherwise use screen size
        val bounds = _workingAreaCalculator?.calculateBounds()
        val workingArea = bounds?.workingArea
        val availableWidth = bounds?.availableWidth?.value ?: screenSize.width.value
        val availableHeight = bounds?.availableHeight?.value ?: screenSize.height.value
        val offsetX = workingArea?.left ?: 0f
        val offsetY = workingArea?.top ?: 0f
        
        return when (snapPosition) {
            SnapPosition.LEFT_HALF -> Offset(offsetX, offsetY)
            SnapPosition.RIGHT_HALF -> Offset(offsetX + availableWidth / 2, offsetY)
            SnapPosition.TOP_LEFT_QUARTER -> Offset(offsetX, offsetY)
            SnapPosition.TOP_RIGHT_QUARTER -> Offset(offsetX + availableWidth / 2, offsetY)
            SnapPosition.BOTTOM_LEFT_QUARTER -> Offset(offsetX, offsetY + availableHeight / 2)
            SnapPosition.BOTTOM_RIGHT_QUARTER -> Offset(offsetX + availableWidth / 2, offsetY + availableHeight / 2)
        }
    }
    
    private fun notifyListeners(event: WindowEvent) {
        eventListeners.forEach { it(event) }
    }
}