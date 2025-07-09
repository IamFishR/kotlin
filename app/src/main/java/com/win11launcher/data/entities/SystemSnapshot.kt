package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "system_snapshots",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["trigger_command"])
    ]
)
data class SystemSnapshot(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "battery_level")
    val batteryLevel: Int,
    
    @ColumnInfo(name = "battery_health")
    val batteryHealth: String?,
    
    @ColumnInfo(name = "battery_temp")
    val batteryTemp: Float?,
    
    @ColumnInfo(name = "memory_usage")
    val memoryUsage: Long,
    
    @ColumnInfo(name = "cpu_usage")
    val cpuUsage: Float?,
    
    @ColumnInfo(name = "network_state")
    val networkState: String,               // WIFI, MOBILE, NONE
    
    @ColumnInfo(name = "wifi_ssid")
    val wifiSSID: String?,
    
    @ColumnInfo(name = "running_apps")
    val runningApps: String,                // JSON array of running apps
    
    @ColumnInfo(name = "trigger_command")
    val triggerCommand: String?,            // Command that triggered this snapshot
    
    @ColumnInfo(name = "custom_data")
    val customData: String?                 // JSON for extensibility
)