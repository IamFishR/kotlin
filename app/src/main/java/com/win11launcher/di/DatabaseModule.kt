package com.win11launcher.di

import android.content.Context
import androidx.room.Room
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.dao.UserProfileDao
import com.win11launcher.data.dao.CommandHistoryDao
import com.win11launcher.data.dao.AIConversationDao
import com.win11launcher.data.dao.SystemMonitoringDao
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
    fun provideCommandHistoryDao(database: NotesDatabase): CommandHistoryDao {
        return database.commandHistoryDao()
    }
    
    @Provides
    fun provideAIConversationDao(database: NotesDatabase): AIConversationDao {
        return database.aiConversationDao()
    }
    
    @Provides
    fun provideSystemMonitoringDao(database: NotesDatabase): SystemMonitoringDao {
        return database.systemMonitoringDao()
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