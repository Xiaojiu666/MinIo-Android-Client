package io.minio.android.repo

import io.minio.DeleteObjectsResponse
import io.minio.ListObjectsArgs
import io.minio.MinioClient
import io.minio.RemoveObjectsArgs
import io.minio.messages.Bucket
import io.minio.messages.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import io.minio.Result
import io.minio.StatObjectArgs
import io.minio.StatObjectResponse
import io.minio.messages.DeleteObject
import java.util.stream.Collectors.toList

class MInIoClientRepo @Inject constructor(private val minioClient: MinioClient) {


    suspend fun queryListBucket(): List<Bucket> {
        return withContext(Dispatchers.IO) {
            minioClient.listBuckets().toList()
        }
    }

    suspend fun queryListObject(bucket: Bucket, filePath: String = ""): List<Item> {
        return withContext(Dispatchers.IO) {
            minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucket.name()).prefix(filePath).recursive(false)
                    .build()
            ).mapNotNull {
                it?.get()
            }
        }
    }

    suspend fun queryObjectState(bucket: Bucket, objectName: String): StatObjectResponse {
        return withContext(Dispatchers.IO) {
            minioClient.statObject(
                StatObjectArgs.builder().bucket(bucket.name())
                    .objectName(objectName)
                    .build()
            )
        }
    }

    suspend fun deleteObject(bucket: Bucket) {
        val args = RemoveObjectsArgs.builder().bucket(bucket.name()).objects(
            listOf(
                DeleteObject("1713409479754.jpg")
            )
        ).build()
        minioClient.removeObjects(args)
    }
}