package io.minio.android.util.cache

import android.util.LruCache

class LruDataCache : DataCache {

    private val cacheSize = Runtime.getRuntime().maxMemory() / 32
    private val cache = LruCache<String, Any>(cacheSize.toInt())

    override fun <T> put(key: String, value: T?) {
        if (value == null) {
            cache.remove(key)
        } else {
            cache.put(key, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        return cache.get(key)?.let {
            it as T
        }
    }
}

