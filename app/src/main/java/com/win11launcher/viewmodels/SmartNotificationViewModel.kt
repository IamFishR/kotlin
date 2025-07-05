package com.win11launcher.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.win11launcher.data.entities.Note
import com.win11launcher.data.entities.TrackingRule
import com.win11launcher.data.repositories.NoteRepository
import com.win11launcher.data.repositories.TrackingRuleRepository
import com.win11launcher.ui.screens.AppNotificationGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SmartNotificationViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val trackingRuleRepository: TrackingRuleRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SmartNotificationUiState())
    val uiState = _uiState.asStateFlow()
    
    private val _allNotes = MutableStateFlow<List<Note>>(emptyList())
    
    val groupedNotes: StateFlow<List<AppNotificationGroup>> = _allNotes
        .map { notes ->
            notes.groupBy { it.sourcePackage }
                .map { (packageName, noteList) ->
                    AppNotificationGroup(
                        packageName = packageName,
                        appName = noteList.firstOrNull()?.sourceAppName ?: packageName,
                        notes = noteList.sortedByDescending { it.createdAt }
                    )
                }
                .sortedByDescending { it.notes.size }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            noteRepository.getAllNotes().collect { notes ->
                _allNotes.value = notes
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalNotes = notes.size
                )
            }
        }
    }
    
    fun refreshNotes() {
        loadNotes()
    }
    
    fun selectNotification(note: Note) {
        _uiState.value = _uiState.value.copy(selectedNotification = note)
    }
    
    fun dismissNotificationDetail() {
        _uiState.value = _uiState.value.copy(selectedNotification = null)
    }
    
    fun removeDuplicates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val duplicates = findDuplicateNotes(_allNotes.value)
            duplicates.forEach { note ->
                noteRepository.deleteNote(note)
            }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = "Removed ${duplicates.size} duplicate notifications"
            )
        }
    }
    
    fun removeDuplicatesForApp(packageName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val appNotes = _allNotes.value.filter { it.sourcePackage == packageName }
            val duplicates = findDuplicateNotes(appNotes)
            
            duplicates.forEach { note ->
                noteRepository.deleteNote(note)
            }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = "Removed ${duplicates.size} duplicate notifications for this app"
            )
        }
    }
    
    fun createRuleForApp(packageName: String, appName: String) {
        viewModelScope.launch {
            trackingRuleRepository.createDefaultRuleForApp(packageName, appName)
            
            _uiState.value = _uiState.value.copy(
                message = "Created tracking rule for $appName"
            )
        }
    }
    
    fun createRuleForNotification(note: Note) {
        viewModelScope.launch {
            val keywords = extractKeywords(note.title, note.content)
            
            trackingRuleRepository.createKeywordRuleForNotification(
                note.sourcePackage,
                note.sourceAppName,
                note.title,
                note.content,
                keywords
            )
            
            _uiState.value = _uiState.value.copy(
                message = "Created tracking rule based on this notification"
            )
        }
    }
    
    fun deleteNotification(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
            _uiState.value = _uiState.value.copy(
                message = "Notification deleted"
            )
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    private fun findDuplicateNotes(notes: List<Note>): List<Note> {
        val duplicates = mutableListOf<Note>()
        val processed = mutableSetOf<String>()
        
        for (note in notes) {
            val key = "${note.sourcePackage}:${note.title}:${note.content}"
            if (processed.contains(key)) {
                duplicates.add(note)
            } else {
                processed.add(key)
            }
        }
        
        // Also check for fuzzy duplicates
        val fuzzyDuplicates = findFuzzyDuplicates(notes)
        duplicates.addAll(fuzzyDuplicates)
        
        return duplicates.distinctBy { it.id }
    }
    
    private fun findFuzzyDuplicates(notes: List<Note>): List<Note> {
        val duplicates = mutableListOf<Note>()
        val threshold = 0.85 // 85% similarity
        
        for (i in notes.indices) {
            for (j in i + 1 until notes.size) {
                val note1 = notes[i]
                val note2 = notes[j]
                
                if (note1.sourcePackage == note2.sourcePackage) {
                    val titleSimilarity = calculateSimilarity(note1.title, note2.title)
                    val contentSimilarity = calculateSimilarity(note1.content, note2.content)
                    
                    if (titleSimilarity > threshold && contentSimilarity > threshold) {
                        // Keep the newer one, mark older as duplicate
                        if (note1.createdAt > note2.createdAt) {
                            duplicates.add(note2)
                        } else {
                            duplicates.add(note1)
                        }
                    }
                }
            }
        }
        
        return duplicates
    }
    
    private fun calculateSimilarity(str1: String, str2: String): Double {
        val longer = if (str1.length > str2.length) str1 else str2
        val shorter = if (str1.length > str2.length) str2 else str1
        
        if (longer.isEmpty()) return 1.0
        
        val editDistance = calculateLevenshteinDistance(longer, shorter)
        return (longer.length - editDistance) / longer.length.toDouble()
    }
    
    private fun calculateLevenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) {
            dp[i][0] = i
        }
        
        for (j in 0..str2.length) {
            dp[0][j] = j
        }
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    private fun extractKeywords(title: String, content: String): List<String> {
        val text = "$title $content"
        val words = text.split(Regex("\\W+"))
            .filter { it.length > 3 }
            .map { it.lowercase() }
            .filter { word ->
                // Filter out common words
                !listOf("the", "and", "for", "are", "but", "not", "you", "all", "any", "can", "had", "her", "was", "one", "our", "out", "day", "get", "has", "him", "how", "its", "may", "new", "now", "old", "see", "two", "way", "who", "boy", "did", "use", "her", "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did", "use")
                    .contains(word)
            }
        
        return words.distinct().take(5)
    }
}

data class SmartNotificationUiState(
    val isLoading: Boolean = false,
    val totalNotes: Int = 0,
    val selectedNotification: Note? = null,
    val message: String? = null
)