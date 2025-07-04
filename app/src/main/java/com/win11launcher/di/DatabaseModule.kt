package com.win11launcher.di

import android.content.Context
import androidx.room.Room
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.database.NotesDatabase
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
}