package com.win11launcher.viewmodel

import androidx.lifecycle.ViewModel
import com.win11launcher.services.AIService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AIServiceViewModel @Inject constructor(
    val aiService: AIService
) : ViewModel() {
    
    override fun onCleared() {
        super.onCleared()
        // Clean up AI service when ViewModel is destroyed
        aiService.unloadModel()
    }
}