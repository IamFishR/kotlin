package com.win11launcher.services

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.win11launcher.models.WindowState
import com.win11launcher.models.WindowStateType
import com.win11launcher.navigation.WindowRouter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages taskbar representation of windows, including thumbnails, grouping, and interactions.
 * Provides the interface between the window system and the taskbar UI.
 */
@Singleton
class TaskbarManager @Inject constructor(
    private val windowManager: WindowManager
) {
    
    // Taskbar items state
    private val _taskbarItems = MutableStateFlow<List<TaskbarItem>>(emptyList())
    val taskbarItems: StateFlow<List<TaskbarItem>> = _taskbarItems.asStateFlow()
    
    // Window grouping state
    private val _windowGroups = MutableStateFlow<Map<String, WindowGroup>>(emptyMap())
    val windowGroups: StateFlow<Map<String, WindowGroup>> = _windowGroups.asStateFlow()
    
    // Taskbar configuration
    private val _taskbarConfig = MutableStateFlow(TaskbarConfig())
    val taskbarConfig: StateFlow<TaskbarConfig> = _taskbarConfig.asStateFlow()
    
    init {
        // Listen to window changes and update taskbar
        observeWindowChanges()
    }
    
    /**
     * Updates the taskbar items based on current window state
     */
    fun updateTaskbarItems() {
        val windows = windowManager.getTaskbarWindows()
        val groups = groupWindowsByType(windows)
        
        val taskbarItems = groups.map { (groupId, group) ->
            TaskbarItem(
                id = groupId,
                title = group.title,
                icon = group.icon,
                windowCount = group.windows.size,
                hasNotification = group.windows.any { it.hasNotification() },
                isActive = group.windows.any { it.hasFocus },
                isBadged = group.windows.any { it.isBadged() },
                windows = group.windows
            )
        }
        
        _taskbarItems.value = taskbarItems
        _windowGroups.value = groups
    }
    
    /**
     * Handles taskbar item click
     */
    fun onTaskbarItemClick(taskbarItem: TaskbarItem) {
        when {
            taskbarItem.windows.size == 1 -> {
                val window = taskbarItem.windows.first()
                handleSingleWindowClick(window)
            }
            taskbarItem.windows.size > 1 -> {
                handleMultipleWindowsClick(taskbarItem)
            }
        }
    }
    
    /**
     * Handles taskbar item right-click (context menu)
     */
    fun onTaskbarItemRightClick(taskbarItem: TaskbarItem): TaskbarContextMenu {
        return TaskbarContextMenu(
            items = buildContextMenuItems(taskbarItem)
        )
    }
    
    /**
     * Handles taskbar item hover (shows thumbnails)
     */
    fun onTaskbarItemHover(taskbarItem: TaskbarItem): WindowThumbnail? {
        return when {
            taskbarItem.windows.size == 1 -> {
                createWindowThumbnail(taskbarItem.windows.first())
            }
            taskbarItem.windows.size > 1 -> {
                createGroupThumbnail(taskbarItem)
            }
            else -> null
        }
    }
    
    /**
     * Pins an application to the taskbar
     */
    fun pinApplication(appId: String, appName: String, appIcon: ImageVector) {
        val currentConfig = _taskbarConfig.value
        val newPinnedApps = currentConfig.pinnedApps + PinnedApp(
            id = appId,
            name = appName,
            icon = appIcon,
            order = currentConfig.pinnedApps.size
        )
        
        _taskbarConfig.value = currentConfig.copy(pinnedApps = newPinnedApps)
    }
    
    /**
     * Unpins an application from the taskbar
     */
    fun unpinApplication(appId: String) {
        val currentConfig = _taskbarConfig.value
        val newPinnedApps = currentConfig.pinnedApps.filter { it.id != appId }
        
        _taskbarConfig.value = currentConfig.copy(pinnedApps = newPinnedApps)
    }
    
    /**
     * Reorders taskbar items
     */
    fun reorderTaskbarItems(fromIndex: Int, toIndex: Int) {
        val currentItems = _taskbarItems.value.toMutableList()
        if (fromIndex in currentItems.indices && toIndex in currentItems.indices) {
            val item = currentItems.removeAt(fromIndex)
            currentItems.add(toIndex, item)
            _taskbarItems.value = currentItems
        }
    }
    
    /**
     * Gets taskbar configuration
     */
    fun getTaskbarConfig(): TaskbarConfig {
        return _taskbarConfig.value
    }
    
    /**
     * Updates taskbar configuration
     */
    fun updateTaskbarConfig(config: TaskbarConfig) {
        _taskbarConfig.value = config
    }
    
    /**
     * Shows/hides taskbar
     */
    fun toggleTaskbarVisibility() {
        val currentConfig = _taskbarConfig.value
        _taskbarConfig.value = currentConfig.copy(
            isVisible = !currentConfig.isVisible
        )
    }
    
    /**
     * Auto-hides taskbar
     */
    fun setAutoHide(autoHide: Boolean) {
        val currentConfig = _taskbarConfig.value
        _taskbarConfig.value = currentConfig.copy(
            autoHide = autoHide
        )
    }
    
    /**
     * Gets window thumbnail for a specific window
     */
    fun getWindowThumbnail(windowId: String): WindowThumbnail? {
        val window = windowManager.getWindow(windowId)
        return window?.let { createWindowThumbnail(it) }
    }
    
    /**
     * Gets all window thumbnails for taskbar preview
     */
    fun getAllWindowThumbnails(): List<WindowThumbnail> {
        return windowManager.getVisibleWindows().map { createWindowThumbnail(it) }
    }
    
    // Private helper methods
    
    private fun observeWindowChanges() {
        // In a real implementation, this would observe window manager state changes
        // For now, we'll manually trigger updates
    }
    
    private fun groupWindowsByType(windows: List<WindowState>): Map<String, WindowGroup> {
        val groups = mutableMapOf<String, WindowGroup>()
        
        windows.forEach { window ->
            val groupId = extractGroupId(window)
            val existingGroup = groups[groupId]
            
            if (existingGroup != null) {
                groups[groupId] = existingGroup.copy(
                    windows = existingGroup.windows + window
                )
            } else {
                groups[groupId] = WindowGroup(
                    id = groupId,
                    title = extractGroupTitle(window),
                    icon = window.icon,
                    windows = listOf(window)
                )
            }
        }
        
        return groups
    }
    
    private fun extractGroupId(window: WindowState): String {
        return when {
            window.id.startsWith("external_app_") -> {
                val appId = window.id.removePrefix("external_app_")
                "app_$appId"
            }
            else -> window.id
        }
    }
    
    private fun extractGroupTitle(window: WindowState): String {
        return when {
            window.id.startsWith("external_app_") -> window.title
            else -> window.title
        }
    }
    
    private fun handleSingleWindowClick(window: WindowState) {
        when (window.state) {
            WindowStateType.MINIMIZED -> {
                windowManager.restoreWindow(window.id)
            }
            WindowStateType.NORMAL, WindowStateType.MAXIMIZED, WindowStateType.SNAPPED -> {
                if (window.hasFocus) {
                    windowManager.minimizeWindow(window.id)
                } else {
                    windowManager.focusWindow(window.id)
                }
            }
            WindowStateType.FLOATING -> {
                windowManager.focusWindow(window.id)
            }
        }
    }
    
    private fun handleMultipleWindowsClick(taskbarItem: TaskbarItem) {
        val focusedWindow = taskbarItem.windows.find { it.hasFocus }
        
        if (focusedWindow != null) {
            // If one window is focused, focus the next one
            val currentIndex = taskbarItem.windows.indexOf(focusedWindow)
            val nextIndex = (currentIndex + 1) % taskbarItem.windows.size
            val nextWindow = taskbarItem.windows[nextIndex]
            
            if (nextWindow.state == WindowStateType.MINIMIZED) {
                windowManager.restoreWindow(nextWindow.id)
            } else {
                windowManager.focusWindow(nextWindow.id)
            }
        } else {
            // No window is focused, focus the first non-minimized one
            val visibleWindow = taskbarItem.windows.find { it.state != WindowStateType.MINIMIZED }
            if (visibleWindow != null) {
                windowManager.focusWindow(visibleWindow.id)
            } else {
                // All windows are minimized, restore the first one
                val firstWindow = taskbarItem.windows.first()
                windowManager.restoreWindow(firstWindow.id)
            }
        }
    }
    
    private fun buildContextMenuItems(taskbarItem: TaskbarItem): List<TaskbarContextMenuItem> {
        val items = mutableListOf<TaskbarContextMenuItem>()
        
        // Individual window actions
        if (taskbarItem.windows.size > 1) {
            items.add(TaskbarContextMenuItem.Separator)
            taskbarItem.windows.forEach { window ->
                items.add(
                    TaskbarContextMenuItem.Action(
                        text = window.title,
                        icon = window.icon,
                        action = { windowManager.focusWindow(window.id) }
                    )
                )
            }
            items.add(TaskbarContextMenuItem.Separator)
        }
        
        // Common actions
        items.add(
            TaskbarContextMenuItem.Action(
                text = "Close ${if (taskbarItem.windows.size > 1) "all windows" else "window"}",
                action = { 
                    taskbarItem.windows.forEach { window ->
                        if (window.isClosable) {
                            windowManager.closeWindow(window.id)
                        }
                    }
                }
            )
        )
        
        if (taskbarItem.windows.any { it.state == WindowStateType.MINIMIZED }) {
            items.add(
                TaskbarContextMenuItem.Action(
                    text = "Restore",
                    action = {
                        taskbarItem.windows
                            .filter { it.state == WindowStateType.MINIMIZED }
                            .forEach { window ->
                                windowManager.restoreWindow(window.id)
                            }
                    }
                )
            )
        }
        
        return items
    }
    
    private fun createWindowThumbnail(window: WindowState): WindowThumbnail {
        return WindowThumbnail(
            windowId = window.id,
            title = window.title,
            icon = window.icon,
            isMinimized = window.state == WindowStateType.MINIMIZED,
            hasFocus = window.hasFocus,
            canClose = window.isClosable
        )
    }
    
    private fun createGroupThumbnail(taskbarItem: TaskbarItem): WindowThumbnail {
        return WindowThumbnail(
            windowId = taskbarItem.id,
            title = "${taskbarItem.title} (${taskbarItem.windows.size} windows)",
            icon = taskbarItem.icon,
            isMinimized = taskbarItem.windows.all { it.state == WindowStateType.MINIMIZED },
            hasFocus = taskbarItem.windows.any { it.hasFocus },
            canClose = taskbarItem.windows.any { it.isClosable }
        )
    }
}

