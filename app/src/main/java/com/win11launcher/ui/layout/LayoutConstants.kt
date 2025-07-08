package com.win11launcher.ui.layout

import androidx.compose.ui.unit.dp

object LayoutConstants {
    // Taskbar dimensions
    val TASKBAR_HEIGHT = 56.dp
    val TASKBAR_MARGIN_HORIZONTAL = 16.dp
    val TASKBAR_MARGIN_BOTTOM = 8.dp
    val TASKBAR_CORNER_RADIUS = 8.dp
    
    // Working area padding
    val WORKING_AREA_PADDING_TOP = 8.dp
    val WORKING_AREA_PADDING_HORIZONTAL = 8.dp
    val WORKING_AREA_PADDING_BOTTOM = 8.dp
    
    // StartMenu dimensions
    val START_MENU_MAX_HEIGHT = 700.dp
    val START_MENU_WIDTH = 600.dp
    val START_MENU_MARGIN_BOTTOM = 16.dp
    
    // Window dimensions
    val WINDOW_TITLE_BAR_HEIGHT = 32.dp
    val WINDOW_BORDER_WIDTH = 1.dp
    val WINDOW_RESIZE_HANDLE_SIZE = 8.dp
    val WINDOW_CORNER_RADIUS = 8.dp
    
    // Overlay dimensions
    val OVERLAY_BACKDROP_ALPHA = 0.3f
    val OVERLAY_ANIMATION_DURATION = 300
    
    // System tray dimensions
    val SYSTEM_TRAY_ITEM_SIZE = 24.dp
    val SYSTEM_TRAY_SPACING = 8.dp
    
    // General spacing
    val SPACING_SMALL = 4.dp
    val SPACING_MEDIUM = 8.dp
    val SPACING_LARGE = 16.dp
    val SPACING_EXTRA_LARGE = 24.dp
    
    // Icon and button sizes
    val ICON_SMALL = 14.dp
    val ICON_MEDIUM = 16.dp
    val ICON_LARGE = 20.dp
    val ICON_EXTRA_LARGE = 24.dp
    val ICON_HUGE = 32.dp
    val ICON_MASSIVE = 48.dp
    
    // Component sizes
    val BUTTON_HEIGHT_SMALL = 24.dp
    val BUTTON_HEIGHT_MEDIUM = 32.dp
    val BUTTON_HEIGHT_LARGE = 36.dp
    val BUTTON_HEIGHT_EXTRA_LARGE = 40.dp
    val BUTTON_HEIGHT_HUGE = 56.dp
    
    // Layout sizes
    val LAYOUT_WIDTH_SMALL = 80.dp
    val LAYOUT_WIDTH_MEDIUM = 180.dp
    val LAYOUT_HEIGHT_SMALL = 28.dp
    val LAYOUT_HEIGHT_MEDIUM = 600.dp
    val LAYOUT_HEIGHT_LARGE = 700.dp
    
    // Maximum screen dimensions
    val MAX_SCREEN_WIDTH = 1920.dp
    val MAX_SCREEN_HEIGHT = 1080.dp
    
    // Window defaults
    val DEFAULT_WINDOW_WIDTH = 800.dp
    val DEFAULT_WINDOW_HEIGHT = 600.dp
    val MIN_WINDOW_WIDTH = 200.dp
    val MIN_WINDOW_HEIGHT = 150.dp
    
    // App icon sizes
    val APP_ICON_SIZE = 32.dp
    val APP_ICON_CONTENT_SIZE = 24.dp
    val APP_ICON_CORNER_RADIUS = 6.dp
    val APP_ICON_FALLBACK_SIZE = 20.dp
    
    // Command prompt dimensions
    val COMMAND_PROMPT_BORDER_WIDTH = 1.dp
    val COMMAND_PROMPT_ELEVATION = 16.dp
    val COMMAND_PROMPT_PROGRESS_SIZE = 16.dp
    val COMMAND_PROMPT_PROGRESS_STROKE = 2.dp
    val COMMAND_PROMPT_RESIZE_HANDLE_SIZE = 12.dp
    
    // Permissions screen dimensions
    val PERMISSIONS_SCREEN_TOP_SPACING = 40.dp
    val PERMISSIONS_SCREEN_CARD_RADIUS = 12.dp
    val PERMISSIONS_SCREEN_BADGE_RADIUS = 4.dp
    
    // Z-index values
    const val Z_INDEX_BACKGROUND = 0f
    const val Z_INDEX_WORKING_AREA = 1f
    const val Z_INDEX_WINDOWS = 2f
    const val Z_INDEX_TASKBAR = 3f
    const val Z_INDEX_OVERLAYS = 4f
    const val Z_INDEX_MODAL = 5f
    
    // Screen edge detection
    val SCREEN_EDGE_THRESHOLD = 50.dp
    val SNAP_ZONE_SIZE = 20.dp
}