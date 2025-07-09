package com.win11launcher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.win11launcher.data.InstalledApp
import com.win11launcher.ui.layout.LayoutConstants

@Composable
fun AppIcon(
    app: InstalledApp,
    size: Dp = LayoutConstants.APP_ICON_SIZE,
    iconSize: Dp = LayoutConstants.APP_ICON_CONTENT_SIZE,
    backgroundColor: Color = Color(0xFF323233),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(LayoutConstants.APP_ICON_CORNER_RADIUS))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Convert Drawable to ImageBitmap for Compose
        val iconBitmap: ImageBitmap? = remember(app.iconDrawable) {
            app.iconDrawable?.let { drawable ->
                try {
                    val bitmap = drawable.toBitmap(
                        width = iconSize.value.toInt(),
                        height = iconSize.value.toInt()
                    )
                    bitmap.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
        }
        
        if (iconBitmap != null) {
            androidx.compose.foundation.Image(
                painter = BitmapPainter(iconBitmap),
                contentDescription = app.name,
                modifier = Modifier.size(iconSize)
            )
        } else {
            // Fallback for apps without icons or conversion errors
            FallbackAppIcon(
                appName = app.name,
                iconSize = iconSize
            )
        }
    }
}

@Composable
fun AppIconLarge(
    app: InstalledApp,
    modifier: Modifier = Modifier
) {
    AppIcon(
        app = app,
        size = LayoutConstants.ICON_MASSIVE,
        iconSize = LayoutConstants.ICON_EXTRA_LARGE,
        modifier = modifier
    )
}

@Composable
fun AppIconMedium(
    app: InstalledApp,
    modifier: Modifier = Modifier
) {
    AppIcon(
        app = app,
        size = LayoutConstants.ICON_HUGE,
        iconSize = LayoutConstants.ICON_LARGE,
        modifier = modifier
    )
}

@Composable
fun AppIconSmall(
    app: InstalledApp,
    modifier: Modifier = Modifier
) {
    AppIcon(
        app = app,
        size = LayoutConstants.APP_ICON_SIZE,
        iconSize = LayoutConstants.APP_ICON_CONTENT_SIZE,
        modifier = modifier
    )
}

@Composable
private fun FallbackAppIcon(
    appName: String,
    iconSize: Dp
) {
    Icon(
        imageVector = Icons.Default.Apps,
        contentDescription = appName,
        tint = Color.White,
        modifier = Modifier.size(iconSize)
    )
}

// Alternative approach for better performance - using Coil for image loading
@Composable
fun AppIconWithCoil(
    app: InstalledApp,
    size: Dp = LayoutConstants.APP_ICON_SIZE,
    iconSize: Dp = LayoutConstants.APP_ICON_CONTENT_SIZE,
    backgroundColor: Color = Color(0xFF323233),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(LayoutConstants.APP_ICON_CORNER_RADIUS))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconDrawable != null) {
            // Use Coil AsyncImage for better performance
            coil.compose.AsyncImage(
                model = app.iconDrawable,
                contentDescription = app.name,
                modifier = Modifier.size(iconSize),
                fallback = androidx.compose.ui.res.painterResource(
                    id = android.R.drawable.sym_def_app_icon
                )
            )
        } else {
            FallbackAppIcon(
                appName = app.name,
                iconSize = iconSize
            )
        }
    }
}