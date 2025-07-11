package com.win11launcher.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.win11launcher.services.AIService
import com.win11launcher.services.AIMemoryManager
import com.win11launcher.data.database.NotesDatabase

@Composable
fun TestChatWindowContent() {
    val context = LocalContext.current
    
    // Manually create dependencies for testing
    val (aiService, aiMemoryManager) = remember {
        val database = NotesDatabase.getDatabase(context)
        val aiService = AIService(context)
        val aiMemoryManager = AIMemoryManager(database.aiMemoryDao(), aiService)
        Pair(aiService, aiMemoryManager)
    }
    
    // Create a simple test ViewModel manually
    val testViewModel = remember {
        object {
            private val _message = mutableStateOf("")
            val message: State<String> = _message
            private val _response = mutableStateOf("")
            val response: State<String> = _response
            private val _isLoading = mutableStateOf(false)
            val isLoading: State<Boolean> = _isLoading
            
            fun updateMessage(newMessage: String) {
                _message.value = newMessage
            }
            
            suspend fun sendMessage() {
                if (_message.value.isBlank() || _isLoading.value) return
                
                _isLoading.value = true
                try {
                    println("🧪 Test: Sending message: ${_message.value}")
                    val result = aiService.generateResponse(_message.value)
                    println("🧪 Test: AI Response: success=${result.success}, error=${result.error}")
                    
                    if (result.success) {
                        _response.value = result.response
                    } else {
                        _response.value = "Error: ${result.error}"
                    }
                } catch (e: Exception) {
                    println("🧪 Test: Exception: ${e.message}")
                    e.printStackTrace()
                    _response.value = "Exception: ${e.message}"
                } finally {
                    _isLoading.value = false
                    _message.value = ""
                }
            }
        }
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "🧪 Test AI Chat",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            // Response area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (testViewModel.isLoading.value) {
                        CircularProgressIndicator()
                    } else if (testViewModel.response.value.isNotEmpty()) {
                        Text(
                            text = testViewModel.response.value,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Type a message and click Send to test AI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Input area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = testViewModel.message.value,
                    onValueChange = testViewModel::updateMessage,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a test message...") },
                    enabled = !testViewModel.isLoading.value
                )
                
                Button(
                    onClick = {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            testViewModel.sendMessage()
                        }
                    },
                    enabled = !testViewModel.isLoading.value && testViewModel.message.value.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    }
}