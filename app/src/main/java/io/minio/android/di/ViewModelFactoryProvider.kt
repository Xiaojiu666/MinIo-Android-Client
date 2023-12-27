package io.minio.android.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.minio.android.workflow.image.ImagePreViewModelFactory
import io.minio.android.workflow.text.TxtPreViewModelFactory
import io.minio.android.workflow.video.VideoPreViewModel
import io.minio.android.workflow.video.VideoPreViewModelFactory

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {

    fun imagePreViewModelFactory(): ImagePreViewModelFactory

    fun txtPreViewModelFactory(): TxtPreViewModelFactory

    fun videoPreViewModelFactory(): VideoPreViewModelFactory
}