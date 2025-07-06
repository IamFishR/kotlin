package com.win11launcher.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.win11launcher.data.entities.NotificationEntity
import com.win11launcher.data.repositories.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllNotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _selectedFilter = MutableStateFlow("ALL")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    private val allNotifications = notificationRepository.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val filteredNotifications = combine(
        allNotifications,
        _selectedFilter
    ) { notifications, filter ->
        when (filter) {
            "AI_PROCESSED" -> notifications.filter { it.isAiProcessed }
            "NOTES_CREATED" -> notifications.filter { it.notesCreated }
            "USER_INTEREST" -> notifications.filter { it.userShowedInterest }
            else -> notifications.filter { 
                // Only show AI-processed notifications if user showed interest
                !it.isAiProcessed || it.userShowedInterest
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private val stats = allNotifications.map { notifications ->
        NotificationStats(
            total = notifications.size,
            aiProcessed = notifications.count { it.isAiProcessed },
            notesCreated = notifications.count { it.notesCreated },
            userInterest = notifications.count { it.userShowedInterest }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationStats(0, 0, 0, 0)
    )
    
    val uiState = combine(
        filteredNotifications,
        _selectedFilter,
        _isLoading,
        _error,
        stats
    ) { notifications, filter, loading, error, statsData ->
        AllNotificationsUiState(
            notifications = notifications,
            selectedFilter = filter,
            isLoading = loading,
            error = error,
            stats = statsData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AllNotificationsUiState()
    )
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
    
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Refresh is handled automatically by the Flow
                // This could trigger a manual refresh if needed
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun markUserInterest(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markUserInteraction(
                    id = notificationId,
                    showedInterest = true,
                    interactionType = "starred",
                    interactionAt = System.currentTimeMillis(),
                    rating = null,
                    notes = null
                )
            } catch (e: Exception) {
                _error.value = "Failed to mark interest: ${e.message}"
            }
        }
    }
    
    fun rateNotification(notificationId: String, rating: Int) {
        viewModelScope.launch {
            try {
                notificationRepository.markUserInteraction(
                    id = notificationId,
                    showedInterest = true,
                    interactionType = "rated",
                    interactionAt = System.currentTimeMillis(),
                    rating = rating,
                    notes = null
                )
            } catch (e: Exception) {
                _error.value = "Failed to rate notification: ${e.message}"
            }
        }
    }
    
    fun archiveNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.archiveNotification(
                    id = notificationId,
                    isArchived = true,
                    archivedAt = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _error.value = "Failed to archive notification: ${e.message}"
            }
        }
    }
    
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.softDeleteNotification(notificationId)
            } catch (e: Exception) {
                _error.value = "Failed to delete notification: ${e.message}"
            }
        }
    }
    
    data class AllNotificationsUiState(
        val notifications: List<NotificationEntity> = emptyList(),
        val selectedFilter: String = "ALL",
        val isLoading: Boolean = false,
        val error: String? = null,
        val stats: NotificationStats = NotificationStats(0, 0, 0, 0)
    )
    
    data class NotificationStats(
        val total: Int,
        val aiProcessed: Int,
        val notesCreated: Int,
        val userInterest: Int
    )
}