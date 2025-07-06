
package com.win11launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.win11launcher.services.AIService
import com.win11launcher.ui.theme.Win11Colors // Import Win11Colors
import com.win11launcher.viewmodels.ChatbotViewModel

@Composable
fun ChatbotScreen() {
    val context = LocalContext.current
    val aiService = remember { AIService(context) }
    val chatbotViewModel: ChatbotViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatbotViewModel(aiService) as T
            }
        }
    )

    val messages by chatbotViewModel.messages
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Win11Colors.SystemBackground) // Use Win11Colors
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val (sender, text) = message
                MessageBubble(sender = sender, text = text)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = Win11Colors.TextSecondary) }, // Styled placeholder
                shape = RoundedCornerShape(12.dp), // Adjusted shape
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Win11Colors.TextPrimary,
                    unfocusedTextColor = Win11Colors.TextPrimary,
                    cursorColor = Win11Colors.SystemAccent,
                    focusedBorderColor = Win11Colors.SystemAccent,
                    unfocusedBorderColor = Win11Colors.Outline,
                    disabledTextColor = Win11Colors.TextSecondary, // Added for completeness
                    disabledBorderColor = Win11Colors.OutlineVariant, // Added for completeness
                    errorCursorColor = Win11Colors.Error, // Added for completeness
                    errorBorderColor = Win11Colors.Error, // Added for completeness
                    focusedLabelColor = Win11Colors.SystemAccent, // Added for completeness
                    unfocusedLabelColor = Win11Colors.TextSecondary, // Added for completeness
                    disabledLabelColor = Win11Colors.TextTertiary, // Added for completeness
                    focusedPlaceholderColor = Win11Colors.TextSecondary, // Added for completeness
                    unfocusedPlaceholderColor = Win11Colors.TextSecondary, // Added for completeness
                    disabledPlaceholderColor = Win11Colors.TextTertiary // Added for completeness
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (inputText.isNotBlank()) {
                    chatbotViewModel.sendMessage(inputText)
                    inputText = ""
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Win11Colors.SystemAccent // Use Win11Colors
                )
            }
        }
    }
}

@Composable
fun MessageBubble(sender: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (sender == "user") Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp)) // Adjusted shape
                .background(
                    if (sender == "user") Win11Colors.SystemAccent else Win11Colors.SystemSurface // Use Win11Colors
                )
                .padding(12.dp)
        ) {
            Text(text = text, color = Win11Colors.TextPrimary) // Use Win11Colors for text
        }
    }
}
