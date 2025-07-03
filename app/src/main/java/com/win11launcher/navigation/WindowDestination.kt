package com.win11launcher.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Sealed class representing different window destinations/types in the application.
 * Each destination defines how a window should be configured and what content it displays.
 */
@Stable
sealed class WindowDestination {
    abstract val windowId: String
    abstract val title: String
    abstract val icon: ImageVector
    abstract val defaultSize: DpSize
    abstract val isResizable: Boolean
    abstract val isMinimizable: Boolean
    abstract val isMaximizable: Boolean
    abstract val isClosable: Boolean
    abstract val alwaysOnTop: Boolean
    abstract val showInTaskbar: Boolean
    abstract val hasMenuBar: Boolean
    abstract val minimumSize: DpSize
    abstract val maximumSize: DpSize?
    
    /**
     * Home/Desktop window - the main launcher interface
     */
    object Home : WindowDestination() {
        override val windowId: String = "home"
        override val title: String = "Desktop"
        override val icon: ImageVector = Icons.Default.Home
        override val defaultSize: DpSize = DpSize(1200.dp, 800.dp)
        override val isResizable: Boolean = true
        override val isMinimizable: Boolean = false
        override val isMaximizable: Boolean = true
        override val isClosable: Boolean = false
        override val alwaysOnTop: Boolean = false
        override val showInTaskbar: Boolean = false
        override val hasMenuBar: Boolean = false
        override val minimumSize: DpSize = DpSize(800.dp, 600.dp)
        override val maximumSize: DpSize? = null
    }
    
    /**
     * Notes Hub main window
     */
    object NotesHub : WindowDestination() {
        override val windowId: String = "notes_hub_main"
        override val title: String = "Notes Hub"
        override val icon: ImageVector = Icons.Default.Note
        override val defaultSize: DpSize = DpSize(1000.dp, 700.dp)
        override val isResizable: Boolean = true
        override val isMinimizable: Boolean = true
        override val isMaximizable: Boolean = true
        override val isClosable: Boolean = true
        override val alwaysOnTop: Boolean = false
        override val showInTaskbar: Boolean = true
        override val hasMenuBar: Boolean = true
        override val minimumSize: DpSize = DpSize(600.dp, 400.dp)
        override val maximumSize: DpSize? = null
    }
    
    /**
     * Rule creation wizard window
     */
    object RuleWizard : WindowDestination() {
        override val windowId: String = "notes_rule_wizard"
        override val title: String = "Create Notification Rule"
        override val icon: ImageVector = Icons.Default.Add
        override val defaultSize: DpSize = DpSize(600.dp, 500.dp)
        override val isResizable: Boolean = false
        override val isMinimizable: Boolean = true
        override val isMaximizable: Boolean = false
        override val isClosable: Boolean = true
        override val alwaysOnTop: Boolean = false
        override val showInTaskbar: Boolean = true
        override val hasMenuBar: Boolean = false
        override val minimumSize: DpSize = DpSize(600.dp, 500.dp)
        override val maximumSize: DpSize = DpSize(600.dp, 500.dp)
    }
    
    /**
     * Individual note detail window
     */
    data class NoteDetail(
        val noteId: String,
        val noteTitle: String
    ) : WindowDestination() {
        override val windowId: String = "note_detail_$noteId"
        override val title: String = noteTitle
        override val icon: ImageVector = Icons.Default.Description
        override val defaultSize: DpSize = DpSize(800.dp, 600.dp)
        override val isResizable: Boolean = true
        override val isMinimizable: Boolean = true
        override val isMaximizable: Boolean = true
        override val isClosable: Boolean = true
        override val alwaysOnTop: Boolean = false
        override val showInTaskbar: Boolean = true
        override val hasMenuBar: Boolean = true
        override val minimumSize: DpSize = DpSize(400.dp, 300.dp)
        override val maximumSize: DpSize? = null
    }
    
    /**
     * File Manager window
     */
    object FileManager : WindowDestination() {
        override val windowId: String = "file_manager"
        override val title: String = "File Manager"
        override val icon: ImageVector = Icons.Default.Folder
        override val defaultSize: DpSize = DpSize(900.dp, 650.dp)
        override val isResizable: Boolean = true
        override val isMinimizable: Boolean = true
        override val isMaximizable: Boolean = true
        override val isClosable: Boolean = true
        override val alwaysOnTop: Boolean = false
        override val showInTaskbar: Boolean = true
        override val hasMenuBar: Boolean = true
        override val minimumSize: DpSize = DpSize(600.dp, 400.dp)
        override val maximumSize: DpSize? = null
    }
    
