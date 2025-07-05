package com.win11launcher.data.repositories

import com.win11launcher.data.dao.NoteDao
import com.win11launcher.data.entities.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getUnsavedNotifications(): Flow<List<Note>> = noteDao.getUnsavedNotifications()
    
    fun getNotesByFolder(folderId: String): Flow<List<Note>> = noteDao.getNotesByFolder(folderId)
    
    fun getStarredNotes(): Flow<List<Note>> = noteDao.getStarredNotes()
    
    fun getArchivedNotes(): Flow<List<Note>> = noteDao.getArchivedNotes()
    
    suspend fun getNoteById(noteId: String): Note? = noteDao.getNoteById(noteId)
    
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    
    fun getNotesByTag(tag: String): Flow<List<Note>> = noteDao.getNotesByTag(tag)
    
    fun getNotesBySourceApp(packageName: String): Flow<List<Note>> = noteDao.getNotesBySourceApp(packageName)
    
    fun getNotesInDateRange(startTime: Long, endTime: Long): Flow<List<Note>> = 
        noteDao.getNotesInDateRange(startTime, endTime)
    
    suspend fun getNotesCountInFolder(folderId: String): Int = noteDao.getNotesCountInFolder(folderId)
    
    suspend fun getNotesCountByRuleSince(ruleId: String, startTime: Long): Int = 
        noteDao.getNotesCountByRuleSince(ruleId, startTime)
    
    suspend fun findExactRecentNote(ruleId: String, title: String, content: String, sinceTime: Long): Note? = 
        noteDao.findExactRecentNote(ruleId, title, content, sinceTime)
    
    suspend fun findRecentNotesByRule(ruleId: String, sinceTime: Long): List<Note> = 
        noteDao.findRecentNotesByRule(ruleId, sinceTime)
    
    suspend fun findAllRecentNotes(sinceTime: Long): List<Note> = 
        noteDao.findAllRecentNotes(sinceTime)
    
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    
    suspend fun insertNotes(notes: List<Note>) = noteDao.insertNotes(notes)
    
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    suspend fun updateNoteStarred(noteId: String, isStarred: Boolean) = 
        noteDao.updateNoteStarred(noteId, isStarred)
    
    suspend fun updateNoteArchived(noteId: String, isArchived: Boolean) = 
        noteDao.updateNoteArchived(noteId, isArchived)
    
    suspend fun moveNoteToFolder(noteId: String, folderId: String) = 
        noteDao.moveNoteToFolder(noteId, folderId)
    
    suspend fun updateNoteTags(noteId: String, tags: String) = 
        noteDao.updateNoteTags(noteId, tags)
    
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    
    suspend fun deleteNoteById(noteId: String) = noteDao.deleteNoteById(noteId)
    
    suspend fun deleteNotesByFolder(folderId: String) = noteDao.deleteNotesByFolder(folderId)
    
    suspend fun deleteOldArchivedNotes(cutoffTime: Long) = noteDao.deleteOldArchivedNotes(cutoffTime)
    
    suspend fun findDuplicateNotes(packageName: String? = null): List<Note> {
        val recentNotes = findAllRecentNotes(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
        val filteredNotes = if (packageName != null) {
            recentNotes.filter { it.sourcePackage == packageName }
        } else {
            recentNotes
        }
        
        return findDuplicatesInList(filteredNotes)
    }
    
    private fun findDuplicatesInList(notes: List<Note>): List<Note> {
        val duplicates = mutableListOf<Note>()
        val seen = mutableSetOf<String>()
        
        for (note in notes.sortedBy { it.createdAt }) {
            val key = "${note.sourcePackage}:${note.title.trim()}:${note.content.trim()}"
            if (seen.contains(key)) {
                duplicates.add(note)
            } else {
                seen.add(key)
            }
        }
        
        return duplicates
    }
    
    suspend fun removeDuplicates(packageName: String? = null): Int {
        val duplicates = findDuplicateNotes(packageName)
        duplicates.forEach { deleteNote(it) }
        return duplicates.size
    }
}