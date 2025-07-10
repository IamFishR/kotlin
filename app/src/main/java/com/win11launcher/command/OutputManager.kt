package com.win11launcher.command

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import java.util.Base64

object OutputManager {
    
    private const val MAX_OUTPUT_SIZE = 10 * 1024 // 10KB
    private const val COMPRESSION_THRESHOLD = 1024 // 1KB
    
    data class ProcessedOutput(
        val preview: String,
        val fullOutput: String?,
        val compressed: Boolean,
        val truncated: Boolean
    )
    
    fun processCommandOutput(output: String): ProcessedOutput {
        // Create preview (first 200 chars)
        val preview = if (output.length > 200) {
            output.substring(0, 200) + "..."
        } else {
            output
        }
        
        // Handle large outputs
        return when {
            output.length <= 200 -> {
                // Small output - store inline
                ProcessedOutput(
                    preview = preview,
                    fullOutput = null,
                    compressed = false,
                    truncated = false
                )
            }
            
            output.length > MAX_OUTPUT_SIZE -> {
                // Too large - truncate
                val truncated = output.substring(0, MAX_OUTPUT_SIZE) + 
                    "\n\n... Output truncated (${output.length} total characters)"
                
                ProcessedOutput(
                    preview = preview,
                    fullOutput = if (truncated.length > COMPRESSION_THRESHOLD) {
                        compressString(truncated)
                    } else {
                        truncated
                    },
                    compressed = truncated.length > COMPRESSION_THRESHOLD,
                    truncated = true
                )
            }
            
            output.length > COMPRESSION_THRESHOLD -> {
                // Compress medium-large outputs
                ProcessedOutput(
                    preview = preview,
                    fullOutput = compressString(output),
                    compressed = true,
                    truncated = false
                )
            }
            
            else -> {
                // Store as-is
                ProcessedOutput(
                    preview = preview,
                    fullOutput = output,
                    compressed = false,
                    truncated = false
                )
            }
        }
    }
    
    private fun compressString(input: String): String {
        return try {
            val baos = ByteArrayOutputStream()
            GZIPOutputStream(baos).use { gzip ->
                gzip.write(input.toByteArray(Charsets.UTF_8))
            }
            Base64.getEncoder().encodeToString(baos.toByteArray())
        } catch (e: Exception) {
            input // Fallback to uncompressed if compression fails
        }
    }
    
    fun decompressString(compressed: String): String {
        return try {
            val bytes = Base64.getDecoder().decode(compressed)
            GZIPInputStream(bytes.inputStream()).use { gzip ->
                gzip.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            compressed // Fallback to treating as uncompressed
        }
    }
}