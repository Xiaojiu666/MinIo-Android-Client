package io.minio.android.usecase

import io.minio.android.repo.MinioApiRepo
import javax.inject.Inject

class DownloadTxtUseCase @Inject constructor(
    private val minioApiRepo: MinioApiRepo
) {

    suspend operator fun invoke(filePath: String): String {
        return minioApiRepo.downLoadTxtFile(filePath)
    }
}