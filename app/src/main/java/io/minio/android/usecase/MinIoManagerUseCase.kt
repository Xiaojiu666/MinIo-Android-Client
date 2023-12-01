package io.minio.android.usecase

import io.minio.ListObjectsArgs
import io.minio.MinioClient
import io.minio.Result
import io.minio.StatObjectArgs
import io.minio.StatObjectResponse
import io.minio.messages.Bucket
import io.minio.messages.Item
import javax.inject.Inject

class MinIoManagerUseCase @Inject constructor(private val minioClient: MinioClient) {

    fun queryBucketList(): List<Bucket> = minioClient.listBuckets().toList()

    fun queryFolderByPath(bucket: Bucket, path: String): List<Result<Item>> {
        val args =
            ListObjectsArgs.builder().bucket(bucket.name()).prefix(path).recursive(false).build()
        return minioClient.listObjects(args).toList()
    }
}