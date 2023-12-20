package io.minio.android.repo.service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun downloadText(@Url url: String): String
}