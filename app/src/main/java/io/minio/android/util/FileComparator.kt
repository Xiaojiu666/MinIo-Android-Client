package io.minio.android.util

import io.minio.android.entities.FolderItemData

class FileComparator: Comparator<FolderItemData> {
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
        return matchResult?.value?.toIntOrNull() ?: 0
    }
}