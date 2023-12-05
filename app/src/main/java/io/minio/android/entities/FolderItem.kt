package io.minio.android.entities


data class FolderItemData(val fileType: FileType, val realPath: String, val downloadUrl: String)

data class FolderPage(val folderTitle: String, val folderPageFolderList: List<FolderItemData?>)

sealed class FileType(val name: String) : Comparable<FileType> {
    override fun compareTo(other: FileType): Int {
        return compareValuesBy(this, other) {
            when (it) {
                is Folder -> 0
                is ImageFile -> 1
                is TextFile -> 2
            }
        }
    }

    data class Folder(val folderName: String, val subSize: Int) : FileType(folderName)

    data class ImageFile(val imageName: String, val fileSize: String, val lastModifyData: String) :
        FileType(imageName)

    data class TextFile(val textName: String, val fileSize: String, val lastModifyData: String) :
        FileType(textName)
}