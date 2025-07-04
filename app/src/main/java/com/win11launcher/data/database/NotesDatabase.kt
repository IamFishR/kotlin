package com.win11launcher.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.win11launcher.data.entities.Note
import com.win11launcher.data.entities.Folder
import com.win11launcher.data.entities.TrackingRule
import com.win11launcher.data.entities.RuleActivity
import com.win11launcher.data.entities.FinancialPattern
import com.win11launcher.data.entities.ResearchPattern
import com.win11launcher.data.entities.SmartSuggestion
import com.win11launcher.data.entities.AppSetting
import com.win11launcher.data.entities.PermissionState
import com.win11launcher.data.entities.UserProfile
import com.win11launcher.data.entities.UserCustomization
import com.win11launcher.data.entities.UserFile
import com.win11launcher.data.dao.NoteDao
import com.win11launcher.data.dao.FolderDao
import com.win11launcher.data.dao.TrackingRuleDao
import com.win11launcher.data.dao.RuleActivityDao
import com.win11launcher.data.dao.FinancialPatternDao
import com.win11launcher.data.dao.ResearchPatternDao
import com.win11launcher.data.dao.SmartSuggestionDao
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.dao.UserProfileDao

@Database(
    entities = [Note::class, Folder::class, TrackingRule::class, RuleActivity::class, FinancialPattern::class, ResearchPattern::class, SmartSuggestion::class, AppSetting::class, PermissionState::class, UserProfile::class, UserCustomization::class, UserFile::class],
    version = 5,
    exportSchema = true
)
abstract class NotesDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun trackingRuleDao(): TrackingRuleDao
    abstract fun ruleActivityDao(): RuleActivityDao
    abstract fun financialPatternDao(): FinancialPatternDao
    abstract fun researchPatternDao(): ResearchPatternDao
    abstract fun smartSuggestionDao(): SmartSuggestionDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun userProfileDao(): UserProfileDao
    
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create financial_patterns table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS financial_patterns (
                        id TEXT NOT NULL PRIMARY KEY,
                        transaction_type TEXT NOT NULL,
                        amount REAL,
                        merchant TEXT,
                        category TEXT NOT NULL,
                        bank_name TEXT,
                        frequency TEXT NOT NULL,
                        time_pattern TEXT NOT NULL,
                        is_recurring INTEGER NOT NULL,
                        last_seen INTEGER NOT NULL,
                        confidence REAL NOT NULL,
                        source_package TEXT,
                        pattern_keywords TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        occurrence_count INTEGER NOT NULL
                    )
                """)
                
                // Create indices for financial_patterns
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_patterns_transaction_type ON financial_patterns (transaction_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_patterns_category ON financial_patterns (category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_patterns_bank_name ON financial_patterns (bank_name)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_patterns_is_recurring ON financial_patterns (is_recurring)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_patterns_last_seen ON financial_patterns (last_seen)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_patterns_confidence ON financial_patterns (confidence)")
                
                // Create research_patterns table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS research_patterns (
                        id TEXT NOT NULL PRIMARY KEY,
                        topic TEXT NOT NULL,
                        source_type TEXT NOT NULL,
                        key_terms TEXT NOT NULL,
                        relevance_score REAL NOT NULL,
                        trending_score REAL NOT NULL,
                        last_updated INTEGER NOT NULL,
                        source_url TEXT,
                        source_package TEXT,
                        created_at INTEGER NOT NULL,
                        confidence_score REAL NOT NULL,
                        language TEXT NOT NULL,
                        content_length INTEGER NOT NULL,
                        has_technical_content INTEGER NOT NULL
                    )
                """)
                
                // Create indices for research_patterns
                database.execSQL("CREATE INDEX IF NOT EXISTS index_research_patterns_topic ON research_patterns (topic)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_research_patterns_source_type ON research_patterns (source_type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_research_patterns_relevance_score ON research_patterns (relevance_score)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_research_patterns_trending_score ON research_patterns (trending_score)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_research_patterns_last_updated ON research_patterns (last_updated)")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create smart_suggestions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS smart_suggestions (
                        id TEXT NOT NULL PRIMARY KEY,
                        category TEXT NOT NULL,
                        sub_category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        automated_rule_config TEXT NOT NULL,
                        expected_benefit TEXT NOT NULL,
                        confidence_score REAL NOT NULL,
                        priority INTEGER NOT NULL,
                        is_finance_related INTEGER NOT NULL,
                        estimated_savings REAL,
                        savings_type TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        is_dismissed INTEGER NOT NULL DEFAULT 0,
                        is_applied INTEGER NOT NULL DEFAULT 0,
                        dismissal_reason TEXT,
                        application_date INTEGER,
                        source_patterns TEXT,
                        suggested_folder_name TEXT,
                        suggested_folder_color TEXT,
                        suggested_folder_icon TEXT,
                        success_metrics TEXT
                    )
                """)
                
                // Create indices for smart_suggestions
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_category ON smart_suggestions (category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_sub_category ON smart_suggestions (sub_category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_confidence_score ON smart_suggestions (confidence_score)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_priority ON smart_suggestions (priority)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_is_finance_related ON smart_suggestions (is_finance_related)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_created_at ON smart_suggestions (created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_is_dismissed ON smart_suggestions (is_dismissed)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_smart_suggestions_is_applied ON smart_suggestions (is_applied)")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create app_settings table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_settings (
                        key TEXT NOT NULL PRIMARY KEY,
                        value TEXT NOT NULL,
                        settingType TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        isUserModified INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // Create permission_states table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS permission_states (
                        permissionName TEXT NOT NULL PRIMARY KEY,
                        isGranted INTEGER NOT NULL,
                        isRequired INTEGER NOT NULL,
                        requestCount INTEGER NOT NULL DEFAULT 0,
                        lastRequestTime INTEGER,
                        lastGrantedTime INTEGER,
                        lastDeniedTime INTEGER,
                        userNotes TEXT NOT NULL DEFAULT '',
                        autoRequestEnabled INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // Create indices for app_settings
                database.execSQL("CREATE INDEX IF NOT EXISTS index_app_settings_category ON app_settings (category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_app_settings_settingType ON app_settings (settingType)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_app_settings_isUserModified ON app_settings (isUserModified)")
                
                // Create indices for permission_states
                database.execSQL("CREATE INDEX IF NOT EXISTS index_permission_states_isGranted ON permission_states (isGranted)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_permission_states_isRequired ON permission_states (isRequired)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_permission_states_requestCount ON permission_states (requestCount)")
                
                // Insert default settings
                val currentTime = System.currentTimeMillis()
                database.execSQL("""
                    INSERT INTO app_settings (key, value, settingType, category, description, createdAt, updatedAt)
                    VALUES 
                    ('theme_mode', 'DARK', 'STRING', 'appearance', 'Application theme mode', $currentTime, $currentTime),
                    ('auto_check_permissions', 'true', 'BOOLEAN', 'permissions', 'Automatically check permission status', $currentTime, $currentTime),
                    ('show_permission_notifications', 'true', 'BOOLEAN', 'permissions', 'Show notifications for permission changes', $currentTime, $currentTime),
                    ('launcher_auto_start', 'true', 'BOOLEAN', 'system', 'Start launcher automatically on boot', $currentTime, $currentTime)
                """)
                
                // Insert default permission states
                database.execSQL("""
                    INSERT INTO permission_states (permissionName, isGranted, isRequired, createdAt, updatedAt)
                    VALUES 
                    ('android.permission.ACCESS_WIFI_STATE', 1, 1, $currentTime, $currentTime),
                    ('android.permission.CHANGE_WIFI_STATE', 1, 1, $currentTime, $currentTime),
                    ('android.permission.BLUETOOTH_CONNECT', 0, 0, $currentTime, $currentTime),
                    ('android.permission.ACCESS_FINE_LOCATION', 0, 0, $currentTime, $currentTime),
                    ('android.permission.SYSTEM_ALERT_WINDOW', 0, 0, $currentTime, $currentTime)
                """)
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create user_profiles table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_profiles (
                        id TEXT NOT NULL PRIMARY KEY,
                        username TEXT NOT NULL DEFAULT 'User',
                        displayName TEXT NOT NULL DEFAULT '',
                        profilePicturePath TEXT NOT NULL DEFAULT '',
                        profilePictureUri TEXT NOT NULL DEFAULT '',
                        backgroundImagePath TEXT NOT NULL DEFAULT '',
                        themeColor TEXT NOT NULL DEFAULT '#0078D4',
                        bio TEXT NOT NULL DEFAULT '',
                        email TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        lastLoginAt INTEGER,
                        isDefault INTEGER NOT NULL DEFAULT 1
                    )
                """)
                
                // Create user_customizations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_customizations (
                        profileId TEXT NOT NULL PRIMARY KEY,
                        startMenuLayout TEXT NOT NULL DEFAULT 'DEFAULT',
                        taskbarPosition TEXT NOT NULL DEFAULT 'BOTTOM',
                        showUserPictureInStartMenu INTEGER NOT NULL DEFAULT 1,
                        showUsernameInStartMenu INTEGER NOT NULL DEFAULT 1,
                        enableAnimations INTEGER NOT NULL DEFAULT 1,
                        enableSounds INTEGER NOT NULL DEFAULT 0,
                        autoHideTaskbar INTEGER NOT NULL DEFAULT 0,
                        transparencyEffects INTEGER NOT NULL DEFAULT 1,
                        fontSize TEXT NOT NULL DEFAULT 'MEDIUM',
                        iconSize TEXT NOT NULL DEFAULT 'MEDIUM',
                        cornerRadius INTEGER NOT NULL DEFAULT 8,
                        accentColor TEXT NOT NULL DEFAULT '#0078D4',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // Create user_files table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_files (
                        id TEXT NOT NULL PRIMARY KEY,
                        profileId TEXT NOT NULL,
                        fileName TEXT NOT NULL,
                        originalFileName TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        fileType TEXT NOT NULL,
                        fileSize INTEGER NOT NULL,
                        mimeType TEXT NOT NULL DEFAULT '',
                        checksum TEXT NOT NULL DEFAULT '',
                        isCompressed INTEGER NOT NULL DEFAULT 0,
                        compressionRatio REAL NOT NULL DEFAULT 1.0,
                        createdAt INTEGER NOT NULL,
                        lastAccessedAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """)
                
                // Create indices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_profiles_isDefault ON user_profiles (isDefault)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_profiles_username ON user_profiles (username)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_customizations_profileId ON user_customizations (profileId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_files_profileId ON user_files (profileId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_files_fileType ON user_files (fileType)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_files_isActive ON user_files (isActive)")
                
                // Insert default user profile
                val currentTime = System.currentTimeMillis()
                database.execSQL("""
                    INSERT INTO user_profiles (id, username, displayName, createdAt, updatedAt, isDefault)
                    VALUES ('default', 'User', '', $currentTime, $currentTime, 1)
                """)
                
                // Insert default customization
                database.execSQL("""
                    INSERT INTO user_customizations (profileId, createdAt, updatedAt)
                    VALUES ('default', $currentTime, $currentTime)
                """)
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default folder
                db.execSQL("""
                    INSERT INTO folders (id, name, description, color, icon, created_at, updated_at, is_default)
                    VALUES ('default', 'General', 'Default folder for all notes', '#2196F3', 'folder', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 1)
                """)
            }
        }
    }
}