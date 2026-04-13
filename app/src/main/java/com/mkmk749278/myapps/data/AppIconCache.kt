package com.mkmk749278.myapps.data

import android.content.Context
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

object AppIconCache {
    private val cache = object : LruCache<String, ImageBitmap>(160) {}

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
