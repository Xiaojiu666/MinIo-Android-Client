package io.minio.android.entities


data class FolderItemData(
    val fileType: FileType,
    val fileName: String,
    val realPath: String,
    val downloadUrl: String,
    val lastModifierTime: String = "",
    val tag: String = "",
    val checked: Boolean = false,
    val subSize: Int = 0,
    val fileSize: Long = 0,
)


enum class FileType(val index: Int) : Comparable<FileType> {
    FOLDER(0), IMAGE_FILE(1), TEXT_FILE(2), VIDEO_FILE(3)
//    override fun compareTo(other: FileType): Int {
//        return compareValuesBy(this, other) {
//            when (it) {
//                is Folder -> 0
//                is ImageFile -> 1
//                is TextFile -> 2
//            }
//        }
//    }
//
//    fun getFileTypeIndex(): Int {
//        return when (this) {
//            is Folder -> 0
//            is ImageFile -> 1
//            is TextFile -> 2
//        }
//    }
//
//    data class Folder(val folderName: String, val subSize: Int) : FileType(folderName)
//
//    data class ImageFile(
//        val imageName: String,
//        val fileSize: String,
//        val lastModifyData: String
//    ) :
//        FileType(imageName)
//
//    data class TextFile(
//        val textName: String,
//        val fileSize: String,
//        val lastModifyData: String
//    ) :
//        FileType(textName)
}