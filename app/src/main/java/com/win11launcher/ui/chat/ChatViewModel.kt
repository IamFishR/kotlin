package com.win11launcher.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.win11launcher.services.AIService
import com.win11launcher.services.AIMemoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChatUiMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

data class ChatUiState(
    val messages: List<ChatUiMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val error: String? = null,
    val conversationId: String = UUID.randomUUID().toString()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiService: AIService,
    private val aiMemoryManager: AIMemoryManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val userId = "default_user" // In a real app, this would come from user session
    
    init {
        println("🎯 ChatViewModel initialized")
        println("🤖 AIService: $aiService")
        println("🧠 AIMemoryManager: $aiMemoryManager")
        loadConversationHistory()
    }
    
    fun updateMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }
    
    fun sendMessage() {
        val currentMessage = _uiState.value.currentMessage.trim()
        if (currentMessage.isEmpty() || _uiState.value.isLoading) return
        
        println("🚀 Sending message: $currentMessage")
        println("🤖 AIService: $aiService")
        println("🧠 AIMemoryManager: $aiMemoryManager")
        
        viewModelScope.launch {
            try {
                // Add user message to UI
                val userMessage = ChatUiMessage(
                    content = currentMessage,
                    isUser = true
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + userMessage,
                    currentMessage = "",
                    isLoading = true,
                    isTyping = true,
                    error = null
                )
                
                // Store user message in memory
                aiMemoryManager.saveToShortTermMemory(
                    conversationId = _uiState.value.conversationId,
                    sender = "user",
                    content = currentMessage,
                    messageType = "CHAT"
                )
                
                // Build context for AI
                val conversationContext = aiMemoryManager.getConversationContext(
                    conversationId = _uiState.value.conversationId,
                    maxMessages = 20
                )
                
                // Get AI response
                println("📞 Calling AI service with context: $conversationContext")
                val aiResponse = aiService.generateResponse(conversationContext)
                println("📦 AI Response: success=${aiResponse.success}, error=${aiResponse.error}")
                
                _uiState.value = _uiState.value.copy(isTyping = false)
                
                if (aiResponse.success) {
                    // Add AI response to UI
                    val aiMessage = ChatUiMessage(
                        content = aiResponse.response,
                        isUser = false
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false
                    )
                    
                    // Store AI response in memory
                    aiMemoryManager.saveToShortTermMemory(
                        conversationId = _uiState.value.conversationId,
                        sender = "ai",
                        content = aiResponse.response,
                        messageType = "CHAT"
                    )
                    
                } else {
                    // Handle AI error
                    val errorMessage = ChatUiMessage(
                        content = "Sorry, I'm having trouble responding right now. ${aiResponse.error ?: "Please try again."}",
                        isUser = false
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorMessage,
                        isLoading = false,
                        error = aiResponse.error
                    )
                }
                
            } catch (e: Exception) {
                println("❌ ChatViewModel error: ${e.message}")
                e.printStackTrace()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isTyping = false,
                    error = e.message
                )
                
                // Add error message to chat
                val errorMessage = ChatUiMessage(
                    content = "Sorry, something went wrong: ${e.message}. Please try again.",
                    isUser = false
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage
                )
            }
        }
    }
    
    private fun loadConversationHistory() {
        viewModelScope.launch {
            try {
                // Load conversation from memory flow
                aiMemoryManager.getShortTermMemoryFlow(_uiState.value.conversationId)
                    .collect { memories ->
                        val chatMessages = memories.map { memory ->
                            ChatUiMessage(
                                id = memory.messageId,
                                content = memory.contentText,
                                isUser = memory.sender == "user",
                                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .format(Date(memory.timestamp))
                            )
                        }
                        
                        _uiState.value = _uiState.value.copy(messages = chatMessages)
                    }
                
            } catch (e: Exception) {
                // If loading history fails, just start with empty chat
                _uiState.value = _uiState.value.copy(
                    error = "Could not load conversation history"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun startNewConversation() {
        _uiState.value = ChatUiState(
            conversationId = UUID.randomUUID().toString()
        )
    }
    
    fun loadConversation(conversationId: String) {
        _uiState.value = _uiState.value.copy(
            conversationId = conversationId,
            messages = emptyList(),
            currentMessage = "",
            isLoading = false,
            isTyping = false,
            error = null
        )
        loadConversationHistory()
    }
    
    override fun onCleared() {
        super.onCleared()
        // Perform any cleanup if needed
        viewModelScope.launch {
            try {
                // Trigger memory maintenance when chat window is closed
                aiMemoryManager.performMemoryMaintenance(userId)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}