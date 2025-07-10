
package com.win11launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CommandInfo(
    val name: String,
    val description: String,
    val usage: String,
    val example: String
)

data class CommandCategory(
    val name: String,
    val commands: List<CommandInfo>
)

@Composable
fun CommandsReferenceScreen() {
    val commandCategories = listOf(
        CommandCategory(
            name = "SYSTEM",
            commands = listOf(
                CommandInfo("device", "Device information (specs, build, hardware)", "device", "device"),
                CommandInfo("system", "System information (memory, storage, battery, processes)", "system", "system"),
                CommandInfo("memory", "Memory usage with detailed breakdown", "memory", "memory"),
                CommandInfo("storage", "Storage usage for different paths", "storage", "storage"),
                CommandInfo("date", "Date/time with custom formatting", "date", "date"),
                CommandInfo("version", "Application version information", "version", "version"),
                CommandInfo("uptime", "System uptime display", "uptime", "uptime"),
                CommandInfo("settings", "Open system settings (WiFi, Bluetooth, etc.)", "settings wifi", "settings bluetooth"),
                CommandInfo("monitor", "Comprehensive system monitoring", "monitor cpu", "monitor memory --detailed"),
                CommandInfo("snapshot", "System state management", "snapshot create --name=baseline", "snapshot list")
            )
        ),
        CommandCategory(
            name = "NETWORK",
            commands = listOf(
                CommandInfo("network", "Network status and interface information", "network", "network"),
                CommandInfo("wifi", "WiFi management (scan, connect, status, list)", "wifi scan", "wifi list"),
                CommandInfo("bluetooth", "Bluetooth management (scan, pair, status, list)", "bluetooth scan", "bluetooth list"),
                CommandInfo("ping", "Network connectivity testing", "ping 8.8.8.8", "ping google.com"),
                CommandInfo("netstat", "Network statistics and connections", "netstat", "netstat"),
                CommandInfo("wificonfig", "Advanced WiFi management", "wificonfig saved", "wificonfig signal"),
                CommandInfo("netmon", "Network monitoring and diagnostics", "netmon traffic", "netmon bandwidth"),
                CommandInfo("netprofile", "Network profile management", "netprofile", "netprofile")
            )
        ),
        CommandCategory(
            name = "APP",
            commands = listOf(
                CommandInfo("launch", "Launch applications by package name", "launch com.android.settings", "launch com.google.android.youtube"),
                CommandInfo("kill", "Terminate running applications", "kill com.android.settings", "kill com.google.android.youtube"),
                CommandInfo("apps", "List installed applications with filtering", "apps", "apps --user"),
                CommandInfo("appinfo", "Detailed application information", "appinfo com.android.settings", "appinfo com.google.android.youtube"),
                CommandInfo("uninstall", "Uninstall applications", "uninstall com.android.settings", "uninstall com.google.android.youtube"),
                CommandInfo("install", "Install APK files", "install /sdcard/app.apk", "install /sdcard/app.apk"),
                CommandInfo("clear-data", "Clear application data/cache", "clear-data com.android.settings", "clear-data com.google.android.youtube"),
                CommandInfo("permissions", "View application permissions", "permissions com.android.settings", "permissions com.google.android.youtube"),
                CommandInfo("appmon", "Real-time Application Monitoring", "appmon com.android.settings", "appmon com.google.android.youtube --time=5"),
                CommandInfo("appstats", "Application Usage Statistics & Analytics", "appstats", "appstats --usage"),
                CommandInfo("permcheck", "Advanced Permission Auditing", "permcheck", "permcheck --dangerous"),
                CommandInfo("permgrant", "Runtime Permission Granting", "permgrant com.android.settings android.permission.CAMERA", "permgrant com.google.android.youtube android.permission.READ_CONTACTS"),
                CommandInfo("permrevoke", "Runtime Permission Revocation", "permrevoke com.android.settings android.permission.CAMERA", "permrevoke com.google.android.youtube android.permission.READ_CONTACTS"),
                CommandInfo("appcleanup", "Smart Application Cleanup & Optimization", "appcleanup", "appcleanup --aggressive"),
                CommandInfo("appdeps", "Application Dependency Analysis", "appdeps com.android.settings", "appdeps com.google.android.youtube")
            )
        ),
        CommandCategory(
            name = "UTILITY",
            commands = listOf(
                CommandInfo("help", "Context-aware help system", "help", "help launch"),
                CommandInfo("echo", "Text display", "echo Hello World", "echo Hello World"),
                CommandInfo("clear/cls", "Clear command history", "clear", "cls"),
                CommandInfo("commands", "List all available commands", "commands", "commands")
            )
        ),
        CommandCategory(
            name = "FILE",
            commands = listOf(
                CommandInfo("ls", "List files and directories", "ls", "ls /sdcard/"),
                CommandInfo("cp", "Copy files", "cp /sdcard/file.txt /sdcard/file2.txt", "cp /sdcard/file.txt /sdcard/file2.txt"),
                CommandInfo("mv", "Move or rename files", "mv /sdcard/file.txt /sdcard/file3.txt", "mv /sdcard/file.txt /sdcard/file3.txt"),
                CommandInfo("rm", "Remove files or directories", "rm /sdcard/file.txt", "rm /sdcard/file.txt"),
                CommandInfo("mkdir", "Create directories", "mkdir /sdcard/new_dir", "mkdir /sdcard/new_dir"),
                CommandInfo("cat", "Display file content", "cat /sdcard/file.txt", "cat /sdcard/file.txt")
            )
        ),
        CommandCategory(
            name = "POWER",
            commands = listOf(
                CommandInfo("power", "Complete power management suite", "power battery", "power reboot"),
                CommandInfo("battery", "Advanced battery analysis", "battery", "battery"),
                CommandInfo("thermal", "Thermal monitoring", "thermal", "thermal"),
                CommandInfo("screen", "Display power management", "screen brightness 50", "screen timeout 60000")
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        items(commandCategories) { category ->
            Text(
                text = category.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            category.commands.forEach { command ->
                CommandCard(command)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CommandCard(command: CommandInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = command.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = command.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Usage:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = command.usage,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Example:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = command.example,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
