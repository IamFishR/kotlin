package com.win11launcher.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

data class WorkingAreaBounds(
    val screenSize: DpSize,
    val workingArea: Rect,
    val taskbarArea: Rect,
    val taskbarHeight: Dp,
    val availableWidth: Dp,
    val availableHeight: Dp
)

class WorkingAreaCalculator(
    private val screenSize: DpSize
) {
    
    fun calculateBounds(): WorkingAreaBounds {
        val screenWidth = screenSize.width
        val screenHeight = screenSize.height
        
        // Calculate taskbar area
        val taskbarWidth = screenWidth - (LayoutConstants.TASKBAR_MARGIN_HORIZONTAL * 2)
        val taskbarHeight = LayoutConstants.TASKBAR_HEIGHT
        val taskbarX = LayoutConstants.TASKBAR_MARGIN_HORIZONTAL
        val taskbarY = screenHeight - taskbarHeight - LayoutConstants.TASKBAR_MARGIN_BOTTOM
        
        val taskbarArea = Rect(
            offset = Offset(taskbarX.value, taskbarY.value),
            size = Size(taskbarWidth.value, taskbarHeight.value)
        )
        
        // Calculate working area (screen minus taskbar and margins)
        val workingAreaTop = LayoutConstants.WORKING_AREA_PADDING_TOP
        val workingAreaLeft = LayoutConstants.WORKING_AREA_PADDING_HORIZONTAL
        val workingAreaRight = screenWidth - LayoutConstants.WORKING_AREA_PADDING_HORIZONTAL
        val workingAreaBottom = taskbarY - LayoutConstants.WORKING_AREA_PADDING_BOTTOM
        
        val workingArea = Rect(
            offset = Offset(workingAreaLeft.value, workingAreaTop.value),
            size = Size(
                (workingAreaRight - workingAreaLeft).value,
                (workingAreaBottom - workingAreaTop).value
            )
        )
        
        val availableWidth = workingAreaRight - workingAreaLeft
        val availableHeight = workingAreaBottom - workingAreaTop
        
        return WorkingAreaBounds(
            screenSize = screenSize,
            workingArea = workingArea,
            taskbarArea = taskbarArea,
            taskbarHeight = taskbarHeight,
            availableWidth = availableWidth,
            availableHeight = availableHeight
        )
    }
    
    fun getTaskbarBounds(): Rect = calculateBounds().taskbarArea
    
    fun getWorkingAreaBounds(): Rect = calculateBounds().workingArea
    
    fun getMaxWindowSize(): DpSize {
        val bounds = calculateBounds()
        return DpSize(bounds.availableWidth, bounds.availableHeight)
    }
    
    fun getStartMenuPosition(): Offset {
        val bounds = calculateBounds()
        val taskbarCenter = bounds.taskbarArea.center
        val startMenuWidth = LayoutConstants.START_MENU_WIDTH
        val startMenuMaxHeight = LayoutConstants.START_MENU_MAX_HEIGHT
        
        // Position above taskbar, centered horizontally with taskbar
        val x = (taskbarCenter.x - startMenuWidth.value / 2).coerceIn(
            LayoutConstants.WORKING_AREA_PADDING_HORIZONTAL.value,
            bounds.screenSize.width.value - startMenuWidth.value - LayoutConstants.WORKING_AREA_PADDING_HORIZONTAL.value
        )
        
        val y = bounds.taskbarArea.top - startMenuMaxHeight.value - LayoutConstants.START_MENU_MARGIN_BOTTOM.value
        
        return Offset(x, y.coerceAtLeast(LayoutConstants.WORKING_AREA_PADDING_TOP.value))
    }
    
    fun getTaskbarPosition(): Offset {
        val bounds = calculateBounds()
        return Offset(bounds.taskbarArea.left, bounds.taskbarArea.top)
    }
    
    fun isPositionInTaskbar(position: Offset): Boolean {
        return calculateBounds().taskbarArea.contains(position)
    }
    
    fun isPositionInWorkingArea(position: Offset): Boolean {
        return calculateBounds().workingArea.contains(position)
    }
    
    fun constrainToWorkingArea(rect: Rect): Rect {
        val workingArea = getWorkingAreaBounds()
        
        val constrainedLeft = rect.left.coerceIn(workingArea.left, workingArea.right - rect.width)
        val constrainedTop = rect.top.coerceIn(workingArea.top, workingArea.bottom - rect.height)
        
        return Rect(
            offset = Offset(constrainedLeft, constrainedTop),
            size = rect.size
        )
    }
    
    fun getSnapZones(): List<Rect> {
        val bounds = calculateBounds()
        val workingArea = bounds.workingArea
        val zoneSize = LayoutConstants.SNAP_ZONE_SIZE.value
        
        return listOf(
            // Left snap zone
            Rect(
                offset = Offset(workingArea.left, workingArea.top),
                size = Size(zoneSize, workingArea.height)
            ),
            // Right snap zone
            Rect(
                offset = Offset(workingArea.right - zoneSize, workingArea.top),
                size = Size(zoneSize, workingArea.height)
            ),
            // Top snap zone (maximize)
            Rect(
                offset = Offset(workingArea.left, workingArea.top),
                size = Size(workingArea.width, zoneSize)
            )
        )
    }
}

@Composable
fun rememberWorkingAreaCalculator(): WorkingAreaCalculator {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        val screenSize = with(density) {
            DpSize(
                configuration.screenWidthDp.dp,
                configuration.screenHeightDp.dp
            )
        }
        WorkingAreaCalculator(screenSize)
    }
}