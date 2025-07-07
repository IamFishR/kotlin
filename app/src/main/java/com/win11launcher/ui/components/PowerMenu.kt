package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.win11launcher.utils.SystemPowerManager

data class PowerOption(
    val text: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun PowerMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit
) {
    if (showMenu) {
        val context = LocalContext.current
        val powerManager = remember { SystemPowerManager(context) }
        
        val powerOptions = remember {
            listOf(
                PowerOption(
                    text = "Lock",
                    icon = Icons.Default.Lock,
                    action = { 
                        powerManager.lockScreen()
                        onDismiss()
                    }
                ),
                PowerOption(
                    text = "Restart",
                    icon = Icons.Default.RestartAlt,
                    action = { 
                        powerManager.restart()
                        onDismiss()
                    }
                ),
                PowerOption(
                    text = "Shut down",
                    icon = Icons.Default.PowerSettingsNew,
                    action = { 
                        powerManager.shutdown()
                        onDismiss()
                    }
                )
            )
        }

        Popup(
            alignment = Alignment.BottomEnd,
            offset = androidx.compose.ui.unit.IntOffset(-16, -80),
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .width(180.dp)
                    .blur(radius = 32.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D).copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    powerOptions.forEach { option ->
                        PowerMenuItem(
                            option = option,
                            onClick = option.action
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerMenuItem(
    option: PowerOption,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.text,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = option.text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}