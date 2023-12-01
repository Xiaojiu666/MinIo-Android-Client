package io.minio.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.minio.MinioClient
import io.minio.android.BuildConfig
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
}