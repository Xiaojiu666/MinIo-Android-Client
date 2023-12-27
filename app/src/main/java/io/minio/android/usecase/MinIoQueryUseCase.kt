package io.minio.android.usecase


import io.minio.android.BuildConfig
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.repo.MInIoClientRepo
import io.minio.android.util.FileComparator
import io.minio.android.util.cache.DataCache
import io.minio.android.util.processFileName
import io.minio.messages.Bucket
import javax.inject.Inject

class MinIoQueryUseCase @Inject constructor(
    private val mInIoClientRepo: MInIoClientRepo,
    private val dataCache: DataCache
) {

    suspend fun queryBucketList(): List<Bucket> = mInIoClientRepo.queryListBucket()

    suspend fun queryFoldersByPath(
        bucket: Bucket,
        filePath: String = "",
        cache: Boolean = true
    ): List<FolderItemData> {
        println("queryFoldersByPath bucket : ${bucket.name()} ,filePath : $filePath")
        val cacheData = dataCache.get<List<FolderItemData>>("${bucket.name()}$filePath")
        if (cacheData != null && cache) {
            return cacheData
        }
        val folders = mInIoClientRepo.queryListObject(bucket, filePath).map {
            val fileRealName = it.objectName()
            val extension = fileRealName.substringAfterLast('.', "").filterFileType()
            if (it.isDir) {
                val subFolder = mInIoClientRepo.queryListObject(
                    bucket, fileRealName
                ).toList().size
                FolderItemData(
                    FileType.FOLDER,
                    fileRealName.processFileName(),
                    realPath = fileRealName,
                    downloadUrl = "${BuildConfig.ENDPOINT}/${bucket.name()}/$fileRealName",
                    subSize = subFolder,
                )
            } else {
                val fileState = mInIoClientRepo.queryObjectState(bucket, fileRealName)
                FolderItemData(
                    extension,
                    fileRealName.processFileName(),
                    realPath = fileRealName,
                    downloadUrl = "${BuildConfig.ENDPOINT}/${bucket.name()}/$fileRealName",
                    lastModifierTime = fileState.lastModified().toString(),
                    tag = fileState.etag() ?: "",
                    fileSize = fileState.size()
                )
            }
        }.sortedWith(FileComparator())
        dataCache.put("${bucket.name()}$filePath", folders)
        println("queryFoldersByPath folders : $folders")
        return folders
    }




    private fun String.filterFileType(): FileType {
        return when (this) {
            "txt", "log", "xml", "json" -> FileType.TEXT_FILE
            "jpg", "jpeg", "png", "gif" -> FileType.IMAGE_FILE
            "mp4","MP4","MOV", "3gp", "avi", "mkv" -> FileType.VIDEO_FILE
            else -> FileType.TEXT_FILE
        }
    }

}