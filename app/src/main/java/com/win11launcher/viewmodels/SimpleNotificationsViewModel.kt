package com.win11launcher.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.win11launcher.data.entities.Note
import com.win11launcher.data.repositories.NoteRepository
import javax.inject.Inject

@HiltViewModel
class SimpleNotificationsViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    // Get all notifications (Notes) that are not ignored/archived
    val allNotifications: Flow<List<Note>> = noteRepository.getAllNotes()
        .map { notes ->
            notes.filter { !it.isArchived }
                .sortedByDescending { it.createdAt }
        }
    
    fun ignoreNotification(notificationId: String) {
        viewModelScope.launch {
            // Archive the notification to "ignore" it
            noteRepository.updateNoteArchived(notificationId, true)
        }
    }
}