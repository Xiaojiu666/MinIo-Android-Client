package io.minio.android.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File


fun Long.formatFileSize(): String {
    val kilobyte = 1024
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024
    val terabyte = gigabyte * 1024

    return if (this < kilobyte) {
        "$this bits"
    } else if (this < megabyte) {
        val sizeInBytes = this.toDouble() / kilobyte
        "%.2f KB".format(sizeInBytes)
    } else if (this < gigabyte) {
        val sizeInKB = this.toDouble() / megabyte
        "%.2f MB".format(sizeInKB)
    } else if (this < terabyte) {
        val sizeInMB = this.toDouble() / gigabyte
        "%.2f GB".format(sizeInMB)
    } else {
        val sizeInGB = this.toDouble() / terabyte
        "%.2f TB".format(sizeInGB)
    }
}

fun String.processFileName(): String {
    var result = this.removeSuffix("/")

    val lastSlashIndex = result.lastIndexOf("/")
    if (lastSlashIndex != -1 && lastSlashIndex < result.length - 1) {
        result = result.substring(lastSlashIndex + 1)
    }

    return result
}

fun <T> MutableList<T>.removeElementsAfterIndex(indexToRemove: Int): MutableList<T> {
    if (indexToRemove < 0 || indexToRemove >= this.size) {
        // 索引越界检查
        return this
    }
    return this.subList(0, indexToRemove + 1).toMutableList()
}

fun Uri.ToFile(context: Context): File? {
    // 检查 Uri 的 scheme
    if ("content" == this.scheme) {
        // 处理 content 类型的 Uri
        return getFileFromContentUri(context, this)
    } else if ("file" == this.scheme) {
        // 处理 file 类型的 Uri
        return this.path?.let { File(it) }
    }
    return null // 如果无法处理，返回 null 或者抛出异常，视情况而定
}

private fun getFileFromContentUri(context: Context, contentUri: Uri): File? {
    // 使用 ContentResolver 获取真实路径
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor? = context.contentResolver.query(contentUri, projection, null, null, null)
    if (cursor != null && cursor.moveToFirst()) {
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val filePath: String = cursor.getString(columnIndex)
        cursor.close()
        return File(filePath)
    }
    return null
}
