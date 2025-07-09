package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "network_history",
    foreignKeys = [
        ForeignKey(
            entity = CommandHistory::class,
            parentColumns = ["id"],
            childColumns = ["command_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["type"]),
        Index(value = ["timestamp"]),
        Index(value = ["command_id"])
    ]
)
data class NetworkHistory(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "type")
    val type: String,                       // WIFI, BLUETOOTH
    
    @ColumnInfo(name = "action")
    val action: String,                     // SCAN, CONNECT, DISCONNECT, PAIR
    
    @ColumnInfo(name = "target_identifier")
    val targetIdentifier: String,           // SSID, MAC address, etc.
    
    @ColumnInfo(name = "success")
    val success: Boolean,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "additional_data")
    val additionalData: String?,            // JSON for signal strength, etc.
    
    @ColumnInfo(name = "command_id")
    val commandId: String?                  // Reference to command_history
)