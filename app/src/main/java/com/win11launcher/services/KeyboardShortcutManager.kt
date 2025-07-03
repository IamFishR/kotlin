package com.win11launcher.services

import androidx.compose.ui.input.key.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages keyboard shortcuts for window operations and system functions.
 * Provides Windows-like keyboard shortcuts for window management.
 */
@Singleton
class KeyboardShortcutManager @Inject constructor(
    private val windowManager: WindowManager,
    private val windowSnapManager: WindowSnapManager,
    private val taskbarManager: TaskbarManager
) {
    
    // Keyboard shortcut state
    private val _registeredShortcuts = MutableStateFlow<Map<KeyboardShortcut, ShortcutAction>>(emptyMap())
    val registeredShortcuts: StateFlow<Map<KeyboardShortcut, ShortcutAction>> = _registeredShortcuts.asStateFlow()
    
    // Modifier key states
    private val _modifierStates = MutableStateFlow(ModifierStates())
    val modifierStates: StateFlow<ModifierStates> = _modifierStates.asStateFlow()
    
    init {
        registerDefaultShortcuts()
    }
    
    /**
     * Handles key events and executes shortcuts
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        updateModifierStates(event)
        
        if (event.type == KeyEventType.KeyDown) {
            val shortcut = KeyboardShortcut.fromKeyEvent(event)
            val action = _registeredShortcuts.value[shortcut]
            
            if (action != null) {
                executeShortcut(action)
                return true
            }
        }
        
        return false
    }
    
    /**
     * Registers a keyboard shortcut
     */
    fun registerShortcut(shortcut: KeyboardShortcut, action: ShortcutAction) {
        val currentShortcuts = _registeredShortcuts.value.toMutableMap()
        currentShortcuts[shortcut] = action
        _registeredShortcuts.value = currentShortcuts
    }
    
    /**
     * Unregisters a keyboard shortcut
     */
    fun unregisterShortcut(shortcut: KeyboardShortcut) {
        val currentShortcuts = _registeredShortcuts.value.toMutableMap()
        currentShortcuts.remove(shortcut)
        _registeredShortcuts.value = currentShortcuts
    }
    
    /**
     * Gets all registered shortcuts
     */
    fun getAllShortcuts(): Map<KeyboardShortcut, ShortcutAction> {
        return _registeredShortcuts.value
    }
    
    /**
     * Executes a shortcut action
     */
    private fun executeShortcut(action: ShortcutAction) {
        val focusedWindow = windowManager.getFocusedWindow()
        
        when (action) {
            is ShortcutAction.WindowAction -> {
                if (focusedWindow != null) {
                    action.execute(focusedWindow.id)
                }
            }
            is ShortcutAction.SystemAction -> {
                action.execute()
            }
            is ShortcutAction.CustomAction -> {
                action.execute()
            }
        }
    }
    
    /**
     * Updates modifier key states
     */
    private fun updateModifierStates(event: KeyEvent) {
        val currentStates = _modifierStates.value
        
        when (event.key) {
            Key.CtrlLeft, Key.CtrlRight -> {
                _modifierStates.value = currentStates.copy(
                    ctrlPressed = event.type == KeyEventType.KeyDown
                )
            }
            Key.ShiftLeft, Key.ShiftRight -> {
                _modifierStates.value = currentStates.copy(
                    shiftPressed = event.type == KeyEventType.KeyDown
                )
            }
            Key.AltLeft, Key.AltRight -> {
                _modifierStates.value = currentStates.copy(
                    altPressed = event.type == KeyEventType.KeyDown
                )
            }
            Key.MetaLeft, Key.MetaRight -> {
                _modifierStates.value = currentStates.copy(
                    metaPressed = event.type == KeyEventType.KeyDown
                )
            }
        }
    }
    
    /**
     * Registers default Windows-like shortcuts
     */
    private fun registerDefaultShortcuts() {
        val shortcuts = mapOf(
            // Window management
            KeyboardShortcut(Key.F4, alt = true) to ShortcutAction.WindowAction("Close Window") { windowId ->
                windowManager.closeWindow(windowId)
            },
            
            KeyboardShortcut(Key.Tab, alt = true) to ShortcutAction.SystemAction("Alt+Tab") {
                // Implement Alt+Tab functionality
                showWindowSwitcher()
            },
            
            KeyboardShortcut(Key.Escape, alt = true) to ShortcutAction.SystemAction("Alt+Escape") {
                // Minimize all windows
                windowManager.windows.values.forEach { window ->
                    if (window.isMinimizable) {
                        windowManager.minimizeWindow(window.id)
                    }
                }
            },
            
            // Window snapping
            KeyboardShortcut(Key.DirectionLeft, meta = true) to ShortcutAction.WindowAction("Snap Left") { windowId ->
                windowSnapManager.snapWindowLeft(windowId)
            },
            
            KeyboardShortcut(Key.DirectionRight, meta = true) to ShortcutAction.WindowAction("Snap Right") { windowId ->
                windowSnapManager.snapWindowRight(windowId)
            },
            
            KeyboardShortcut(Key.DirectionUp, meta = true) to ShortcutAction.WindowAction("Maximize") { windowId ->
                windowManager.maximizeWindow(windowId)
            },
            
            KeyboardShortcut(Key.DirectionDown, meta = true) to ShortcutAction.WindowAction("Minimize") { windowId ->
                windowManager.minimizeWindow(windowId)
            },
            
            // Quarter snapping
            KeyboardShortcut(Key.DirectionLeft, meta = true, shift = true) to ShortcutAction.WindowAction("Snap Top Left") { windowId ->
                windowSnapManager.snapWindowTopLeft(windowId)
            },
            
            KeyboardShortcut(Key.DirectionRight, meta = true, shift = true) to ShortcutAction.WindowAction("Snap Top Right") { windowId ->
                windowSnapManager.snapWindowTopRight(windowId)
            },
            
            KeyboardShortcut(Key.DirectionUp, meta = true, shift = true) to ShortcutAction.WindowAction("Snap Bottom Left") { windowId ->
                windowSnapManager.snapWindowBottomLeft(windowId)
            },
            
            KeyboardShortcut(Key.DirectionDown, meta = true, shift = true) to ShortcutAction.WindowAction("Snap Bottom Right") { windowId ->
                windowSnapManager.snapWindowBottomRight(windowId)
            },
            
            // System shortcuts
            KeyboardShortcut(Key.D, meta = true) to ShortcutAction.SystemAction("Show Desktop") {
                showDesktop()
            },
            
            KeyboardShortcut(Key.L, meta = true) to ShortcutAction.SystemAction("Lock Screen") {
                lockScreen()
            },
            
            KeyboardShortcut(Key.R, meta = true) to ShortcutAction.SystemAction("Run Dialog") {
                // Show run dialog
            },
            
            KeyboardShortcut(Key.E, meta = true) to ShortcutAction.SystemAction("File Manager") {
                // Open file manager
            },
            
            KeyboardShortcut(Key.I, meta = true) to ShortcutAction.SystemAction("Settings") {
                // Open settings
            },
            
            // Taskbar shortcuts
            KeyboardShortcut(Key.T, ctrl = true, shift = true) to ShortcutAction.SystemAction("Toggle Taskbar") {
                taskbarManager.toggleTaskbarVisibility()
            },
            
            // Function keys
            KeyboardShortcut(Key.F11) to ShortcutAction.WindowAction("Toggle Fullscreen") { windowId ->
                val window = windowManager.getWindow(windowId)
                if (window?.isMaximized() == true) {
                    windowManager.restoreWindow(windowId)
                } else {
                    windowManager.maximizeWindow(windowId)
                }
            },
            
            KeyboardShortcut(Key.F5) to ShortcutAction.SystemAction("Refresh") {
                // Refresh current window/desktop
            },
            
            // Number shortcuts for taskbar
            KeyboardShortcut(Key.One, meta = true) to ShortcutAction.SystemAction("Taskbar Item 1") {
                activateTaskbarItem(0)
            },
            KeyboardShortcut(Key.Two, meta = true) to ShortcutAction.SystemAction("Taskbar Item 2") {
                activateTaskbarItem(1)
            },
            KeyboardShortcut(Key.Three, meta = true) to ShortcutAction.SystemAction("Taskbar Item 3") {
                activateTaskbarItem(2)
            },
            KeyboardShortcut(Key.Four, meta = true) to ShortcutAction.SystemAction("Taskbar Item 4") {
                activateTaskbarItem(3)
            },
            KeyboardShortcut(Key.Five, meta = true) to ShortcutAction.SystemAction("Taskbar Item 5") {
                activateTaskbarItem(4)
            },
            KeyboardShortcut(Key.Six, meta = true) to ShortcutAction.SystemAction("Taskbar Item 6") {
                activateTaskbarItem(5)
            },
            KeyboardShortcut(Key.Seven, meta = true) to ShortcutAction.SystemAction("Taskbar Item 7") {
                activateTaskbarItem(6)
            },
            KeyboardShortcut(Key.Eight, meta = true) to ShortcutAction.SystemAction("Taskbar Item 8") {
                activateTaskbarItem(7)
            },
            KeyboardShortcut(Key.Nine, meta = true) to ShortcutAction.SystemAction("Taskbar Item 9") {
                activateTaskbarItem(8)
            },
            KeyboardShortcut(Key.Zero, meta = true) to ShortcutAction.SystemAction("Taskbar Item 10") {
                activateTaskbarItem(9)
            }
        )
        
        _registeredShortcuts.value = shortcuts
    }
    
    // Helper methods for shortcut actions
    
    private fun showWindowSwitcher() {
        // Implementation for Alt+Tab window switcher
        val visibleWindows = windowManager.getVisibleWindows()
        if (visibleWindows.size > 1) {
            val currentWindow = windowManager.getFocusedWindow()
            val currentIndex = visibleWindows.indexOf(currentWindow)
            val nextIndex = (currentIndex + 1) % visibleWindows.size
            val nextWindow = visibleWindows[nextIndex]
            
            windowManager.focusWindow(nextWindow.id)
        }
    }
    
    private fun showDesktop() {
        // Minimize all windows to show desktop
        windowManager.windows.values.forEach { window ->
            if (window.isMinimizable && window.isVisible) {
                windowManager.minimizeWindow(window.id)
            }
        }
    }
    
    private fun lockScreen() {
        // Implementation for screen lock
        // This would integrate with the system's lock screen functionality
    }
    
    private fun activateTaskbarItem(index: Int) {
        val taskbarItems = taskbarManager.taskbarItems.value
        if (index < taskbarItems.size) {
            val item = taskbarItems[index]
            taskbarManager.onTaskbarItemClick(item)
        }
    }
}

