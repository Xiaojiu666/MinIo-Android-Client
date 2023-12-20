package io.minio.android.repo

import io.minio.android.repo.service.ApiService
import javax.inject.Inject

class MinioApiRepo @Inject constructor(private val apiService: ApiService) {


    suspend fun downLoadTxtFile(url: String): String {
        return apiService.downloadText(url)
    }
}