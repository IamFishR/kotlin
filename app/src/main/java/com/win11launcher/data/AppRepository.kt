package com.win11launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

data class InstalledApp(
    val name: String,
    val packageName: String,
    val iconDrawable: Drawable?, // Keep for now but will be optimized
    val activityName: String
)

class AppRepository(private val context: Context) {
    private val _installedApps = mutableStateOf<List<InstalledApp>>(emptyList())
    val installedApps: State<List<InstalledApp>> = _installedApps

    private val packageManager = context.packageManager

    fun loadInstalledApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                InstalledApp(
                    name = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    iconDrawable = try { resolveInfo.loadIcon(packageManager) } catch (e: Exception) { null },
                    activityName = resolveInfo.activityInfo.name
                )
            }
            .sortedBy { it.name }

        _installedApps.value = apps
    }

    fun launchApp(app: InstalledApp) {
        try {
            val intent = Intent().apply {
                setClassName(app.packageName, app.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: try to launch using package manager
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            launchIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        }
    }
}