/**
 * Represents a keyboard shortcut combination
 */
data class KeyboardShortcut(
    val key: Key,
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false,
    val meta: Boolean = false
) {
    companion object {
        fun fromKeyEvent(event: KeyEvent): KeyboardShortcut {
            return KeyboardShortcut(
                key = event.key,
                ctrl = event.isCtrlPressed,
                shift = event.isShiftPressed,
                alt = event.isAltPressed,
                meta = event.isMetaPressed
            )
        }
    }
    
    override fun toString(): String {
        val modifiers = mutableListOf<String>()
        if (ctrl) modifiers.add("Ctrl")
        if (shift) modifiers.add("Shift")
        if (alt) modifiers.add("Alt")
        if (meta) modifiers.add("Win")
        
        return if (modifiers.isNotEmpty()) {
            "${modifiers.joinToString("+")}+${key.keyCode}"
        } else {
            key.keyCode.toString()
        }
    }
}

/**
 * Represents different types of shortcut actions
 */
sealed class ShortcutAction {
    data class WindowAction(
        val description: String,
        val execute: (windowId: String) -> Unit
    ) : ShortcutAction()
    
    data class SystemAction(
        val description: String,
        val execute: () -> Unit
    ) : ShortcutAction()
    
    data class CustomAction(
        val description: String,
        val execute: () -> Unit
    ) : ShortcutAction()
}

/**
 * Tracks the state of modifier keys
 */
data class ModifierStates(
    val ctrlPressed: Boolean = false,
    val shiftPressed: Boolean = false,
    val altPressed: Boolean = false,
    val metaPressed: Boolean = false
)

/**
 * Configuration for keyboard shortcuts
 */
data class KeyboardShortcutConfig(
    val enabled: Boolean = true,
    val customShortcuts: Map<KeyboardShortcut, ShortcutAction> = emptyMap(),
    val disabledShortcuts: Set<KeyboardShortcut> = emptySet()
)