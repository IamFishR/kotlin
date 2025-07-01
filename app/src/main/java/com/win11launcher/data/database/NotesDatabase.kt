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
import com.win11launcher.data.dao.NoteDao
import com.win11launcher.data.dao.FolderDao
import com.win11launcher.data.dao.TrackingRuleDao
import com.win11launcher.data.dao.RuleActivityDao

@Database(
    entities = [Note::class, Folder::class, TrackingRule::class, RuleActivity::class],
    version = 1,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun trackingRuleDao(): TrackingRuleDao
    abstract fun ruleActivityDao(): RuleActivityDao
    
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
                .build()
                INSTANCE = instance
                instance
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