// Data classes for taskbar functionality

/**
 * Represents a taskbar item
 */
data class TaskbarItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val windowCount: Int,
    val hasNotification: Boolean = false,
    val isActive: Boolean = false,
    val isBadged: Boolean = false,
    val windows: List<WindowState>
)

/**
 * Represents a group of windows in the taskbar
 */
data class WindowGroup(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val windows: List<WindowState>
)

/**
 * Represents a window thumbnail for taskbar preview
 */
data class WindowThumbnail(
    val windowId: String,
    val title: String,
    val icon: ImageVector,
    val isMinimized: Boolean,
    val hasFocus: Boolean,
    val canClose: Boolean
)

/**
 * Taskbar configuration
 */
data class TaskbarConfig(
    val isVisible: Boolean = true,
    val autoHide: Boolean = false,
    val position: TaskbarPosition = TaskbarPosition.BOTTOM,
    val size: TaskbarSize = TaskbarSize.MEDIUM,
    val pinnedApps: List<PinnedApp> = emptyList(),
    val showWindowThumbnails: Boolean = true,
    val groupSimilarWindows: Boolean = true,
    val showNotificationBadges: Boolean = true
)

/**
 * Represents a pinned application in the taskbar
 */
data class PinnedApp(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val order: Int
)

/**
 * Taskbar position enum
 */
enum class TaskbarPosition {
    TOP, BOTTOM, LEFT, RIGHT
}

/**
 * Taskbar size enum
 */
enum class TaskbarSize {
    SMALL, MEDIUM, LARGE
}

/**
 * Taskbar context menu
 */
data class TaskbarContextMenu(
    val items: List<TaskbarContextMenuItem>
)

/**
 * Taskbar context menu item
 */
sealed class TaskbarContextMenuItem {
    data class Action(
        val text: String,
        val icon: ImageVector? = null,
        val action: () -> Unit
    ) : TaskbarContextMenuItem()
    
    object Separator : TaskbarContextMenuItem()
}

// Extension functions for window state
private fun WindowState.hasNotification(): Boolean {
    // Check if window has pending notifications
    return false // Implement based on your notification system
}

private fun WindowState.isBadged(): Boolean {
    // Check if window has a badge (like unread count)
    return false // Implement based on your badge system
}