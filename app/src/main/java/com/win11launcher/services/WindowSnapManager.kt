package com.win11launcher.services

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.win11launcher.models.SnapPosition
import com.win11launcher.models.WindowState
import com.win11launcher.models.WindowStateType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Manages window snapping functionality including drag-to-snap zones,
 * keyboard shortcuts, and automatic window arrangement.
 */
@Singleton
class WindowSnapManager @Inject constructor(
    private val windowManager: WindowManager
) {
    
    // Snap sensitivity in pixels
    private val snapSensitivity = 50f
    
    // Snap zone definitions
    private val snapZones = listOf(
        SnapZone(
            id = "left_half",
            position = SnapPosition.LEFT_HALF,
            bounds = Rect(0f, 0f, 0.5f, 1f)
        ),
        SnapZone(
            id = "right_half",
            position = SnapPosition.RIGHT_HALF,
            bounds = Rect(0.5f, 0f, 1f, 1f)
        ),
        SnapZone(
            id = "top_left_quarter",
            position = SnapPosition.TOP_LEFT_QUARTER,
            bounds = Rect(0f, 0f, 0.5f, 0.5f)
        ),
        SnapZone(
            id = "top_right_quarter",
            position = SnapPosition.TOP_RIGHT_QUARTER,
            bounds = Rect(0.5f, 0f, 1f, 0.5f)
        ),
        SnapZone(
            id = "bottom_left_quarter",
            position = SnapPosition.BOTTOM_LEFT_QUARTER,
            bounds = Rect(0f, 0.5f, 0.5f, 1f)
        ),
        SnapZone(
            id = "bottom_right_quarter",
            position = SnapPosition.BOTTOM_RIGHT_QUARTER,
            bounds = Rect(0.5f, 0.5f, 1f, 1f)
        )
    )
    
    /**
     * Handles window drag and checks for snap zones
     */
    fun handleWindowDrag(windowId: String, currentPosition: Offset, dragDelta: Offset): SnapResult {
        val window = windowManager.getWindow(windowId) ?: return SnapResult.NoSnap
        
        if (!window.canMove()) return SnapResult.NoSnap
        
        val screenSize = windowManager.screenSize
        val newPosition = Offset(
            currentPosition.x + dragDelta.x,
            currentPosition.y + dragDelta.y
        )
        
        // Check if window is near screen edges
        val edgeSnap = checkEdgeSnap(newPosition, screenSize)
        if (edgeSnap != null) {
            return SnapResult.EdgeSnap(edgeSnap)
        }
        
        // Check if window is in a snap zone
        val zoneSnap = checkSnapZone(newPosition, screenSize)
        if (zoneSnap != null) {
            return SnapResult.ZoneSnap(zoneSnap)
        }
        
        return SnapResult.NoSnap
    }
    
    /**
     * Snaps a window to a specific position
     */
    fun snapWindow(windowId: String, snapPosition: SnapPosition) {
        val window = windowManager.getWindow(windowId) ?: return
        
        if (!window.canMove() || !window.canResize()) return
        
        val screenSize = windowManager.screenSize
        val snapBounds = calculateSnapBounds(snapPosition, screenSize)
        
        windowManager.moveWindow(windowId, Offset(snapBounds.left, snapBounds.top))
        windowManager.resizeWindow(windowId, DpSize(
            width = (snapBounds.width).dp,
            height = (snapBounds.height).dp
        ))
        
        // Update window state to snapped
        val updatedWindow = window.withState(WindowStateType.SNAPPED, snapPosition)
        // The WindowManager should handle this state update
    }
    
    /**
     * Snaps window to left half of screen
     */
    fun snapWindowLeft(windowId: String) {
        snapWindow(windowId, SnapPosition.LEFT_HALF)
    }
    
    /**
     * Snaps window to right half of screen
     */
    fun snapWindowRight(windowId: String) {
        snapWindow(windowId, SnapPosition.RIGHT_HALF)
    }
    
    /**
     * Snaps window to top-left quarter
     */
    fun snapWindowTopLeft(windowId: String) {
        snapWindow(windowId, SnapPosition.TOP_LEFT_QUARTER)
    }
    
    /**
     * Snaps window to top-right quarter
     */
    fun snapWindowTopRight(windowId: String) {
        snapWindow(windowId, SnapPosition.TOP_RIGHT_QUARTER)
    }
    
    /**
     * Snaps window to bottom-left quarter
     */
    fun snapWindowBottomLeft(windowId: String) {
        snapWindow(windowId, SnapPosition.BOTTOM_LEFT_QUARTER)
    }
    
    /**
     * Snaps window to bottom-right quarter
     */
    fun snapWindowBottomRight(windowId: String) {
        snapWindow(windowId, SnapPosition.BOTTOM_RIGHT_QUARTER)
    }
    
    /**
     * Unsnaps a window, restoring it to normal state
     */
    fun unsnap(windowId: String) {
        val window = windowManager.getWindow(windowId) ?: return
        
        if (window.state == WindowStateType.SNAPPED) {
            windowManager.restoreWindow(windowId)
        }
    }
    
    /**
     * Arranges two windows side by side
     */
    fun arrangeSideBySide(leftWindowId: String, rightWindowId: String) {
        snapWindow(leftWindowId, SnapPosition.LEFT_HALF)
        snapWindow(rightWindowId, SnapPosition.RIGHT_HALF)
    }
    
    /**
     * Arranges four windows in quarters
     */
    fun arrangeQuadrant(
        topLeftWindowId: String,
        topRightWindowId: String,
        bottomLeftWindowId: String,
        bottomRightWindowId: String
    ) {
        snapWindow(topLeftWindowId, SnapPosition.TOP_LEFT_QUARTER)
        snapWindow(topRightWindowId, SnapPosition.TOP_RIGHT_QUARTER)
        snapWindow(bottomLeftWindowId, SnapPosition.BOTTOM_LEFT_QUARTER)
        snapWindow(bottomRightWindowId, SnapPosition.BOTTOM_RIGHT_QUARTER)
    }
    
    /**
     * Gets all available snap zones
     */
    fun getSnapZones(): List<SnapZone> {
        return snapZones
    }
    
    /**
     * Checks if a position is within a snap zone
     */
    fun isInSnapZone(position: Offset, screenSize: DpSize): SnapZone? {
        val normalizedX = position.x / screenSize.width.value
        val normalizedY = position.y / screenSize.height.value
        
        return snapZones.find { zone ->
            normalizedX >= zone.bounds.left && 
            normalizedX <= zone.bounds.right &&
            normalizedY >= zone.bounds.top && 
            normalizedY <= zone.bounds.bottom
        }
    }
    
    /**
     * Gets the snap zone for a specific position
     */
    fun getSnapZoneForPosition(snapPosition: SnapPosition): SnapZone? {
        return snapZones.find { it.position == snapPosition }
    }
    
    /**
     * Calculates the bounds for a snap position
     */
    private fun calculateSnapBounds(snapPosition: SnapPosition, screenSize: DpSize): Rect {
        val screenWidth = screenSize.width.value
        val screenHeight = screenSize.height.value
        
        return when (snapPosition) {
            SnapPosition.LEFT_HALF -> Rect(
                left = 0f,
                top = 0f,
                right = screenWidth / 2,
                bottom = screenHeight
            )
            SnapPosition.RIGHT_HALF -> Rect(
                left = screenWidth / 2,
                top = 0f,
                right = screenWidth,
                bottom = screenHeight
            )
            SnapPosition.TOP_LEFT_QUARTER -> Rect(
                left = 0f,
                top = 0f,
                right = screenWidth / 2,
                bottom = screenHeight / 2
            )
            SnapPosition.TOP_RIGHT_QUARTER -> Rect(
                left = screenWidth / 2,
                top = 0f,
                right = screenWidth,
                bottom = screenHeight / 2
            )
            SnapPosition.BOTTOM_LEFT_QUARTER -> Rect(
                left = 0f,
                top = screenHeight / 2,
                right = screenWidth / 2,
                bottom = screenHeight
            )
            SnapPosition.BOTTOM_RIGHT_QUARTER -> Rect(
                left = screenWidth / 2,
                top = screenHeight / 2,
                right = screenWidth,
                bottom = screenHeight
            )
        }
    }
    
    /**
     * Checks if window is near screen edges for snapping
     */
    private fun checkEdgeSnap(position: Offset, screenSize: DpSize): SnapPosition? {
        val screenWidth = screenSize.width.value
        val screenHeight = screenSize.height.value
        
        // Check left edge
        if (position.x <= snapSensitivity) {
            return if (position.y <= screenHeight / 2) {
                SnapPosition.TOP_LEFT_QUARTER
            } else {
                SnapPosition.BOTTOM_LEFT_QUARTER
            }
        }
        
        // Check right edge
        if (position.x >= screenWidth - snapSensitivity) {
            return if (position.y <= screenHeight / 2) {
                SnapPosition.TOP_RIGHT_QUARTER
            } else {
                SnapPosition.BOTTOM_RIGHT_QUARTER
            }
        }
        
        // Check top edge (maximize)
        if (position.y <= snapSensitivity) {
            return null // This would trigger maximize, not snap
        }
        
        return null
    }
    
    /**
     * Checks if window is in a snap zone
     */
    private fun checkSnapZone(position: Offset, screenSize: DpSize): SnapPosition? {
        val zone = isInSnapZone(position, screenSize)
        return zone?.position
    }
}

/**
 * Represents a snap zone on the screen
 */
data class SnapZone(
    val id: String,
    val position: SnapPosition,
    val bounds: Rect // Normalized bounds (0.0 to 1.0)
)

/**
 * Result of snap detection
 */
sealed class SnapResult {
    object NoSnap : SnapResult()
    data class EdgeSnap(val snapPosition: SnapPosition) : SnapResult()
    data class ZoneSnap(val snapPosition: SnapPosition) : SnapResult()
}

/**
 * Snap animation configuration
 */
data class SnapAnimationConfig(
    val duration: Long = 300L,
    val easing: androidx.compose.animation.core.Easing = androidx.compose.animation.core.EaseInOutCubic
)