    /**
     * Settings window
     */
    object Settings : WindowDestination() {
        override val windowId: String = "settings"
        override val title: String = "Settings"
        override val icon: ImageVector = Icons.Default.Settings
        override val defaultSize: DpSize = DpSize(800.dp, 700.dp)
        override val isResizable: Boolean = true
        override val isMinimizable: Boolean = true
        override val isMaximizable: Boolean = true
        override val isClosable: Boolean = true
        override val alwaysOnTop: Boolean = false
        override val showInTaskbar: Boolean = true
        override val hasMenuBar: Boolean = false
        override val minimumSize: DpSize = DpSize(600.dp, 500.dp)
        override val maximumSize: DpSize? = null
    }
    
    /**
     * All Apps window
     */
    object AllApps : WindowDestination() {
        override val windowId: String = "all_apps"
        override val title: String = "All Apps"
        override val icon: ImageVector = Icons.Default.Apps
        override val defaultSize: DpSize = DpSize(800.dp, 600.dp)
        override val isResizable: Boolean = true
        override val isMinimizable: Boolean = true
        override val isMaximizable: Boolean = true
        override val isClosable: Boolean = true
        override val alwaysOnTop: Boolean = false
        override val showInTaskbar: Boolean = true
        override val hasMenuBar: Boolean = false
        override val minimumSize: DpSize = DpSize(600.dp, 400.dp)
        override val maximumSize: DpSize? = null
    }
    
    /**
     * Generic application window for external apps
     */
    data class ExternalApp(
        val appId: String,
        val appName: String,
        val appIcon: ImageVector,
        val customSize: DpSize? = null,
        val customResizable: Boolean = true,
        val customMinimizable: Boolean = true,
        val customMaximizable: Boolean = true,
        val customClosable: Boolean = true,
        val customAlwaysOnTop: Boolean = false,
        val customShowInTaskbar: Boolean = true,
        val customHasMenuBar: Boolean = false
    ) : WindowDestination() {
        override val windowId: String = "external_app_$appId"
        override val title: String = appName
        override val icon: ImageVector = appIcon
        override val defaultSize: DpSize = customSize ?: DpSize(800.dp, 600.dp)
        override val isResizable: Boolean = customResizable
        override val isMinimizable: Boolean = customMinimizable
        override val isMaximizable: Boolean = customMaximizable
        override val isClosable: Boolean = customClosable
        override val alwaysOnTop: Boolean = customAlwaysOnTop
        override val showInTaskbar: Boolean = customShowInTaskbar
        override val hasMenuBar: Boolean = customHasMenuBar
        override val minimumSize: DpSize = DpSize(400.dp, 300.dp)
        override val maximumSize: DpSize? = null
    }
    
    /**
     * Modal dialog window
     */
    data class Dialog(
        val dialogId: String,
        val dialogTitle: String,
        val dialogIcon: ImageVector,
        val parentWindowId: String,
        val customSize: DpSize = DpSize(400.dp, 300.dp)
    ) : WindowDestination() {
        override val windowId: String = "dialog_$dialogId"
        override val title: String = dialogTitle
        override val icon: ImageVector = dialogIcon
        override val defaultSize: DpSize = customSize
        override val isResizable: Boolean = false
        override val isMinimizable: Boolean = false
        override val isMaximizable: Boolean = false
        override val isClosable: Boolean = true
        override val alwaysOnTop: Boolean = true
        override val showInTaskbar: Boolean = false
        override val hasMenuBar: Boolean = false
        override val minimumSize: DpSize = customSize
        override val maximumSize: DpSize = customSize
    }
    
    /**
     * Utility function to create a window configuration from this destination
     */
    fun toWindowConfig(): WindowConfig {
        return WindowConfig(
            windowId = windowId,
            title = title,
            icon = icon,
            initialSize = defaultSize,
            resizable = isResizable,
            minimizable = isMinimizable,
            maximizable = isMaximizable,
            closable = isClosable,
            alwaysOnTop = alwaysOnTop,
            showInTaskbar = showInTaskbar,
            hasMenuBar = hasMenuBar,
            minimumSize = minimumSize,
            maximumSize = maximumSize
        )
    }
}

/**
 * Window configuration data class for creating windows
 */
data class WindowConfig(
    val windowId: String,
    val title: String,
    val icon: ImageVector,
    val initialSize: DpSize,
    val initialPosition: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset(100f, 100f),
    val resizable: Boolean = true,
    val minimizable: Boolean = true,
    val maximizable: Boolean = true,
    val closable: Boolean = true,
    val alwaysOnTop: Boolean = false,
    val showInTaskbar: Boolean = true,
    val hasMenuBar: Boolean = false,
    val minimumSize: DpSize = DpSize(200.dp, 150.dp),
    val maximumSize: DpSize? = null
)