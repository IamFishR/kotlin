package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.FinancialPattern
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialPatternDao {
    
    @Query("SELECT * FROM financial_patterns ORDER BY last_seen DESC")
    fun getAllPatterns(): Flow<List<FinancialPattern>>
    
    @Query("SELECT * FROM financial_patterns WHERE id = :id")
    suspend fun getPatternById(id: String): FinancialPattern?
    
    @Query("SELECT * FROM financial_patterns WHERE transaction_type = :type ORDER BY confidence DESC")
    suspend fun getPatternsByType(type: String): List<FinancialPattern>
    
    @Query("SELECT * FROM financial_patterns WHERE category = :category ORDER BY confidence DESC")
    suspend fun getPatternsByCategory(category: String): List<FinancialPattern>
    
    @Query("SELECT * FROM financial_patterns WHERE bank_name = :bankName ORDER BY last_seen DESC")
    suspend fun getPatternsByBank(bankName: String): List<FinancialPattern>
    
    @Query("SELECT * FROM financial_patterns WHERE is_recurring = 1 ORDER BY confidence DESC")
    fun getRecurringPatterns(): Flow<List<FinancialPattern>>
    
    @Query("SELECT * FROM financial_patterns WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    suspend fun getHighConfidencePatterns(minConfidence: Float): List<FinancialPattern>
    
    @Query("SELECT * FROM financial_patterns WHERE merchant LIKE '%' || :merchantName || '%' ORDER BY confidence DESC")
    suspend fun getPatternsByMerchant(merchantName: String): List<FinancialPattern>
    
    @Query("SELECT * FROM financial_patterns WHERE source_package = :packageName ORDER BY last_seen DESC")
    suspend fun getPatternsBySourcePackage(packageName: String): List<FinancialPattern>
    
    @Query("SELECT DISTINCT category FROM financial_patterns ORDER BY category")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT DISTINCT bank_name FROM financial_patterns WHERE bank_name IS NOT NULL ORDER BY bank_name")
    suspend fun getAllBanks(): List<String>
    
    @Query("SELECT DISTINCT merchant FROM financial_patterns WHERE merchant IS NOT NULL ORDER BY merchant")
    suspend fun getAllMerchants(): List<String>
    
    @Query("SELECT * FROM financial_patterns WHERE last_seen >= :since ORDER BY last_seen DESC")
    suspend fun getRecentPatterns(since: Long): List<FinancialPattern>
    
    @Query("SELECT COUNT(*) FROM financial_patterns WHERE category = :category")
    suspend fun getPatternCountByCategory(category: String): Int
    
    @Query("SELECT AVG(amount) FROM financial_patterns WHERE category = :category AND amount IS NOT NULL")
    suspend fun getAverageAmountByCategory(category: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: FinancialPattern)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<FinancialPattern>)
    
    @Update
    suspend fun updatePattern(pattern: FinancialPattern)
    
    @Delete
    suspend fun deletePattern(pattern: FinancialPattern)
    
    @Query("DELETE FROM financial_patterns WHERE id = :id")
    suspend fun deletePatternById(id: String)
    
    @Query("DELETE FROM financial_patterns WHERE last_seen < :cutoff")
    suspend fun deleteOldPatterns(cutoff: Long)
    
    @Query("DELETE FROM financial_patterns WHERE confidence < :minConfidence")
    suspend fun deleteLowConfidencePatterns(minConfidence: Float)
    
    @Query("UPDATE financial_patterns SET occurrence_count = occurrence_count + 1, last_seen = :timestamp, updated_at = :timestamp WHERE id = :id")
    suspend fun incrementOccurrenceCount(id: String, timestamp: Long)
    
    @Query("UPDATE financial_patterns SET confidence = :confidence, updated_at = :timestamp WHERE id = :id")
    suspend fun updateConfidence(id: String, confidence: Float, timestamp: Long)
}