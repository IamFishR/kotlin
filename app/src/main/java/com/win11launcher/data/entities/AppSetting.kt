package com.win11launcher.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val key: String,
    val value: String,
    val settingType: SettingType,
    val category: String,
    val description: String = "",
    val isUserModified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class SettingType {
    STRING,
    BOOLEAN,
    INTEGER,
    FLOAT,
    JSON
}

@Entity(tableName = "permission_states")
data class PermissionState(
    @PrimaryKey
    val permissionName: String,
    val isGranted: Boolean,
    val isRequired: Boolean,
    val requestCount: Int = 0,
    val lastRequestTime: Long? = null,
    val lastGrantedTime: Long? = null,
    val lastDeniedTime: Long? = null,
    val userNotes: String = "",
    val autoRequestEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)