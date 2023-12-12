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
                    println("fileRealName $fileRealName")
                    FolderItemData(
                        type,
                        fileRealName.processFileName(),
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

    suspend fun queryFoldersByPath(bucket: Bucket, filePath: String = ""): List<FolderItemData> {
        println("queryFoldersByPath bucket : p${bucket.name()} filePath : ${filePath}")
        return withContext(Dispatchers.IO) {
            val args =
                ListObjectsArgs.builder().bucket(bucket.name()).prefix(filePath).recursive(false)
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
                        fileRealName.processFileName(),
                        fileRealName,
                        "${BuildConfig.ENDPOINT}/${bucket.name()}/$fileRealName"
                    )
                }
            }.sortedWith(FileComparator())
            folderList
        }
    }

    class FileComparator : Comparator<FolderItemData> {
        override fun compare(file1: FolderItemData, file2: FolderItemData): Int {
            // 先按照文件类型排序
            val fileType1 = file1.fileType
            val fileType2 = file2.fileType
            if (fileType1.getFileTypeIndex() != fileType2.getFileTypeIndex()) {
                return fileType1.compareTo(fileType2)
            }
            // 对于相同类型的文件，再按照文件名排序
            return extractNumber(fileType1.name).compareTo(extractNumber(fileType2.name))
        }

        private fun extractNumber(filename: String): Int {
            val numberPattern = "\\d+".toRegex()
            val matchResult = numberPattern.find(filename)
            println("matchResult $matchResult ")
            return matchResult?.value?.toInt() ?: 0
        }
    }
}