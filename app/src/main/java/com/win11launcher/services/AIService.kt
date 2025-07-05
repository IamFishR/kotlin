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
import com.google.mediapipe.tasks.genai.llminference.LlmInference

@Singleton
class AIService @Inject constructor(
    private val context: Context
) {
    private var llmInference: LlmInference? = null
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
                if (isModelLoaded && llmInference != null) {
                    return@withContext AIResponse(true, "Model already loaded")
                }
                
                if (isLoading) {
                    return@withContext AIResponse(false, "", "Model is already being loaded")
                }
                
                isLoading = true
                Log.d(TAG, "Starting MediaPipe LLM model initialization...")
                
                // Copy model from assets to internal storage if needed
                val modelFile = copyModelToInternalStorage()
                
                if (!modelFile.exists()) {
                    isLoading = false
                    return@withContext AIResponse(false, "", "Model file not found")
                }
                
                // Initialize MediaPipe LLM inference with the model file
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
                    .build()
                
                llmInference = LlmInference.createFromOptions(context, options)
                
                isModelLoaded = true
                isLoading = false
                
                Log.d(TAG, "MediaPipe LLM model initialized successfully")
                AIResponse(true, "Gemma 3 model loaded successfully via MediaPipe")
                
            } catch (e: Exception) {
                isLoading = false
                llmInference = null
                Log.e(TAG, "Failed to initialize MediaPipe LLM model", e)
                AIResponse(false, "", "Failed to load model: ${e.message}")
            }
        }
    }
    
    suspend fun generateResponse(prompt: String): AIResponse {
        return withContext(Dispatchers.IO) {
            try {
                if (!isModelLoaded || llmInference == null) {
                    val initResult = initializeModel()
                    if (!initResult.success) {
                        return@withContext initResult
                    }
                }
                
                Log.d(TAG, "Generating response for: $prompt")
                
                // Format the prompt for Gemma 3
                val formattedPrompt = formatPromptForGemma(prompt)
                
                // Generate response using MediaPipe LLM inference
                val response = llmInference!!.generateResponse(formattedPrompt)
                
                if (response != null && response.isNotEmpty()) {
                    Log.d(TAG, "Response generated successfully")
                    AIResponse(true, response.trim())
                } else {
                    Log.e(TAG, "Model returned null or empty response")
                    AIResponse(false, "", "Model failed to generate response")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate response", e)
                AIResponse(false, "", "Failed to generate response: ${e.message}")
            }
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
            Runtime: MediaPipe LLM Inference
            Status: ${getModelStatus()}
            Capabilities: Text generation, Q&A, conversation
        """.trimIndent()
    }
    
    fun unloadModel() {
        try {
            llmInference?.close()
            llmInference = null
            isModelLoaded = false
            isLoading = false
            Log.d(TAG, "MediaPipe LLM model unloaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error unloading MediaPipe LLM model", e)
        }
    }
}