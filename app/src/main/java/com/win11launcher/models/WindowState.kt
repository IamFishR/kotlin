package com.win11launcher.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Window
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.win11launcher.ui.layout.LayoutConstants

/**
 * Represents the different states a window can be in
 */
enum class WindowStateType {
    NORMAL,     // Default windowed state with custom size/position
    MAXIMIZED,  // Full screen minus taskbar
    MINIMIZED,  // Hidden but accessible via taskbar
    SNAPPED,    // Windows 11 style snap zones (left/right half, quarters)
    FLOATING    // Always on top mode
}

/**
 * Represents the snap position for snapped windows
 */
enum class SnapPosition {
    LEFT_HALF,
    RIGHT_HALF,
    TOP_LEFT_QUARTER,
    TOP_RIGHT_QUARTER,
    BOTTOM_LEFT_QUARTER,
    BOTTOM_RIGHT_QUARTER
}

/**
 * Comprehensive window state data class containing all window properties
 * as specified in the requirements
 */
@Stable
data class WindowState(
    val id: String,
    val title: String,
    val icon: ImageVector = Icons.Default.Window,
    val position: Offset = Offset.Zero,
    val size: DpSize = DpSize(LayoutConstants.DEFAULT_WINDOW_WIDTH, LayoutConstants.DEFAULT_WINDOW_HEIGHT),
    val state: WindowStateType = WindowStateType.NORMAL,
    val isResizable: Boolean = true,
    val isMinimizable: Boolean = true,
    val isMaximizable: Boolean = true,
    val zIndex: Int = 0,
    val lastFocusTime: Long = System.currentTimeMillis(),
    val snapPosition: SnapPosition? = null,
    val isAlwaysOnTop: Boolean = false,
    val isModal: Boolean = false,
    val parentWindowId: String? = null,
    val opacity: Float = 1.0f,
    val hasFocus: Boolean = false,
    val isVisible: Boolean = true,
    val minimumSize: DpSize = DpSize(LayoutConstants.MIN_WINDOW_WIDTH, LayoutConstants.MIN_WINDOW_HEIGHT),
    val maximumSize: DpSize? = null,
    val isClosable: Boolean = true,
    val hasMenuBar: Boolean = false,
    val showInTaskbar: Boolean = true,
    val theme: String? = null,
    val lastModified: Long = System.currentTimeMillis()
) {
    /**
     * Creates a copy of the window state with updated position
     */
    fun withPosition(newPosition: Offset): WindowState {
        return copy(
            position = newPosition,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Creates a copy of the window state with updated size
     */
    fun withSize(newSize: DpSize): WindowState {
        val constrainedSize = when {
            maximumSize != null -> DpSize(
                width = newSize.width.coerceAtMost(maximumSize.width),
                height = newSize.height.coerceAtMost(maximumSize.height)
            )
            else -> newSize
        }.let { size ->
            DpSize(
                width = size.width.coerceAtLeast(minimumSize.width),
                height = size.height.coerceAtLeast(minimumSize.height)
            )
        }
        
        return copy(
            size = constrainedSize,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Creates a copy of the window state with updated window state type
     */
    fun withState(newState: WindowStateType, snapPosition: SnapPosition? = null): WindowState {
        return copy(
            state = newState,
            snapPosition = if (newState == WindowStateType.SNAPPED) snapPosition else null,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Creates a copy of the window state with updated focus
     */
    fun withFocus(focused: Boolean, focusTime: Long = System.currentTimeMillis()): WindowState {
        return copy(
            hasFocus = focused,
            lastFocusTime = if (focused) focusTime else lastFocusTime,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Creates a copy of the window state with updated visibility
     */
    fun withVisibility(visible: Boolean): WindowState {
        return copy(
            isVisible = visible,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Creates a copy of the window state with updated z-index
     */
    fun withZIndex(newZIndex: Int): WindowState {
        return copy(
            zIndex = newZIndex,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Creates a copy of the window state with updated title
     */
    fun withTitle(newTitle: String): WindowState {
        return copy(
            title = newTitle,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Checks if the window can be resized
     */
    fun canResize(): Boolean = isResizable && state != WindowStateType.MAXIMIZED

    /**
     * Checks if the window can be moved
     */
    fun canMove(): Boolean = state != WindowStateType.MAXIMIZED && state != WindowStateType.SNAPPED

    /**
     * Checks if the window is in a normal state (not minimized, maximized, or snapped)
     */
    fun isNormal(): Boolean = state == WindowStateType.NORMAL

    /**
     * Checks if the window is currently minimized
     */
    fun isMinimized(): Boolean = state == WindowStateType.MINIMIZED

    /**
     * Checks if the window is currently maximized
     */
    fun isMaximized(): Boolean = state == WindowStateType.MAXIMIZED

    /**
     * Checks if the window is currently snapped
     */
    fun isSnapped(): Boolean = state == WindowStateType.SNAPPED

    /**
     * Checks if the window is floating (always on top)
     */
    fun isFloating(): Boolean = state == WindowStateType.FLOATING || isAlwaysOnTop

    /**
     * Gets the effective opacity based on focus state
     */
    fun getEffectiveOpacity(): Float {
        return if (hasFocus) opacity else (opacity * 0.85f).coerceAtLeast(0.5f)
    }

    companion object {
        /**
         * Creates a default window state with the given id and title
         */
        fun create(
            id: String,
            title: String,
            icon: ImageVector = Icons.Default.Window,
            initialSize: DpSize = DpSize(800.dp, 600.dp),
            initialPosition: Offset = Offset.Zero
        ): WindowState {
            return WindowState(
                id = id,
                title = title,
                icon = icon,
                size = initialSize,
                position = initialPosition,
                lastFocusTime = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis()
            )
        }
        
        /**
         * Creates a window state from a WindowConfig
         */
        fun fromConfig(config: com.win11launcher.navigation.WindowConfig): WindowState {
            return WindowState(
                id = config.windowId,
                title = config.title,
                icon = config.icon,
                position = config.initialPosition,
                size = config.initialSize,
                state = WindowStateType.NORMAL,
                isResizable = config.resizable,
                isMinimizable = config.minimizable,
                isMaximizable = config.maximizable,
                isClosable = config.closable,
                isAlwaysOnTop = config.alwaysOnTop,
                hasMenuBar = config.hasMenuBar,
                minimumSize = config.minimumSize,
                maximumSize = config.maximumSize,
                lastFocusTime = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis()
            )
        }
    }
}

/**
 * Events that can occur on a window
 */
sealed class WindowEvent {
    data class Focus(val windowId: String) : WindowEvent()
    data class Move(val windowId: String, val position: Offset) : WindowEvent()
    data class Resize(val windowId: String, val size: DpSize) : WindowEvent()
    data class Minimize(val windowId: String) : WindowEvent()
    data class Maximize(val windowId: String) : WindowEvent()
    data class Restore(val windowId: String) : WindowEvent()
    data class Close(val windowId: String) : WindowEvent()
    data class UpdateTitle(val windowId: String, val title: String) : WindowEvent()
    data class BringToFront(val windowId: String) : WindowEvent()
    data class Snap(val windowId: String, val snapPosition: SnapPosition) : WindowEvent()
    data class ToggleAlwaysOnTop(val windowId: String) : WindowEvent()
}