package com.win11launcher.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex

@Composable
fun WorkingAreaContainer(
    modifier: Modifier = Modifier,
    workingAreaCalculator: WorkingAreaCalculator = rememberWorkingAreaCalculator(),
    content: @Composable BoxScope.() -> Unit
) {
    val bounds = remember(workingAreaCalculator) { workingAreaCalculator.calculateBounds() }
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(LayoutConstants.Z_INDEX_WORKING_AREA)
    ) {
        // Working area content
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { bounds.workingArea.left.toDp() },
                    y = with(density) { bounds.workingArea.top.toDp() }
                )
                .size(
                    width = with(density) { bounds.workingArea.width.toDp() },
                    height = with(density) { bounds.workingArea.height.toDp() }
                )
                .clip(RoundedCornerShape(LayoutConstants.WINDOW_CORNER_RADIUS))
        ) {
            content()
        }
    }
}

@Composable
fun TaskbarContainer(
    modifier: Modifier = Modifier,
    workingAreaCalculator: WorkingAreaCalculator = rememberWorkingAreaCalculator(),
    content: @Composable BoxScope.() -> Unit
) {
    val bounds = remember(workingAreaCalculator) { workingAreaCalculator.calculateBounds() }
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(LayoutConstants.Z_INDEX_TASKBAR)
    ) {
        // Taskbar content
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { bounds.taskbarArea.left.toDp() },
                    y = with(density) { bounds.taskbarArea.top.toDp() }
                )
                .size(
                    width = with(density) { bounds.taskbarArea.width.toDp() },
                    height = with(density) { bounds.taskbarArea.height.toDp() }
                )
                .clip(RoundedCornerShape(topStart = LayoutConstants.TASKBAR_CORNER_RADIUS, topEnd = LayoutConstants.TASKBAR_CORNER_RADIUS))
        ) {
            content()
        }
    }
}

@Composable
fun OverlayContainer(
    modifier: Modifier = Modifier,
    position: Offset = Offset.Zero,
    showBackdrop: Boolean = true,
    onDismiss: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(LayoutConstants.Z_INDEX_OVERLAYS)
    ) {
        // Backdrop
        if (showBackdrop) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = LayoutConstants.OVERLAY_BACKDROP_ALPHA))
            )
        }
        
        // Overlay content
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { position.x.toDp() },
                    y = with(density) { position.y.toDp() }
                )
        ) {
            content()
        }
    }
}

@Composable
fun WindowAreaContainer(
    modifier: Modifier = Modifier,
    workingAreaCalculator: WorkingAreaCalculator = rememberWorkingAreaCalculator(),
    content: @Composable BoxScope.() -> Unit
) {
    val bounds = remember(workingAreaCalculator) { workingAreaCalculator.calculateBounds() }
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(LayoutConstants.Z_INDEX_WINDOWS)
    ) {
        // Window area content (clipped to working area)
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { bounds.workingArea.left.toDp() },
                    y = with(density) { bounds.workingArea.top.toDp() }
                )
                .size(
                    width = with(density) { bounds.workingArea.width.toDp() },
                    height = with(density) { bounds.workingArea.height.toDp() }
                )
                .clip(RoundedCornerShape(LayoutConstants.WINDOW_CORNER_RADIUS))
        ) {
            content()
        }
    }
}

@Composable
fun LayoutDebugOverlay(
    workingAreaCalculator: WorkingAreaCalculator = rememberWorkingAreaCalculator(),
    showDebug: Boolean = false
) {
    if (!showDebug) return
    
    val bounds = remember(workingAreaCalculator) { workingAreaCalculator.calculateBounds() }
    val density = LocalDensity.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(LayoutConstants.Z_INDEX_MODAL)
    ) {
        // Working area outline
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { bounds.workingArea.left.toDp() },
                    y = with(density) { bounds.workingArea.top.toDp() }
                )
                .size(
                    width = with(density) { bounds.workingArea.width.toDp() },
                    height = with(density) { bounds.workingArea.height.toDp() }
                )
                .background(Color.Red.copy(alpha = 0.2f))
        )
        
        // Taskbar area outline
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { bounds.taskbarArea.left.toDp() },
                    y = with(density) { bounds.taskbarArea.top.toDp() }
                )
                .size(
                    width = with(density) { bounds.taskbarArea.width.toDp() },
                    height = with(density) { bounds.taskbarArea.height.toDp() }
                )
                .background(Color.Blue.copy(alpha = 0.2f))
        )
        
        // Snap zones
        workingAreaCalculator.getSnapZones().forEach { snapZone ->
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { snapZone.left.toDp() },
                        y = with(density) { snapZone.top.toDp() }
                    )
                    .size(
                        width = with(density) { snapZone.width.toDp() },
                        height = with(density) { snapZone.height.toDp() }
                    )
                    .background(Color.Green.copy(alpha = 0.3f))
            )
        }
    }
}