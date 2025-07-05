package com.win11launcher.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIService @Inject constructor(
    private val context: Context
) {
    private var isModelLoaded = false
    private var isLoading = false
    private val modelFileName = "gemma3-1B-it-int4.tflite"
    
    companion object {
        private const val TAG = "AIService"
    }
    
    data class AIResponse(
        val success: Boolean,
        val response: String,
        val error: String? = null
    )
    
    suspend fun initializeModel(): AIResponse {
        return withContext(Dispatchers.IO) {
            try {
                if (isModelLoaded) {
                    return@withContext AIResponse(true, "Model already loaded")
                }
                
                if (isLoading) {
                    return@withContext AIResponse(false, "", "Model is already being loaded")
                }
                
                isLoading = true
                Log.d(TAG, "Starting model initialization...")
                
                // Copy model from assets to internal storage if needed
                val modelFile = copyModelToInternalStorage()
                
                if (!modelFile.exists()) {
                    isLoading = false
                    return@withContext AIResponse(false, "", "Model file not found")
                }
                
                // Simulate model loading for now
                delay(2000)
                
                isModelLoaded = true
                isLoading = false
                
                Log.d(TAG, "Model initialized successfully")
                AIResponse(true, "Gemma 3 model loaded successfully")
                
            } catch (e: Exception) {
                isLoading = false
                Log.e(TAG, "Failed to initialize model", e)
                AIResponse(false, "", "Failed to load model: ${e.message}")
            }
        }
    }
    
    suspend fun generateResponse(prompt: String): AIResponse {
        return withContext(Dispatchers.IO) {
            try {
                if (!isModelLoaded) {
                    val initResult = initializeModel()
                    if (!initResult.success) {
                        return@withContext initResult
                    }
                }
                
                Log.d(TAG, "Generating response for: $prompt")
                
                // Simulate AI response generation for now
                delay(1000)
                
                // Generate a mock response based on the prompt
                val mockResponse = generateMockResponse(prompt)
                
                Log.d(TAG, "Response generated successfully")
                AIResponse(true, mockResponse)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate response", e)
                AIResponse(false, "", "Failed to generate response: ${e.message}")
            }
        }
    }
    
    private fun generateMockResponse(prompt: String): String {
        // Generate contextual mock responses for testing
        return when {
            prompt.contains("hello", ignoreCase = true) -> 
                "Hello! I'm Gemma 3, your on-device AI assistant. How can I help you today?"
            prompt.contains("android", ignoreCase = true) -> 
                "Android is a mobile operating system developed by Google. It's based on the Linux kernel and designed primarily for touchscreen mobile devices."
            prompt.contains("kotlin", ignoreCase = true) -> 
                "Kotlin is a modern programming language developed by JetBrains. It's fully interoperable with Java and is Google's preferred language for Android development."
            prompt.contains("what", ignoreCase = true) -> 
                "That's an interesting question! I'm currently running in mock mode while we set up the full AI model integration."
            prompt.contains("how", ignoreCase = true) -> 
                "Great question! I'm here to help explain things. Currently, I'm operating in demonstration mode."
            else -> 
                "I understand you're asking about: \"$prompt\". I'm Gemma 3 running on-device in your launcher. This is a demonstration response while we finalize the model integration."
        }
    }
    
    private fun formatPromptForGemma(userPrompt: String): String {
        // Format prompt according to Gemma 3 instruction format
        return "<start_of_turn>user\n$userPrompt<end_of_turn>\n<start_of_turn>model\n"
    }
    
    private fun copyModelToInternalStorage(): File {
        val internalDir = File(context.filesDir, "ai_models")
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }
        
        val modelFile = File(internalDir, modelFileName)
        
        if (modelFile.exists()) {
            return modelFile
        }
        
        try {
            context.assets.open(modelFileName).use { inputStream ->
                FileOutputStream(modelFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            Log.d(TAG, "Model copied to internal storage: ${modelFile.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy model file", e)
        }
        
        return modelFile
    }
    
    fun getModelStatus(): String {
        return when {
            isLoading -> "Loading model..."
            isModelLoaded -> "Model loaded and ready"
            else -> "Model not loaded"
        }
    }
    
    fun getModelInfo(): String {
        return """
            Model: Gemma 3 (1B parameters)
            Format: TensorFlow Lite (INT4 quantized)
            Size: ~580MB
            Status: ${getModelStatus()}
            Capabilities: Text generation, Q&A, conversation
        """.trimIndent()
    }
    
    fun unloadModel() {
        try {
            isModelLoaded = false
            isLoading = false
            Log.d(TAG, "Model unloaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error unloading model", e)
        }
    }
}