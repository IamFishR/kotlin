package com.win11launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.win11launcher.ui.theme.Win11Colors

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var appIcon by remember { mutableStateOf<BitmapPainter?>(null) }
    
    LaunchedEffect(packageName) {
        try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val drawable = packageManager.getApplicationIcon(appInfo)
            val bitmap = drawable.toBitmap()
            appIcon = BitmapPainter(bitmap.asImageBitmap())
        } catch (e: Exception) {
            appIcon = null
        }
    }
    
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Win11Colors.SystemAccent.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (appIcon != null) {
            Image(
                painter = appIcon!!,
                contentDescription = "App icon for $packageName",
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                Icons.Default.Android,
                contentDescription = "Default app icon",
                tint = Win11Colors.SystemAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}