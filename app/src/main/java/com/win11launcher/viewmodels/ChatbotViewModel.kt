
package com.win11launcher.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.win11launcher.services.AIService
import kotlinx.coroutines.launch

class ChatbotViewModel(private val aiService: AIService) : ViewModel() {
    val messages = mutableStateOf<List<Pair<String, String>>>(emptyList())

    fun sendMessage(text: String) {
        messages.value = messages.value + ("user" to text)
        viewModelScope.launch {
            val response = aiService.generateResponse(text)
            if (response.success) {
                messages.value = messages.value + ("ai" to response.response)
            }
        }
    }
}
