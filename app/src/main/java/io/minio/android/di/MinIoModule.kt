package io.minio.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.minio.MinioClient
import io.minio.android.BuildConfig
import io.minio.android.util.cache.DataCache
import io.minio.android.util.cache.LruDataCache
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
}