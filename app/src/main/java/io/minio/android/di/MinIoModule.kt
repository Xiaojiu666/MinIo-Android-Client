package io.minio.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.minio.MinioClient
import io.minio.android.BuildConfig
import io.minio.android.repo.service.ApiService
import io.minio.android.util.cache.DataCache
import io.minio.android.util.cache.LruDataCache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class MinIoModule {

    @Singleton
    @Provides
    fun provideMinIoClient(): MinioClient {
        return MinioClient.builder().endpoint(/* endpoint = */ BuildConfig.ENDPOINT)
            .credentials(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY).build()
    }

    @Singleton
    @Provides
    fun provideDataCache(): DataCache {
        return LruDataCache()
    }

    private val BASE_URL = "https://example.com/" // 你的基础 URL
    private val httpLoggingInterceptor = HttpLoggingInterceptor()

    @Singleton
    @Provides
    fun provideApiService(): ApiService {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(
            httpLoggingInterceptor
        ).build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(ApiService::class.java)
    }
}