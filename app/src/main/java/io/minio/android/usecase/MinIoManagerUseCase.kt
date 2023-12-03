package io.minio.android.usecase

import io.minio.*
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.entities.FolderPage
import io.minio.android.util.formatFileSize
import io.minio.messages.Bucket
import java.util.*
import javax.inject.Inject

class MinIoManagerUseCase @Inject constructor(private val minioClient: MinioClient) {

    suspend fun queryBucketList(): List<Bucket> = minioClient.listBuckets().toList()

    suspend fun queryFolderByPath(bucket: Bucket, prefix: String = ""): FolderPage {
        val args =
            ListObjectsArgs.builder().bucket(bucket.name()).prefix(prefix).recursive(false).build()
        val folderList = minioClient.listObjects(args).toList().map { result ->
            result?.get()?.let {
                val fileRealName = it.objectName()
                val extension = fileRealName.substringAfterLast('.', "")
                val type = if (it.isDir) {
                    val subFolder = minioClient.listObjects(
                        ListObjectsArgs.builder().bucket(bucket.name()).prefix(fileRealName)
                            .recursive(false).build()
                    ).toList()

                    val fileName = if (fileRealName.endsWith("/")) {
                        fileRealName.removeSuffix("/")
                    } else {
                        fileRealName
                    }
                    FileType.Folder(fileName, subFolder.size)
                } else {
                    when (extension.lowercase(Locale.ROOT)) {
                        "jpg", "jpeg", "png", "gif", "webp" -> {
                            val fileName = minioClient.statObject(
                                StatObjectArgs.builder().bucket(bucket.name())
                                    .objectName(fileRealName)
                                    .build()
                            )
                            FileType.ImageFile(
                                fileRealName,
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
                                fileRealName,
                                fileName.size().formatFileSize(),
                                fileName.lastModified().toString(),
                            )
                        }
                    }
                }
                FolderItemData(type, fileRealName)
            }
        }
        val title = prefix.ifEmpty {
            bucket.name()
        }
        return FolderPage(title, folderList)
    }
}