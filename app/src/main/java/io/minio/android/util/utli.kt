package io.minio.android.util



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