package com.win11launcher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.provider.MediaStore
import android.provider.CalendarContract
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import com.win11launcher.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri

data class PinnedApp(
    val name: String,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val packageName: String = "",
    val intentAction: String = "",
    val launchAction: AppLaunchAction
)

sealed class AppLaunchAction {
    object Settings : AppLaunchAction()
    object Files : AppLaunchAction()
    object Calculator : AppLaunchAction()
    object Camera : AppLaunchAction()
    object Photos : AppLaunchAction()
    object Store : AppLaunchAction()
    object Mail : AppLaunchAction()
    object Calendar : AppLaunchAction()
    object Music : AppLaunchAction()
    object Videos : AppLaunchAction()
    object Weather : AppLaunchAction()
    object News : AppLaunchAction()
    object NotesHub : AppLaunchAction()
    object AllNotifications : AppLaunchAction()
    data class Package(val packageName: String) : AppLaunchAction()
    data class Intent(val intent: android.content.Intent) : AppLaunchAction()
}

class AppLauncher(private val context: Context) {
    
    fun getPinnedApps(): List<PinnedApp> {
        return listOf(
            PinnedApp("Settings", icon = Icons.Default.Settings, launchAction = AppLaunchAction.Settings),
            PinnedApp("Files", icon = Icons.Default.Folder, launchAction = AppLaunchAction.Files),
            PinnedApp("Calculator", icon = Icons.Default.Calculate, launchAction = AppLaunchAction.Calculator),
            PinnedApp("Camera", icon = Icons.Default.Camera, launchAction = AppLaunchAction.Camera),
            PinnedApp("Photos", icon = Icons.Default.Photo, launchAction = AppLaunchAction.Photos),
            PinnedApp("Store", icon = Icons.Default.Store, launchAction = AppLaunchAction.Store),
            PinnedApp("Notes Hub", icon = Icons.Default.NoteAlt, launchAction = AppLaunchAction.NotesHub),
            PinnedApp("All Notifications", icon = Icons.Default.Notifications, launchAction = AppLaunchAction.AllNotifications),
            PinnedApp("Mail", icon = Icons.Default.Mail, launchAction = AppLaunchAction.Mail),
            PinnedApp("Calendar", icon = Icons.Default.CalendarToday, launchAction = AppLaunchAction.Calendar),
            PinnedApp("Music", icon = Icons.Default.MusicNote, launchAction = AppLaunchAction.Music),
            PinnedApp("Videos", icon = Icons.Default.VideoLibrary, launchAction = AppLaunchAction.Videos),
            PinnedApp("News", icon = Icons.Default.Article, launchAction = AppLaunchAction.News)
        )
    }
    
    fun launchApp(app: PinnedApp) {
        try {
            when (app.launchAction) {
                AppLaunchAction.Settings -> launchSettings()
                AppLaunchAction.Files -> launchFiles()
                AppLaunchAction.Calculator -> launchCalculator()
                AppLaunchAction.Camera -> launchCamera()
                AppLaunchAction.Photos -> launchPhotos()
                AppLaunchAction.Store -> launchStore()
                AppLaunchAction.Mail -> launchMail()
                AppLaunchAction.Calendar -> launchCalendar()
                AppLaunchAction.Music -> launchMusic()
                AppLaunchAction.Videos -> launchVideos()
                AppLaunchAction.Weather -> launchWeather()
                AppLaunchAction.News -> launchNews()
                AppLaunchAction.NotesHub -> { /* Handled by UI navigation */ }
                AppLaunchAction.AllNotifications -> { /* Handled by UI navigation */ }
                is AppLaunchAction.Package -> launchPackage(app.launchAction.packageName)
                is AppLaunchAction.Intent -> context.startActivity(app.launchAction.intent)
            }
        } catch (e: Exception) {
            // If specific app launch fails, try to find similar apps
            fallbackLaunch(app)
        }
    }
    
