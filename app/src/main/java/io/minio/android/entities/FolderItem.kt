package io.minio.android.entities

import java.nio.file.Path


data class FolderItemData(val fileType: FileType, val path: String)

data class FolderPage(val folderTitle: String, val folderPageFolderList: List<FolderItemData?>)

sealed class FileType(val name: String) {

    data class Folder(val folderName: String, val subSize: Int) : FileType(folderName)

    data class ImageFile(val imageName: String, val fileSize: String, val lastModifyData: String) :
        FileType(imageName)

    data class TextFile(val textName: String, val fileSize: String, val lastModifyData: String) :
        FileType(textName)

}