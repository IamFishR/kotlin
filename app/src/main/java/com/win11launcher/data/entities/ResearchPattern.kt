package com.win11launcher.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "research_patterns",
    indices = [
        Index(value = ["topic"]),
        Index(value = ["source_type"]),
        Index(value = ["relevance_score"]),
        Index(value = ["trending_score"]),
        Index(value = ["last_updated"])
    ]
)
data class ResearchPattern(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "topic")
    val topic: String, // SOLAR_PANELS, RENEWABLE_ENERGY, BATTERY_TECH, ELECTRIC_VEHICLES
    
    @ColumnInfo(name = "source_type")
    val sourceType: String, // NEWS, RESEARCH_PAPER, INDUSTRY_REPORT, BLOG, SOCIAL_MEDIA
    
    @ColumnInfo(name = "key_terms")
    val keyTerms: String, // JSON array of technical terms
    
    @ColumnInfo(name = "relevance_score")
    val relevanceScore: Float,
    
    @ColumnInfo(name = "trending_score")
    val trendingScore: Float,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long,
    
    @ColumnInfo(name = "source_url")
    val sourceUrl: String?,
    
    @ColumnInfo(name = "source_package")
    val sourcePackage: String?, // News app package name
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float,
    
    @ColumnInfo(name = "language")
    val language: String = "en",
    
    @ColumnInfo(name = "content_length")
    val contentLength: Int,
    
    @ColumnInfo(name = "has_technical_content")
    val hasTechnicalContent: Boolean
)

// Research topic constants
object ResearchTopic {
    const val SOLAR_PANELS = "SOLAR_PANELS"
    const val RENEWABLE_ENERGY = "RENEWABLE_ENERGY"
    const val BATTERY_TECH = "BATTERY_TECH"
    const val ELECTRIC_VEHICLES = "ELECTRIC_VEHICLES"
    const val CLEAN_ENERGY = "CLEAN_ENERGY"
    const val ENERGY_STORAGE = "ENERGY_STORAGE"
    const val SMART_GRID = "SMART_GRID"
    const val WIND_ENERGY = "WIND_ENERGY"
    const val HYDROGEN_FUEL = "HYDROGEN_FUEL"
    const val CARBON_CAPTURE = "CARBON_CAPTURE"
    const val FINTECH = "FINTECH"
    const val CRYPTOCURRENCY = "CRYPTOCURRENCY"
    const val AI_ML = "AI_ML"
    const val ROBOTICS = "ROBOTICS"
    const val QUANTUM_COMPUTING = "QUANTUM_COMPUTING"
}

// Source type constants
object SourceType {
    const val NEWS = "NEWS"
    const val RESEARCH_PAPER = "RESEARCH_PAPER"
    const val INDUSTRY_REPORT = "INDUSTRY_REPORT"
    const val BLOG = "BLOG"
    const val SOCIAL_MEDIA = "SOCIAL_MEDIA"
    const val PATENT = "PATENT"
    const val CONFERENCE = "CONFERENCE"
    const val PRESS_RELEASE = "PRESS_RELEASE"
    const val GOVERNMENT_DOCUMENT = "GOVERNMENT_DOCUMENT"
    const val ACADEMIC_PAPER = "ACADEMIC_PAPER"
}