    private fun launchSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    
    private fun launchFiles() {
        val intents = listOf(
            // Try popular file managers first
            createPackageIntent("com.google.android.documentsui"), // Google Files
            createPackageIntent("com.android.documentsui"), // Android Files
            createPackageIntent("com.estrongs.android.pop"), // ES File Explorer
            createPackageIntent("com.mi.android.globalFileexplorer"), // Mi File Manager
            // Fallback to generic file browser
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchCalculator() {
        val intents = listOf(
            createPackageIntent("com.google.android.calculator"), // Google Calculator
            createPackageIntent("com.android.calculator2"), // Android Calculator
            createPackageIntent("com.miui.calculator"), // MIUI Calculator
            createPackageIntent("com.samsung.android.calculator"), // Samsung Calculator
            // Generic calculator intent
            Intent().apply {
                action = "android.intent.action.MAIN"
                addCategory("android.intent.category.APP_CALCULATOR")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    
    private fun launchPhotos() {
        val intents = listOf(
            createPackageIntent("com.google.android.apps.photos"), // Google Photos
            createPackageIntent("com.miui.gallery"), // MIUI Gallery
            createPackageIntent("com.samsung.android.gallery3d"), // Samsung Gallery
            Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchStore() {
        val intents = listOf(
            createPackageIntent("com.android.vending"), // Google Play Store
            createPackageIntent("com.xiaomi.market"), // Mi Store
            createPackageIntent("com.samsung.android.galaxystore"), // Galaxy Store
            Intent(Intent.ACTION_VIEW).apply {
                data = "market://".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchMail() {
        val intents = listOf(
            createPackageIntent("com.google.android.gm"), // Gmail
            createPackageIntent("com.microsoft.office.outlook"), // Outlook
            createPackageIntent("com.samsung.android.email.provider"), // Samsung Email
            Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchCalendar() {
        val intents = listOf(
            createPackageIntent("com.google.android.calendar"), // Google Calendar
            createPackageIntent("com.samsung.android.calendar"), // Samsung Calendar
            Intent(Intent.ACTION_VIEW).apply {
                data = CalendarContract.CONTENT_URI
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchMusic() {
        val intents = listOf(
            createPackageIntent("com.spotify.music"), // Spotify
            createPackageIntent("com.google.android.music"), // Google Play Music
            createPackageIntent("com.amazon.mp3"), // Amazon Music
            Intent(Intent.ACTION_VIEW).apply {
                type = "audio/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchVideos() {
        val intents = listOf(
            createPackageIntent("com.google.android.videos"), // Google Play Movies
            createPackageIntent("com.netflix.mediaclient"), // Netflix
            createPackageIntent("com.amazon.avod.thirdpartyclient"), // Prime Video
            Intent(Intent.ACTION_VIEW).apply {
                type = "video/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchWeather() {
        val intents = listOf(
            createPackageIntent("com.google.android.googlequicksearchbox"), // Google Weather
            createPackageIntent("com.weather.Weather"), // Weather app
            createPackageIntent("com.miui.weather2"), // MIUI Weather
            Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", "weather")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchNews() {
        val intents = listOf(
            createPackageIntent("com.google.android.apps.magazines"), // Google News
            createPackageIntent("com.microsoft.amp.apps.bingnews"), // Microsoft News
            createPackageIntent("flipboard.app"), // Flipboard
            Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", "news")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        
        tryLaunchIntents(intents)
    }
    
    private fun launchPackage(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
    
    private fun createPackageIntent(packageName: String): Intent? {
        return try {
            context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun tryLaunchIntents(intents: List<Intent?>) {
        for (intent in intents) {
            intent?.let {
                try {
                    context.startActivity(it)
                    return // Successfully launched, exit
                } catch (e: Exception) {
                    // Continue to next intent
                }
            }
        }
    }
    
    private fun fallbackLaunch(app: PinnedApp) {
        // As a last resort, try to find any app with similar functionality
        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", app.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            context.startActivity(searchIntent)
        } catch (e: Exception) {
            // Even web search failed, do nothing
        }
    }
}