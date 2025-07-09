package com.win11launcher.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.win11launcher.data.entities.AppSetting
import com.win11launcher.data.entities.PermissionState
import com.win11launcher.data.entities.UserProfile
import com.win11launcher.data.entities.UserCustomization
import com.win11launcher.data.entities.UserFile
import com.win11launcher.data.entities.CommandHistory
import com.win11launcher.data.entities.CommandOutput
import com.win11launcher.data.entities.AIConversation
import com.win11launcher.data.entities.UserScript
import com.win11launcher.data.entities.SystemSnapshot
import com.win11launcher.data.entities.NetworkHistory
import com.win11launcher.data.entities.AppAction
import com.win11launcher.data.entities.FileOperation
import com.win11launcher.data.entities.DatabaseQuery
import com.win11launcher.data.entities.CommandUsage
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.dao.UserProfileDao
import com.win11launcher.data.dao.CommandHistoryDao
import com.win11launcher.data.dao.AIConversationDao
import com.win11launcher.data.dao.SystemMonitoringDao
import com.win11launcher.data.converters.Converters

@Database(
    entities = [
        AppSetting::class, 
        PermissionState::class, 
        UserProfile::class, 
        UserCustomization::class, 
        UserFile::class,
        CommandHistory::class,
        CommandOutput::class,
        AIConversation::class,
        UserScript::class,
        SystemSnapshot::class,
        NetworkHistory::class,
        AppAction::class,
        FileOperation::class,
        DatabaseQuery::class,
        CommandUsage::class
    ],
    version = 14,
    exportSchema = true
)
@androidx.room.TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    
    abstract fun appSettingDao(): AppSettingDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun aiConversationDao(): AIConversationDao
    abstract fun systemMonitoringDao(): SystemMonitoringDao
    
    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null
        
        fun getDatabase(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                )
                .addCallback(DatabaseCallback())
                .addMigrations(MIGRATION_13_14)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create command_history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS command_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        command TEXT NOT NULL,
                        command_type TEXT NOT NULL,
                        sub_command TEXT,
                        arguments TEXT,
                        timestamp INTEGER NOT NULL,
                        execution_time_ms INTEGER NOT NULL,
                        success INTEGER NOT NULL,
                        output_preview TEXT NOT NULL,
                        full_output_id TEXT,
                        session_id TEXT NOT NULL
                    )
                """)
                
                // Create command_outputs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS command_outputs (
                        id TEXT PRIMARY KEY NOT NULL,
                        command_id TEXT NOT NULL,
                        full_output TEXT NOT NULL,
                        output_type TEXT NOT NULL,
                        compressed INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY (command_id) REFERENCES command_history(id) ON DELETE CASCADE
                    )
                """)
                
                // Create ai_conversations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_conversations (
                        id TEXT PRIMARY KEY NOT NULL,
                        session_id TEXT NOT NULL,
                        prompt TEXT NOT NULL,
                        response TEXT NOT NULL,
                        model_used TEXT NOT NULL,
                        tokens_used INTEGER,
                        processing_time_ms INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        conversation_type TEXT NOT NULL,
                        context_data TEXT,
                        generated_commands TEXT,
                        user_rating INTEGER
                    )
                """)
                
                // Create user_scripts table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_scripts (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        commands TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        last_modified INTEGER NOT NULL,
                        last_executed INTEGER,
                        execution_count INTEGER NOT NULL DEFAULT 0,
                        is_favorite INTEGER NOT NULL DEFAULT 0,
                        tags TEXT,
                        schedule_type TEXT,
                        schedule_data TEXT
                    )
                """)
                
                // Create system_snapshots table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS system_snapshots (
                        id TEXT PRIMARY KEY NOT NULL,
                        timestamp INTEGER NOT NULL,
                        battery_level INTEGER NOT NULL,
                        battery_health TEXT,
                        battery_temp REAL,
                        memory_usage INTEGER NOT NULL,
                        cpu_usage REAL,
                        network_state TEXT NOT NULL,
                        wifi_ssid TEXT,
                        running_apps TEXT NOT NULL,
                        trigger_command TEXT,
                        custom_data TEXT
                    )
                """)
                
                // Create network_history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS network_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        type TEXT NOT NULL,
                        action TEXT NOT NULL,
                        target_identifier TEXT NOT NULL,
                        success INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        additional_data TEXT,
                        command_id TEXT,
                        FOREIGN KEY (command_id) REFERENCES command_history(id) ON DELETE SET NULL
                    )
                """)
                
                // Create app_actions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_actions (
                        id TEXT PRIMARY KEY NOT NULL,
                        package_name TEXT NOT NULL,
                        action TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        success INTEGER NOT NULL,
                        command_id TEXT,
                        additional_data TEXT,
                        FOREIGN KEY (command_id) REFERENCES command_history(id) ON DELETE SET NULL
                    )
                """)
                
                // Create file_operations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS file_operations (
                        id TEXT PRIMARY KEY NOT NULL,
                        operation TEXT NOT NULL,
                        source_path TEXT NOT NULL,
                        destination_path TEXT,
                        timestamp INTEGER NOT NULL,
                        success INTEGER NOT NULL,
                        files_affected INTEGER NOT NULL,
                        bytes_transferred INTEGER,
                        command_id TEXT,
                        FOREIGN KEY (command_id) REFERENCES command_history(id) ON DELETE SET NULL
                    )
                """)
                
                // Create db_queries table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS db_queries (
                        id TEXT PRIMARY KEY NOT NULL,
                        query TEXT NOT NULL,
                        table_name TEXT,
                        query_type TEXT NOT NULL,
                        result_count INTEGER NOT NULL,
                        execution_time_ms INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        success INTEGER NOT NULL,
                        error_message TEXT,
                        command_id TEXT,
                        FOREIGN KEY (command_id) REFERENCES command_history(id) ON DELETE SET NULL
                    )
                """)
                
                // Create command_usage table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS command_usage (
                        command TEXT PRIMARY KEY NOT NULL,
                        usage_count INTEGER NOT NULL DEFAULT 0,
                        last_used INTEGER,
                        success_rate REAL NOT NULL DEFAULT 0.0,
                        average_execution_time REAL NOT NULL DEFAULT 0.0,
                        total_execution_time INTEGER NOT NULL DEFAULT 0,
                        category TEXT NOT NULL
                    )
                """)
                
                // Create indices for performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_history_session_id ON command_history(session_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_history_command_type ON command_history(command_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_history_timestamp ON command_history(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_outputs_command_id ON command_outputs(command_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_outputs_timestamp ON command_outputs(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_conversations_session_id ON ai_conversations(session_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_conversations_conversation_type ON ai_conversations(conversation_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_conversations_timestamp ON ai_conversations(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_scripts_name ON user_scripts(name)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_scripts_created_at ON user_scripts(created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_scripts_last_executed ON user_scripts(last_executed)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_system_snapshots_timestamp ON system_snapshots(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_system_snapshots_trigger_command ON system_snapshots(trigger_command)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_network_history_type ON network_history(type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_network_history_timestamp ON network_history(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_network_history_command_id ON network_history(command_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_app_actions_package_name ON app_actions(package_name)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_app_actions_timestamp ON app_actions(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_app_actions_command_id ON app_actions(command_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_file_operations_operation ON file_operations(operation)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_file_operations_timestamp ON file_operations(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_file_operations_command_id ON file_operations(command_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_db_queries_query_type ON db_queries(query_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_db_queries_timestamp ON db_queries(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_db_queries_command_id ON db_queries(command_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_usage_category ON command_usage(category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_usage_last_used ON command_usage(last_used)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_command_usage_usage_count ON command_usage(usage_count)")
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default settings
                val currentTime = System.currentTimeMillis()
                db.execSQL("""
                    INSERT INTO app_settings (key, value, settingType, category, description, isUserModified, createdAt, updatedAt)
                    VALUES 
                    ('theme_mode', 'DARK', 'STRING', 'appearance', 'Application theme mode', 0, $currentTime, $currentTime),
                    ('launcher_auto_start', 'true', 'BOOLEAN', 'system', 'Start launcher automatically on boot', 0, $currentTime, $currentTime)
                """)
                
                // Insert default user profile
                db.execSQL("""
                    INSERT INTO user_profiles (id, username, displayName, profilePicturePath, profilePictureUri, backgroundImagePath, themeColor, bio, email, createdAt, updatedAt, isDefault)
                    VALUES ('default', 'User', '', '', '', '', '#0078D4', '', '', $currentTime, $currentTime, 1)
                """)
                
                // Insert default user customization
                db.execSQL("""
                    INSERT INTO user_customizations (profileId, startMenuLayout, taskbarPosition, showUserPictureInStartMenu, showUsernameInStartMenu, enableAnimations, enableSounds, autoHideTaskbar, transparencyEffects, fontSize, iconSize, cornerRadius, accentColor, createdAt, updatedAt)
                    VALUES ('default', 'DEFAULT', 'BOTTOM', 1, 1, 1, 0, 0, 1, 'MEDIUM', 'MEDIUM', 8, '#0078D4', $currentTime, $currentTime)
                """)
                
                // Insert default permission states
                db.execSQL("""
                    INSERT INTO permission_states (permissionName, isGranted, isRequired, requestCount, userNotes, autoRequestEnabled, createdAt, updatedAt)
                    VALUES 
                    ('android.permission.ACCESS_WIFI_STATE', 1, 1, 0, '', 1, $currentTime, $currentTime),
                    ('android.permission.CHANGE_WIFI_STATE', 1, 1, 0, '', 1, $currentTime, $currentTime),
                    ('android.permission.BLUETOOTH_CONNECT', 0, 0, 0, '', 1, $currentTime, $currentTime),
                    ('android.permission.ACCESS_FINE_LOCATION', 0, 0, 0, '', 1, $currentTime, $currentTime),
                    ('android.permission.SYSTEM_ALERT_WINDOW', 0, 0, 0, '', 1, $currentTime, $currentTime)
                """)
            }
        }
    }
}