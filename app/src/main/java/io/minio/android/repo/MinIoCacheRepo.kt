package io.minio.android.repo

import io.minio.android.util.cache.DataCache
import io.minio.messages.Bucket
import javax.inject.Inject

class MinIoCacheRepo @Inject constructor(val dataCache: DataCache) {


    fun queryListObject(bucket: Bucket, filePath: String = "") {

    }
}