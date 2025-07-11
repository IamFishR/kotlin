package com.win11launcher.di

import android.content.Context
import androidx.room.Room
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.dao.UserProfileDao
import com.win11launcher.data.dao.CommandHistoryDao
import com.win11launcher.data.dao.AIConversationDao
import com.win11launcher.data.dao.SystemMonitoringDao
import com.win11launcher.data.dao.AIMemoryDao
import com.win11launcher.data.database.NotesDatabase
import com.win11launcher.data.repositories.CommandLineRepository
import com.win11launcher.data.repositories.UserProfileRepository
import com.win11launcher.data.database.DatabaseCleanupManager
import com.win11launcher.command.CommandRegistry
import com.win11launcher.command.CommandParser
import com.win11launcher.command.CommandExecutionEngine
import com.win11launcher.utils.ProfileImageManager
import com.win11launcher.services.AIService
import com.win11launcher.services.AIMemoryManager
import com.win11launcher.services.AICommandInterpreter
import com.win11launcher.services.SmartCommandCompletion
import com.win11launcher.services.AIPerformanceAnalytics
import com.win11launcher.command.commands.AICommandProvider
import com.win11launcher.command.commands.AskCommandExecutor
import com.win11launcher.command.commands.InterpretCommandExecutor
import com.win11launcher.command.commands.AnalyzeCommandExecutor
import com.win11launcher.command.commands.AIMemoryCommandExecutor
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
    fun provideAIMemoryDao(database: NotesDatabase): AIMemoryDao {
        return database.aiMemoryDao()
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
    
    @Provides
    @Singleton
    fun provideCommandLineRepository(
        commandHistoryDao: CommandHistoryDao,
        aiConversationDao: AIConversationDao,
        systemMonitoringDao: SystemMonitoringDao
    ): CommandLineRepository {
        return CommandLineRepository(commandHistoryDao, aiConversationDao, systemMonitoringDao)
    }
    
    @Provides
    @Singleton
    fun provideAIMemoryManager(
        aiMemoryDao: AIMemoryDao,
        aiService: AIService
    ): AIMemoryManager {
        return AIMemoryManager(aiMemoryDao, aiService)
    }
    
    @Provides
    @Singleton
    fun provideAICommandInterpreter(
        aiService: AIService,
        commandRegistry: CommandRegistry,
        commandLineRepository: CommandLineRepository,
        aiMemoryManager: AIMemoryManager
    ): AICommandInterpreter {
        return AICommandInterpreter(aiService, commandRegistry, commandLineRepository, aiMemoryManager)
    }
    
    @Provides
    @Singleton
    fun provideSmartCommandCompletion(
        commandRegistry: CommandRegistry,
        commandLineRepository: CommandLineRepository,
        aiMemoryManager: AIMemoryManager,
        aiService: AIService
    ): SmartCommandCompletion {
        return SmartCommandCompletion(commandRegistry, commandLineRepository, aiMemoryManager, aiService)
    }
    
    @Provides
    @Singleton
    fun provideAIPerformanceAnalytics(
        commandLineRepository: CommandLineRepository,
        aiMemoryManager: AIMemoryManager,
        aiService: AIService
    ): AIPerformanceAnalytics {
        return AIPerformanceAnalytics(commandLineRepository, aiMemoryManager, aiService)
    }
    
    // AI Command Executors
    @Provides
    @Singleton
    fun provideAskCommandExecutor(
        aiService: AIService,
        aiMemoryManager: AIMemoryManager,
        userProfileRepository: UserProfileRepository
    ): AskCommandExecutor {
        return AskCommandExecutor(aiService, aiMemoryManager, userProfileRepository)
    }
    
    @Provides
    @Singleton
    fun provideInterpretCommandExecutor(
        aiCommandInterpreter: AICommandInterpreter
    ): InterpretCommandExecutor {
        return InterpretCommandExecutor(aiCommandInterpreter)
    }
    
    @Provides
    @Singleton
    fun provideAnalyzeCommandExecutor(
        aiCommandInterpreter: AICommandInterpreter
    ): AnalyzeCommandExecutor {
        return AnalyzeCommandExecutor(aiCommandInterpreter)
    }
    
    @Provides
    @Singleton
    fun provideMemoryCommandExecutor(
        aiMemoryManager: AIMemoryManager,
        userProfileRepository: UserProfileRepository
    ): AIMemoryCommandExecutor {
        return AIMemoryCommandExecutor(aiMemoryManager, userProfileRepository)
    }
    
    @Provides
    @Singleton
    fun provideAICommandProvider(
        askExecutor: AskCommandExecutor,
        interpretExecutor: InterpretCommandExecutor,
        analyzeExecutor: AnalyzeCommandExecutor,
        memoryExecutor: AIMemoryCommandExecutor
    ): AICommandProvider {
        return AICommandProvider(askExecutor, interpretExecutor, analyzeExecutor, memoryExecutor)
    }
    
    @Provides
    @Singleton
    fun provideCommandRegistry(): CommandRegistry {
        return CommandRegistry()
    }
    
    @Provides
    @Singleton
    fun provideCommandParser(commandRegistry: CommandRegistry): CommandParser {
        return CommandParser(commandRegistry)
    }
    
    @Provides
    @Singleton
    fun provideCommandExecutionEngine(
        commandRegistry: CommandRegistry,
        commandParser: CommandParser,
        commandLineRepository: CommandLineRepository,
        aiCommandProvider: AICommandProvider
    ): CommandExecutionEngine {
        return CommandExecutionEngine(commandRegistry, commandParser, commandLineRepository, aiCommandProvider)
    }
    
    @Provides
    @Singleton
    fun provideDatabaseCleanupManager(
        commandLineRepository: CommandLineRepository
    ): DatabaseCleanupManager {
        return DatabaseCleanupManager(commandLineRepository)
    }
}