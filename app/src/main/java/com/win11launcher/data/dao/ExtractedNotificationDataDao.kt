package com.win11launcher.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.win11launcher.data.entities.ExtractedNotificationData
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtractedNotificationDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ExtractedNotificationData)

    @Query("SELECT * FROM extracted_notification_data ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ExtractedNotificationData>>

    @Query("SELECT * FROM extracted_notification_data WHERE source_package = :packageName ORDER BY timestamp DESC")
    fun getByPackage(packageName: String): Flow<List<ExtractedNotificationData>>

    @Query("SELECT * FROM extracted_notification_data WHERE extracted_keywords LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    fun getByKeyword(keyword: String): Flow<List<ExtractedNotificationData>>

    @Query("DELETE FROM extracted_notification_data WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM extracted_notification_data")
    suspend fun deleteAll()

    @Query("DELETE FROM extracted_notification_data WHERE timestamp < :cutoff")
    suspend fun deleteOldData(cutoff: Long)
}