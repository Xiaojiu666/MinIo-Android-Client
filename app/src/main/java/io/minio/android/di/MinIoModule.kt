package io.minio.android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.minio.MinioClient
import io.minio.android.ACCESS_KEY
import io.minio.android.ENDPOINT
import io.minio.android.SECRET_KEY
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class MinIoModule {

    @Singleton
    @Provides
    fun provideMinIoClient(): MinioClient {

        return MinioClient.builder().endpoint(/* endpoint = */ ENDPOINT)
            .credentials(ACCESS_KEY, SECRET_KEY).build()
    }
}