package io.minio.android.usecase

import io.minio.MinioClient
import io.minio.UploadObjectArgs
import io.minio.messages.Bucket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MinIoUpLoadFileUseCase @Inject constructor(private val minioClient: MinioClient) {

    suspend fun upLoadFile(bucket: Bucket, filePath: String, fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val args = UploadObjectArgs
                .builder()
                .objectName(fileName)
                .bucket(bucket.name())
                .filename(filePath).build()
            val result = minioClient.uploadObject(args)
            result.etag().isEmpty()
        }
    }
}