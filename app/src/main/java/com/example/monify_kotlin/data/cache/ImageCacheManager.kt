package com.example.monify_kotlin.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream

class ImageCacheManager(context: Context) {
    private val cacheDir = File(context.cacheDir, "receipt_images")

    // LRU Cache for in-memory bitmap caching
    private val memoryCache: LruCache<String, Bitmap>

    init {
        cacheDir.mkdirs()

        // Get max available VM memory, use 1/8 for cache
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    /**
     * Save image to disk and return local path
     */
    fun saveImageLocally(context: Context, imageUri: Uri): String {
        val fileName = "receipt_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, fileName)

        context.contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        // Also cache in memory
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        memoryCache.put(file.absolutePath, bitmap)

        return file.absolutePath
    }

    /**
     * Get bitmap from cache (memory or disk)
     */
    fun getBitmap(path: String): Bitmap? {
        // Try memory cache first
        memoryCache.get(path)?.let { return it }

        // Load from disk
        val bitmap = BitmapFactory.decodeFile(path)
        bitmap?.let { memoryCache.put(path, it) }
        return bitmap
    }

    /**
     * Delete local image file
     */
    fun deleteImage(path: String) {
        memoryCache.remove(path)
        File(path).delete()
    }
}