package com.win11launcher.data.dao

import androidx.room.*
import com.win11launcher.data.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes WHERE is_archived = 0 ORDER BY created_at DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_archived = 0 ORDER BY created_at DESC")
    fun getNotesByFolder(folderId: String): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE is_starred = 1 AND is_archived = 0 ORDER BY created_at DESC")
    fun getStarredNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE is_archived = 1 ORDER BY created_at DESC")
    fun getArchivedNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' AND is_archived = 0")
    fun getNotesByTag(tag: String): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE source_package = :packageName AND is_archived = 0 ORDER BY created_at DESC")
    fun getNotesBySourceApp(packageName: String): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE created_at >= :startTime AND created_at <= :endTime ORDER BY created_at DESC")
    fun getNotesInDateRange(startTime: Long, endTime: Long): Flow<List<Note>>
    
    @Query("SELECT COUNT(*) FROM notes WHERE folder_id = :folderId AND is_archived = 0")
    suspend fun getNotesCountInFolder(folderId: String): Int
    
    @Query("SELECT COUNT(*) FROM notes WHERE rule_id = :ruleId AND created_at >= :startTime")
    suspend fun getNotesCountByRuleSince(ruleId: String, startTime: Long): Int
    
    @Query("SELECT * FROM notes WHERE rule_id = :ruleId AND content LIKE '%' || :content || '%' AND created_at >= :sinceTime LIMIT 1")
    suspend fun findSimilarRecentNote(ruleId: String, content: String, sinceTime: Long): Note?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<Note>)
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Query("UPDATE notes SET is_starred = :isStarred WHERE id = :noteId")
    suspend fun updateNoteStarred(noteId: String, isStarred: Boolean)
    
    @Query("UPDATE notes SET is_archived = :isArchived WHERE id = :noteId")
    suspend fun updateNoteArchived(noteId: String, isArchived: Boolean)
    
    @Query("UPDATE notes SET folder_id = :folderId WHERE id = :noteId")
    suspend fun moveNoteToFolder(noteId: String, folderId: String)
    
    @Query("UPDATE notes SET tags = :tags WHERE id = :noteId")
    suspend fun updateNoteTags(noteId: String, tags: String)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)
    
    @Query("DELETE FROM notes WHERE folder_id = :folderId")
    suspend fun deleteNotesByFolder(folderId: String)
    
    @Query("DELETE FROM notes WHERE is_archived = 1 AND created_at < :cutoffTime")
    suspend fun deleteOldArchivedNotes(cutoffTime: Long)
}