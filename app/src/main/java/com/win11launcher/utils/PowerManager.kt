package com.win11launcher.utils

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import java.io.IOException

class SystemPowerManager(private val context: Context) {
    
    fun lockScreen() {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (devicePolicyManager.isAdminActive(android.content.ComponentName(context, DeviceAdminReceiver::class.java))) {
                devicePolicyManager.lockNow()
            } else {
                // If device admin is not active, try alternative methods
                lockScreenAlternative()
            }
        } catch (e: Exception) {
            lockScreenAlternative()
        }
    }
    
    private fun lockScreenAlternative() {
        try {
            // Method 1: Try using accessibility service approach
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val intent = Intent(Intent.ACTION_SCREEN_OFF)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                // Method 2: For older versions, use power manager
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // This requires system-level permissions
                    Runtime.getRuntime().exec("input keyevent 26") // Power button press
                }
            }
        } catch (e: Exception) {
            // Fallback: Open lock screen settings
            openLockScreenSettings()
        }
    }
    
    private fun openLockScreenSettings() {
        try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Last resort: just show a toast or notification
        }
    }
    
    fun restart() {
        try {
            // Method 1: Try using root command
            Runtime.getRuntime().exec("su -c 'reboot'")
        } catch (e: IOException) {
            try {
                // Method 2: Try alternative reboot command
                Runtime.getRuntime().exec("reboot")
            } catch (e: IOException) {
                // Method 3: Fallback to restart intent
                restartFallback()
            }
        }
    }
    
    private fun restartFallback() {
        try {
            val intent = Intent(Intent.ACTION_REBOOT)
            intent.putExtra("nowait", 1)
            intent.putExtra("interval", 1)
            intent.putExtra("window", 0)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Open power settings as last resort
            openPowerSettings()
        }
    }
    
    fun shutdown() {
        try {
            // Method 1: Try using root command
            Runtime.getRuntime().exec("su -c 'shutdown -h now'")
        } catch (e: IOException) {
            try {
                // Method 2: Try alternative shutdown command
                Runtime.getRuntime().exec("reboot -p")
            } catch (e: IOException) {
                // Method 3: Fallback to shutdown intent
                shutdownFallback()
            }
        }
    }
    
    private fun shutdownFallback() {
        try {
            val intent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Open power settings as last resort
            openPowerSettings()
        }
    }
    
    private fun openPowerSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Final fallback - do nothing
        }
    }
}

// Device Admin Receiver class for lock screen functionality
class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
    // This class is required for device admin lock functionality
}