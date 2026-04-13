package com.mkmk749278.myapps.data

import android.content.Context
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

object AppIconCache {
    private const val ICON_CACHE_CAPACITY = 160

    private val cache = object : LruCache<String, ImageBitmap>(ICON_CACHE_CAPACITY) {}

    fun load(context: Context, packageName: String): ImageBitmap? {
        cache.get(packageName)?.let { return it }
        val bitmap = runCatching {
            context.packageManager
                .getApplicationIcon(packageName)
                .toBitmap(144, 144)
                .asImageBitmap()
        }.getOrNull() ?: return null
        cache.put(packageName, bitmap)
        return bitmap
    }
}
