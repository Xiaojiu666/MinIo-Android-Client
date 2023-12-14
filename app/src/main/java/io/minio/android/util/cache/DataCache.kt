package io.minio.android.util.cache

interface DataCache {

    fun <T> put(key: String, value: T?)

    fun <T> get(key: String): T?
}
