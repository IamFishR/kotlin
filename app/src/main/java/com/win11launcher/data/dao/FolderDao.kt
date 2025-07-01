package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    
    @Query("SELECT * FROM folders ORDER BY created_at ASC")
    fun getAllFolders(): Flow<List<Folder>>
    
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: String): Folder?
    
    @Query("SELECT * FROM folders WHERE name = :name")
    suspend fun getFolderByName(name: String): Folder?
    
    @Query("SELECT * FROM folders WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultFolder(): Folder?
    
    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFoldersCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<Folder>)
    
    @Update
    suspend fun updateFolder(folder: Folder)
    
    @Query("UPDATE folders SET name = :name, description = :description, color = :color, icon = :icon, updated_at = :updatedAt WHERE id = :folderId")
    suspend fun updateFolderDetails(folderId: String, name: String, description: String, color: String, icon: String, updatedAt: Long)
    
    @Delete
    suspend fun deleteFolder(folder: Folder)
    
    @Query("DELETE FROM folders WHERE id = :folderId AND is_default = 0")
    suspend fun deleteFolderById(folderId: String)
}