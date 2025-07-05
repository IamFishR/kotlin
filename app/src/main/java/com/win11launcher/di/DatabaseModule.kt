package com.win11launcher.di

import android.content.Context
import androidx.room.Room
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.dao.NoteDao
import com.win11launcher.data.dao.TrackingRuleDao
import com.win11launcher.data.dao.UserProfileDao
import com.win11launcher.data.database.NotesDatabase
import com.win11launcher.utils.ProfileImageManager
import com.win11launcher.services.AIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideNotesDatabase(@ApplicationContext context: Context): NotesDatabase {
        return NotesDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideAppSettingDao(database: NotesDatabase): AppSettingDao {
        return database.appSettingDao()
    }
    
    @Provides
    fun provideUserProfileDao(database: NotesDatabase): UserProfileDao {
        return database.userProfileDao()
    }
    
    @Provides
    fun provideNoteDao(database: NotesDatabase): NoteDao {
        return database.noteDao()
    }
    
    @Provides
    fun provideTrackingRuleDao(database: NotesDatabase): TrackingRuleDao {
        return database.trackingRuleDao()
    }
    
    @Provides
    @Singleton
    fun provideProfileImageManager(@ApplicationContext context: Context): ProfileImageManager {
        return ProfileImageManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAIService(@ApplicationContext context: Context): AIService {
        return AIService(context)
    }
}