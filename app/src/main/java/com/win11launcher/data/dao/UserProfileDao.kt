package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.UserProfile
import com.win11launcher.data.entities.UserCustomization
import com.win11launcher.data.entities.UserFile
import com.win11launcher.data.entities.FileType
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    
    // User Profile operations
    @Query("SELECT * FROM user_profiles WHERE id = :profileId")
    suspend fun getUserProfile(profileId: String = "default"): UserProfile?
    
    @Query("SELECT * FROM user_profiles WHERE id = :profileId")
    fun getUserProfileFlow(profileId: String = "default"): Flow<UserProfile?>
    
    @Query("SELECT * FROM user_profiles ORDER BY isDefault DESC, createdAt ASC")
    fun getAllUserProfiles(): Flow<List<UserProfile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
    
    @Update
    suspend fun updateUserProfile(profile: UserProfile)
    
    @Query("DELETE FROM user_profiles WHERE id = :profileId")
    suspend fun deleteUserProfile(profileId: String)
    
    // Quick update methods
    @Query("UPDATE user_profiles SET username = :username, updatedAt = :timestamp WHERE id = :profileId")
    suspend fun updateUsername(
        profileId: String = "default", 
        username: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE user_profiles SET displayName = :displayName, updatedAt = :timestamp WHERE id = :profileId")
    suspend fun updateDisplayName(
        profileId: String = "default", 
        displayName: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE user_profiles SET profilePicturePath = :path, updatedAt = :timestamp WHERE id = :profileId")
    suspend fun updateProfilePicture(
        profileId: String = "default", 
        path: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE user_profiles SET themeColor = :color, updatedAt = :timestamp WHERE id = :profileId")
    suspend fun updateThemeColor(
        profileId: String = "default", 
        color: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE user_profiles SET bio = :bio, updatedAt = :timestamp WHERE id = :profileId")
    suspend fun updateBio(
        profileId: String = "default", 
        bio: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE user_profiles SET lastLoginAt = :timestamp WHERE id = :profileId")
    suspend fun updateLastLogin(
        profileId: String = "default", 
        timestamp: Long = System.currentTimeMillis()
    )
    
    // User Customization operations
    @Query("SELECT * FROM user_customizations WHERE profileId = :profileId")
    suspend fun getUserCustomization(profileId: String = "default"): UserCustomization?
    
    @Query("SELECT * FROM user_customizations WHERE profileId = :profileId")
    fun getUserCustomizationFlow(profileId: String = "default"): Flow<UserCustomization?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCustomization(customization: UserCustomization)
    
    @Update
    suspend fun updateUserCustomization(customization: UserCustomization)
    
    @Query("DELETE FROM user_customizations WHERE profileId = :profileId")
    suspend fun deleteUserCustomization(profileId: String)
    
    // User Files operations
    @Query("SELECT * FROM user_files WHERE id = :fileId")
    suspend fun getUserFile(fileId: String): UserFile?
    
    @Query("SELECT * FROM user_files WHERE profileId = :profileId AND fileType = :fileType AND isActive = 1 ORDER BY createdAt DESC")
    fun getUserFilesByType(profileId: String = "default", fileType: FileType): Flow<List<UserFile>>
    
    @Query("SELECT * FROM user_files WHERE profileId = :profileId AND isActive = 1 ORDER BY createdAt DESC")
    fun getAllUserFiles(profileId: String = "default"): Flow<List<UserFile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserFile(file: UserFile)
    
    @Update
    suspend fun updateUserFile(file: UserFile)
    
    @Query("DELETE FROM user_files WHERE id = :fileId")
    suspend fun deleteUserFile(fileId: String)
    
    @Query("UPDATE user_files SET isActive = 0 WHERE id = :fileId")
    suspend fun deactivateUserFile(fileId: String)
    
    @Query("UPDATE user_files SET lastAccessedAt = :timestamp WHERE id = :fileId")
    suspend fun updateFileLastAccessed(fileId: String, timestamp: Long = System.currentTimeMillis())
    
    // Get current profile picture
    @Query("""
        SELECT * FROM user_files 
        WHERE profileId = :profileId 
        AND fileType = :fileType 
        AND isActive = 1 
        ORDER BY createdAt DESC 
        LIMIT 1
    """)
    suspend fun getCurrentProfilePicture(
        profileId: String = "default", 
        fileType: FileType = FileType.PROFILE_PICTURE
    ): UserFile?
    
    // Analytics and helper queries
    @Query("SELECT COUNT(*) FROM user_files WHERE profileId = :profileId AND isActive = 1")
    suspend fun getUserFileCount(profileId: String = "default"): Int
    
    @Query("SELECT SUM(fileSize) FROM user_files WHERE profileId = :profileId AND isActive = 1")
    suspend fun getTotalFileSizeForUser(profileId: String = "default"): Long?
    
    @Query("SELECT * FROM user_files WHERE profileId = :profileId AND fileType = :fileType AND isActive = 1 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentFilesByType(
        profileId: String = "default", 
        fileType: FileType, 
        limit: Int = 10
    ): List<UserFile>
    
    // Cleanup operations
    @Query("DELETE FROM user_files WHERE isActive = 0 AND createdAt < :cutoffTime")
    suspend fun cleanupOldFiles(cutoffTime: Long)
    
    @Query("UPDATE user_files SET isActive = 0 WHERE profileId = :profileId AND fileType = :fileType AND id != :keepFileId")
    suspend fun deactivateOldFilesOfType(
        profileId: String = "default", 
        fileType: FileType, 
        keepFileId: String
    )
    
    // Complex queries with joins - simplified
    @Query("SELECT * FROM user_profiles WHERE id = :profileId")
    suspend fun getCompleteUserProfile(profileId: String = "default"): UserProfile?
}