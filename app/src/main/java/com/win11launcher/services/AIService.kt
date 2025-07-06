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
    private val modelFileName = "gemma-3n-E4B-it-int4.task"
    
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
                
                // Check and request storage permission
                if (!hasStoragePermission()) {
                    isLoading = false
                    return@withContext AIResponse(false, "", "Storage permission required to access model file. Please grant storage permission in app settings.")
                }
                
                // Copy model from assets to internal storage if needed
                val modelFile = copyModelToInternalStorage()
                
                if (!modelFile.exists()) {
                    isLoading = false
                    return@withContext AIResponse(false, "", "Model file not found at ${modelFile.absolutePath}")
                }
                
                if (!modelFile.canRead()) {
                    isLoading = false
                    return@withContext AIResponse(false, "", "Cannot read model file. Check file permissions and storage access.")
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
                AIResponse(true, "Gemma 3N model loaded successfully via MediaPipe")
                
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
                
                // Format the prompt for Gemma 3N
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
        // Format prompt according to Gemma 3N instruction format
        return "<start_of_turn>user\n$userPrompt<end_of_turn>\n<start_of_turn>model\n"
    }
    
    
    private fun copyModelToInternalStorage(): File {
        // Debug: Log all possible paths and check permissions
        debugModelPaths()
        
        // Try multiple possible paths for the model
        val possiblePaths = listOf(
            "/storage/emulated/0/Models/$modelFileName",
            "/sdcard/Models/$modelFileName",
            "/storage/self/primary/Models/$modelFileName",
            "/mnt/sdcard/Models/$modelFileName",
            "${android.os.Environment.getExternalStorageDirectory()}/Models/$modelFileName"
        )
        
        for (path in possiblePaths) {
            val modelFile = File(path)
            Log.d(TAG, "Checking path: $path")
            Log.d(TAG, "  - Exists: ${modelFile.exists()}")
            Log.d(TAG, "  - Can read: ${modelFile.canRead()}")
            Log.d(TAG, "  - Size: ${if (modelFile.exists()) modelFile.length() else "N/A"}")
            Log.d(TAG, "  - Parent exists: ${modelFile.parentFile?.exists()}")
            
            if (modelFile.exists() && modelFile.canRead()) {
                Log.d(TAG, "Found readable model at: $path")
                return modelFile
            }
        }
        
        // Fallback: check if already copied to app internal storage
        val internalDir = File(context.filesDir, "ai_models")
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }
        
        val modelFile = File(internalDir, modelFileName)
        
        if (modelFile.exists()) {
            Log.d(TAG, "Found model in app internal storage: ${modelFile.absolutePath}")
            return modelFile
        }
        
        // Last resort: try to copy from assets (will fail for large files)
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
            Log.d(TAG, "Model copied from assets to internal storage: ${modelFile.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy model file from assets", e)
        }
        
        return modelFile
    }
    
    private fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ - check for MANAGE_EXTERNAL_STORAGE
            android.os.Environment.isExternalStorageManager() ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            // Android 10 and below
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun debugModelPaths() {
        Log.d(TAG, "=== DEBUG MODEL PATHS ===")
        Log.d(TAG, "Model filename: $modelFileName")
        Log.d(TAG, "External storage directory: ${android.os.Environment.getExternalStorageDirectory()}")
        Log.d(TAG, "External storage state: ${android.os.Environment.getExternalStorageState()}")
        Log.d(TAG, "App files dir: ${context.filesDir}")
        Log.d(TAG, "App external files dir: ${context.getExternalFilesDir(null)}")
        
        // Check storage permissions
        val hasReadPermission = android.content.pm.PackageManager.PERMISSION_GRANTED == 
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        Log.d(TAG, "Has READ_EXTERNAL_STORAGE permission: $hasReadPermission")
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val hasMediaPermission = android.content.pm.PackageManager.PERMISSION_GRANTED == 
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES)
            Log.d(TAG, "Has READ_MEDIA_IMAGES permission: $hasMediaPermission")
        }
        
        // List contents of possible directories
        val possibleDirs = listOf(
            "/storage/emulated/0/Models",
            "/sdcard/Models",
            "/storage/self/primary/Models",
            "${android.os.Environment.getExternalStorageDirectory()}/Models"
        )
        
        for (dirPath in possibleDirs) {
            val dir = File(dirPath)
            Log.d(TAG, "Directory: $dirPath")
            Log.d(TAG, "  - Exists: ${dir.exists()}")
            Log.d(TAG, "  - Can read: ${dir.canRead()}")
            Log.d(TAG, "  - Is directory: ${dir.isDirectory}")
            
            if (dir.exists() && dir.canRead() && dir.isDirectory) {
                try {
                    val files = dir.listFiles()
                    Log.d(TAG, "  - Files count: ${files?.size ?: 0}")
                    files?.forEach { file ->
                        Log.d(TAG, "    - ${file.name} (${file.length()} bytes)")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "  - Error listing files: ${e.message}")
                }
            }
        }
        Log.d(TAG, "=== END DEBUG ===")
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
            Model: Gemma 3N (E4B parameters with selective activation)
            Format: MediaPipe Task (INT4 quantized)
            Size: 4.41GB
            Runtime: MediaPipe LLM Inference
            Status: ${getModelStatus()}
            Capabilities: Multimodal (text, image, audio, video), Text generation, Q&A, conversation
            Context: 32K tokens input/output
            Languages: 140+ supported languages
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