package com.win11launcher.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropDin
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.win11launcher.models.WindowEvent
import com.win11launcher.models.WindowState as AppWindowState
import com.win11launcher.models.WindowStateType

/**
 * Container for a window with title bar, controls, and content area.
 * Handles window chrome, drag operations, and window control actions.
 */
@Composable
fun WindowContainer(
    windowState: AppWindowState,
    onEvent: (WindowEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    
    // Window visibility and state
    val isVisible = windowState.isVisible && windowState.state != WindowStateType.MINIMIZED
    val isMaximized = windowState.state == WindowStateType.MAXIMIZED
    val hasFocus = windowState.hasFocus
    val opacity = windowState.getEffectiveOpacity()
    
    if (!isVisible) return
    
    Box(
        modifier = modifier
            .size(
                width = if (isMaximized) 1920.dp else windowState.size.width,
                height = if (isMaximized) 1080.dp else windowState.size.height
            )
            .offset(
                x = if (isMaximized) 0.dp else with(density) { windowState.position.x.toDp() },
                y = if (isMaximized) 0.dp else with(density) { windowState.position.y.toDp() }
            )
            .zIndex(windowState.zIndex.toFloat())
            .shadow(
                elevation = if (hasFocus) 12.dp else 6.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = opacity)
            )
            .border(
                width = if (hasFocus) 2.dp else 1.dp,
                color = if (hasFocus) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) 
                else 
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onEvent(WindowEvent.Focus(windowState.id)) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title bar
            WindowTitleBar(
                windowState = windowState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Menu bar (if applicable)
            if (windowState.hasMenuBar) {
                WindowMenuBar(
                    windowState = windowState,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    }
}

/**
 * Window title bar with drag handle, title, and window controls
 */
@Composable
private fun WindowTitleBar(
    windowState: AppWindowState,
    onEvent: (WindowEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .height(32.dp)
            .background(
                color = if (windowState.hasFocus) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else 
                    MaterialTheme.colorScheme.surface
            )
            .pointerInput(windowState.id) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false }
                ) { _, dragAmount ->
                    if (windowState.canMove()) {
                        val newPosition = Offset(
                            windowState.position.x + dragAmount.x,
                            windowState.position.y + dragAmount.y
                        )
                        onEvent(WindowEvent.Move(windowState.id, newPosition))
                    }
                }
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Window icon
        Icon(
            imageVector = windowState.icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Window title
        Text(
            text = windowState.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = if (windowState.hasFocus) FontWeight.Medium else FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        // Window controls
        WindowControls(
            windowState = windowState,
            onEvent = onEvent
        )
    }
}

/**
 * Window menu bar for applications that support it
 */
@Composable
private fun WindowMenuBar(
    windowState: AppWindowState,
    onEvent: (WindowEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(28.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Common menu items
        MenuBarItem("File") { /* Handle file menu */ }
        MenuBarItem("Edit") { /* Handle edit menu */ }
        MenuBarItem("View") { /* Handle view menu */ }
        MenuBarItem("Help") { /* Handle help menu */ }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Additional menu actions
        IconButton(
            onClick = { /* Handle menu */ },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Menu bar item button
 */
@Composable
private fun MenuBarItem(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(24.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp
        )
    }
}

/**
 * Window control buttons (minimize, maximize, close)
 */
@Composable
private fun WindowControls(
    windowState: AppWindowState,
    onEvent: (WindowEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Minimize button
        if (windowState.isMinimizable) {
            WindowControlButton(
                icon = Icons.Default.Minimize,
                contentDescription = "Minimize",
                onClick = { onEvent(WindowEvent.Minimize(windowState.id)) }
            )
        }
        
        // Maximize/Restore button
        if (windowState.isMaximizable) {
            WindowControlButton(
                icon = Icons.Default.CropDin,
                contentDescription = if (windowState.isMaximized()) "Restore" else "Maximize",
                onClick = { 
                    if (windowState.isMaximized()) {
                        onEvent(WindowEvent.Restore(windowState.id))
                    } else {
                        onEvent(WindowEvent.Maximize(windowState.id))
                    }
                }
            )
        }
        
        // Close button
        if (windowState.isClosable) {
            WindowControlButton(
                icon = Icons.Default.Close,
                contentDescription = "Close",
                onClick = { onEvent(WindowEvent.Close(windowState.id)) },
                isCloseButton = true
            )
        }
    }
}

/**
 * Individual window control button
 */
@Composable
private fun WindowControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isCloseButton: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(24.dp)
            .background(
                color = if (isCloseButton) 
                    Color.Transparent 
                else 
                    Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(14.dp),
            tint = if (isCloseButton) 
                Color.Red.copy(alpha = 0.8f) 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Window resize handle for the bottom-right corner
 */
@Composable
fun WindowResizeHandle(
    windowState: AppWindowState,
    onEvent: (WindowEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!windowState.canResize()) return
    
    Box(
        modifier = modifier
            .size(12.dp)
            .background(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(2.dp)
            )
            .pointerInput(windowState.id) {
                detectDragGestures { _, dragAmount ->
                    val newSize = DpSize(
                        width = (windowState.size.width.value + with(density) { dragAmount.x.toDp().value }).dp,
                        height = (windowState.size.height.value + with(density) { dragAmount.y.toDp().value }).dp
                    )
                    onEvent(WindowEvent.Resize(windowState.id, newSize))
                }
            }
    )
}