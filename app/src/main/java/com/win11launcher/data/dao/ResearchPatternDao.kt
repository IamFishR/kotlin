package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.ResearchPattern
import kotlinx.coroutines.flow.Flow

@Dao
interface ResearchPatternDao {
    
    @Query("SELECT * FROM research_patterns ORDER BY trending_score DESC, relevance_score DESC")
    fun getAllPatterns(): Flow<List<ResearchPattern>>
    
    @Query("SELECT * FROM research_patterns WHERE id = :id")
    suspend fun getPatternById(id: String): ResearchPattern?
    
    @Query("SELECT * FROM research_patterns WHERE topic = :topic ORDER BY relevance_score DESC")
    suspend fun getPatternsByTopic(topic: String): List<ResearchPattern>
    
    @Query("SELECT * FROM research_patterns WHERE source_type = :sourceType ORDER BY trending_score DESC")
    suspend fun getPatternsBySourceType(sourceType: String): List<ResearchPattern>
    
    @Query("SELECT * FROM research_patterns WHERE relevance_score >= :minScore ORDER BY relevance_score DESC")
    suspend fun getHighRelevancePatterns(minScore: Float): List<ResearchPattern>
    
    @Query("SELECT * FROM research_patterns WHERE trending_score >= :minScore ORDER BY trending_score DESC")
    suspend fun getTrendingPatterns(minScore: Float): List<ResearchPattern>
    
    @Query("SELECT * FROM research_patterns WHERE has_technical_content = 1 ORDER BY relevance_score DESC")
    suspend fun getTechnicalPatterns(): List<ResearchPattern>
    
    @Query("SELECT * FROM research_patterns WHERE key_terms LIKE '%' || :term || '%' ORDER BY relevance_score DESC")
    suspend fun getPatternsByKeyTerm(term: String): List<ResearchPattern>
    
    @Query("SELECT * FROM research_patterns WHERE source_package = :packageName ORDER BY last_updated DESC")
    suspend fun getPatternsBySourcePackage(packageName: String): List<ResearchPattern>
    
    @Query("SELECT * FROM research_patterns WHERE last_updated >= :since ORDER BY last_updated DESC")
    suspend fun getRecentPatterns(since: Long): List<ResearchPattern>
    
    @Query("SELECT DISTINCT topic FROM research_patterns ORDER BY topic")
    suspend fun getAllTopics(): List<String>
    
    @Query("SELECT DISTINCT source_type FROM research_patterns ORDER BY source_type")
    suspend fun getAllSourceTypes(): List<String>
    
    @Query("SELECT COUNT(*) FROM research_patterns WHERE topic = :topic")
    suspend fun getPatternCountByTopic(topic: String): Int
    
    @Query("SELECT AVG(relevance_score) FROM research_patterns WHERE topic = :topic")
    suspend fun getAverageRelevanceByTopic(topic: String): Double?
    
    @Query("SELECT * FROM research_patterns WHERE topic IN (:topics) ORDER BY relevance_score DESC LIMIT :limit")
    suspend fun getTopPatternsByTopics(topics: List<String>, limit: Int): List<ResearchPattern>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: ResearchPattern)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<ResearchPattern>)
    
    @Update
    suspend fun updatePattern(pattern: ResearchPattern)
    
    @Delete
    suspend fun deletePattern(pattern: ResearchPattern)
    
    @Query("DELETE FROM research_patterns WHERE id = :id")
    suspend fun deletePatternById(id: String)
    
    @Query("DELETE FROM research_patterns WHERE last_updated < :cutoff")
    suspend fun deleteOldPatterns(cutoff: Long)
    
    @Query("DELETE FROM research_patterns WHERE relevance_score < :minScore AND trending_score < :minTrendingScore")
    suspend fun deleteLowScorePatterns(minScore: Float, minTrendingScore: Float)
    
    @Query("UPDATE research_patterns SET trending_score = :score, last_updated = :timestamp WHERE id = :id")
    suspend fun updateTrendingScore(id: String, score: Float, timestamp: Long)
    
    @Query("UPDATE research_patterns SET relevance_score = :score, last_updated = :timestamp WHERE id = :id")
    suspend fun updateRelevanceScore(id: String, score: Float, timestamp: Long)
}