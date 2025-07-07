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
import com.win11launcher.data.dao.AppSettingDao
import com.win11launcher.data.dao.UserProfileDao
import com.win11launcher.data.converters.Converters

@Database(
    entities = [AppSetting::class, PermissionState::class, UserProfile::class, UserCustomization::class, UserFile::class],
    version = 12,
    exportSchema = true
)
@androidx.room.TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    
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
.fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
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
                    INSERT INTO user_profiles (id, username, displayName, createdAt, updatedAt, isDefault)
                    VALUES ('default', 'User', '', $currentTime, $currentTime, 1)
                """)
                
                // Insert default user customization
                db.execSQL("""
                    INSERT INTO user_customizations (profileId, createdAt, updatedAt)
                    VALUES ('default', $currentTime, $currentTime)
                """)
                
                // Insert default permission states
                db.execSQL("""
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
    }
}