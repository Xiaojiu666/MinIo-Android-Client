package io.minio.android.usecase


import io.minio.*
import io.minio.android.BuildConfig
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.entities.FolderPage
import io.minio.android.repo.MInIoClientRepo
import io.minio.android.repo.MinIoCacheRepo
import io.minio.android.util.cache.DataCache
import io.minio.android.util.formatFileSize
import io.minio.android.util.processFileName
import io.minio.messages.Bucket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class MinIoManagerUseCase @Inject constructor(
    private val mInIoClientRepo: MInIoClientRepo,
    private val dataCache: DataCache
) {

    suspend fun queryBucketList(): List<Bucket> = mInIoClientRepo.queryListBucket()

    suspend fun queryFoldersByPath(
        bucket: Bucket,
        filePath: String = "",
        cache: Boolean = true
    ): List<FolderItemData> {
        println("queryFoldersByPath bucket : p${bucket.name()} filePath : ${filePath}")
        val cacheData = dataCache.get<List<FolderItemData>>("${bucket.name()}$filePath")
        if (cacheData != null && cache) {
            return cacheData
        }
        val folders = mInIoClientRepo.queryListObject(bucket, filePath).map {
            val fileRealName = it.objectName()
            val extension = fileRealName.substringAfterLast('.', "")
            val type = if (it.isDir) {
                val subFolder = mInIoClientRepo.queryListObject(
                    bucket, fileRealName
                ).toList()
                FileType.Folder(fileRealName.processFileName(), subFolder.size)
            } else {
                when (extension.lowercase(Locale.ROOT)) {
                    "jpg", "jpeg", "png", "gif", "webp" -> {
                        val fileName = mInIoClientRepo.queryObjectState(bucket, fileRealName)
                        FileType.ImageFile(
                            fileRealName.processFileName(),
                            fileName.size().formatFileSize(),
                            fileName.lastModified().toString()
                        )
                    }
                    else -> {
                        val fileName = mInIoClientRepo.queryObjectState(bucket, fileRealName)
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
        }.sortedWith(FileComparator())
        dataCache.put("${bucket.name()}$filePath", folders)
        return folders
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
            return matchResult?.value?.substring(0..9)?.toInt() ?: 0
        }
    }
}