package com.win11launcher.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.win11launcher.services.AIService
import com.win11launcher.services.AIMemoryManager
import com.win11launcher.data.repositories.UserProfileRepository
import com.win11launcher.data.entities.UserProfile
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
    private val aiMemoryManager: AIMemoryManager,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // Get current user profile data for AI context
    private suspend fun getCurrentUser(): UserProfile? {
        return userProfileRepository.getUserProfileSync("default")
    }
    
    private suspend fun getCurrentUserId(): String {
        return getCurrentUser()?.id ?: "default"
    }
    
    // Build AI prompt with user context for personalized responses
    private fun buildUserAwarePrompt(userMessage: String, userProfile: UserProfile?, conversationContext: String): String {
        return buildString {
            // Add user context for personalization
            if (userProfile != null) {
                appendLine("User Profile Information:")
                appendLine("- Name: ${userProfile.username}")
                if (userProfile.displayName.isNotEmpty()) {
                    appendLine("- Display Name: ${userProfile.displayName}")
                }
                if (userProfile.bio.isNotEmpty()) {
                    appendLine("- Bio: ${userProfile.bio}")
                }
                if (userProfile.email.isNotEmpty()) {
                    appendLine("- Email: ${userProfile.email}")
                }
                appendLine("- User's Theme Color: ${userProfile.themeColor}")
                appendLine()
                appendLine("Please address the user by their name (${userProfile.username}) and provide personalized responses based on their profile.")
                appendLine()
            }
            
            // Add conversation context if available
            if (conversationContext.isNotEmpty()) {
                appendLine("Previous Conversation Context:")
                appendLine(conversationContext)
                appendLine()
            }
            
            // Add current user message
            appendLine("Current User Message:")
            appendLine(userMessage)
        }
    }
    
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
                
                // Get user profile for personalized context
                val userProfile = getCurrentUser()
                
                // Build context for AI with user information
                val conversationContext = aiMemoryManager.getConversationContext(
                    conversationId = _uiState.value.conversationId,
                    maxMessages = 20
                )
                
                // Build personalized prompt with user context
                val personalizedPrompt = buildUserAwarePrompt(currentMessage, userProfile, conversationContext)
                
                // Get AI response with user context
                println("📞 Calling AI service with personalized context for user: ${userProfile?.username}")
                val aiResponse = aiService.generateResponse(personalizedPrompt)
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
                val userId = getCurrentUserId()
                aiMemoryManager.performMemoryMaintenance(userId)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}