package io.minio.android.usecase


import io.minio.*
import io.minio.android.BuildConfig
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.entities.FolderPage
import io.minio.android.util.formatFileSize
import io.minio.android.util.processFileName
import io.minio.messages.Bucket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class MinIoManagerUseCase @Inject constructor(private val minioClient: MinioClient) {

    suspend fun queryBucketList(): List<Bucket> = minioClient.listBuckets().toList()

    suspend fun queryFolderByPath(bucket: Bucket, prefix: String = ""): FolderPage {

        return withContext(Dispatchers.IO) {
            val args =
                ListObjectsArgs.builder().bucket(bucket.name()).prefix(prefix).recursive(false)
                    .build()
            val folderList = minioClient.listObjects(args).toList().map { result ->
                result.get().let {
                    val fileRealName = it.objectName()
                    val extension = fileRealName.substringAfterLast('.', "")
                    val type = if (it.isDir) {
                        val subFolder = minioClient.listObjects(
                            ListObjectsArgs.builder().bucket(bucket.name()).prefix(fileRealName)
                                .recursive(false).build()
                        ).toList()
                        FileType.Folder(fileRealName.processFileName(), subFolder.size)
                    } else {
                        when (extension.lowercase(Locale.ROOT)) {
                            "jpg", "jpeg", "png", "gif", "webp" -> {
                                val fileName = minioClient.statObject(
                                    StatObjectArgs.builder().bucket(bucket.name())
                                        .objectName(fileRealName)
                                        .build()
                                )
                                FileType.ImageFile(
                                    fileRealName.processFileName(),
                                    fileName.size().formatFileSize(),
                                    fileName.lastModified().toString()
                                )
                            }
                            else -> {
                                val fileName = minioClient.statObject(
                                    StatObjectArgs.builder().bucket(bucket.name())
                                        .objectName(fileRealName)
                                        .build()
                                )
                                FileType.TextFile(
                                    fileRealName.processFileName(),
                                    fileName.size().formatFileSize(),
                                    fileName.lastModified().toString(),
                                )
                            }
                        }
                    }
                    FolderItemData(
                        type,
                        fileRealName,
                        "${BuildConfig.ENDPOINT}/${bucket.name()}/$fileRealName"
                    )
                }
            }.sortedWith(compareBy { it.fileType }).sortedWith(
                    compareBy { it.fileType.name.toIntOrNull() })


            val title = prefix.ifEmpty {
                bucket.name()
            }.processFileName()
            FolderPage(title, folderList)
        }

    }
}