package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "folders",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["created_at"])
    ]
)
data class Folder(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "color")
    val color: String = "#2196F3", // Default blue color
    
    @ColumnInfo(name = "icon")
    val icon: String = "folder", // Material icon name
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false
)