package io.minio.android.usecase

import io.minio.android.repo.MInIoClientRepo
import io.minio.android.util.cache.DataCache
import io.minio.messages.Bucket
import javax.inject.Inject

class MinIoDeleteUseCase  @Inject constructor(
    private val mInIoClientRepo: MInIoClientRepo,
) {
    suspend fun deleteFile(bucket: Bucket, deleteList: List<String>) =
        mInIoClientRepo.deleteObject(bucket, deleteList)
}