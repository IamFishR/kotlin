package com.win11launcher.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileImageManager @Inject constructor(
    private val context: Context
) {
    
    private val _profileImages = mutableStateOf<List<ProfileImage>>(emptyList())
    val profileImages: State<List<ProfileImage>> = _profileImages
    
    private val profileImagesDir: File by lazy {
        File(context.filesDir, "profile_images").apply {
            if (!exists()) mkdirs()
        }
    }
    
    suspend fun saveProfileImage(
        uri: Uri,
        profileId: String = "default",
        maxWidth: Int = 512,
        maxHeight: Int = 512,
        quality: Int = 85
    ): Result<ProfileImage> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open image"))
            
            // Decode and process the image
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: return@withContext Result.failure(Exception("Failed to decode image"))
            
            inputStream.close()
            
            // Rotate image if needed based on EXIF data
            val rotatedBitmap = rotateImageIfRequired(originalBitmap, uri)
            
            // Resize image to fit within max dimensions
            val resizedBitmap = resizeBitmap(rotatedBitmap, maxWidth, maxHeight)
            
            // Generate unique filename
            val fileName = "profile_${profileId}_${UUID.randomUUID()}.jpg"
            val file = File(profileImagesDir, fileName)
            
            // Save compressed image
            FileOutputStream(file).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            
            // Clean up bitmaps
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            resizedBitmap.recycle()
            
            // Create ProfileImage object
            val profileImage = ProfileImage(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                fileName = fileName,
                filePath = file.absolutePath,
                fileSize = file.length(),
                width = resizedBitmap.width,
                height = resizedBitmap.height,
                createdAt = System.currentTimeMillis()
            )
            
            // Update the list
            _profileImages.value = _profileImages.value + profileImage
            
            Result.success(profileImage)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProfileImage(profileImage: ProfileImage): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val file = File(profileImage.filePath)
            val deleted = if (file.exists()) file.delete() else true
            
            if (deleted) {
                _profileImages.value = _profileImages.value.filter { it.id != profileImage.id }
            }
            
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadProfileImages(profileId: String = "default"): List<ProfileImage> = withContext(Dispatchers.IO) {
        try {
            val images = profileImagesDir.listFiles()
                ?.filter { it.name.startsWith("profile_${profileId}_") && it.name.endsWith(".jpg") }
                ?.map { file ->
                    // Get image dimensions
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    
                    ProfileImage(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        fileName = file.name,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        width = options.outWidth,
                        height = options.outHeight,
                        createdAt = file.lastModified()
                    )
                } ?: emptyList()
            
            _profileImages.value = images
            images
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getProfileImageBitmap(profileImage: ProfileImage): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (File(profileImage.filePath).exists()) {
                BitmapFactory.decodeFile(profileImage.filePath)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun cleanupOldImages(profileId: String = "default", keepLatest: Int = 5): Int = withContext(Dispatchers.IO) {
        try {
            val images = loadProfileImages(profileId)
            val sortedImages = images.sortedByDescending { it.createdAt }
            val imagesToDelete = sortedImages.drop(keepLatest)
            
            var deletedCount = 0
            imagesToDelete.forEach { image ->
                val result = deleteProfileImage(image)
                if (result.isSuccess && result.getOrDefault(false)) {
                    deletedCount++
                }
            }
            
            deletedCount
        } catch (e: Exception) {
            0
        }
    }
    
    private fun rotateImageIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()
            
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }
    
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape
            maxWidth to (maxWidth / aspectRatio).toInt()
        } else {
            // Portrait or square
            (maxHeight * aspectRatio).toInt() to maxHeight
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    fun getProfileImageFile(profileImage: ProfileImage): File {
        return File(profileImage.filePath)
    }
    
    fun getProfileImagesDirectory(): File = profileImagesDir
    
    suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.IO) {
        try {
            profileImagesDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

data class ProfileImage(
    val id: String,
    val profileId: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val width: Int,
    val height: Int,
    val createdAt: Long
)