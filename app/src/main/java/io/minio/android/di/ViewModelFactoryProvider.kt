package io.minio.android.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.minio.android.workflow.image.ImagePreViewModelFactory

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {

    fun imagePreViewModelFactory(): ImagePreViewModelFactory
}