package com.win11launcher.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
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
            val componentName = ComponentName(context, DeviceAdminReceiver::class.java)
            if (devicePolicyManager.isAdminActive(componentName)) {
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
            // Try to open security settings as fallback
            openLockScreenSettings()
        } catch (e: Exception) {
            // Handle gracefully - do nothing if settings can't be opened
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
            // Open power settings since we can't actually restart without system permissions
            openPowerSettings()
        } catch (e: Exception) {
            // Handle gracefully
        }
    }
    
    fun shutdown() {
        try {
            // Open power settings since we can't actually shutdown without system permissions
            openPowerSettings()
        } catch (e: Exception) {
            // Handle gracefully
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

