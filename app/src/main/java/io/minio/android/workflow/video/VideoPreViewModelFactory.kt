package io.minio.android.workflow.video

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.hilt.android.EntryPointAccessors
import io.minio.android.di.ViewModelFactoryProvider

@AssistedFactory
interface VideoPreViewModelFactory {
    fun create(
        @Assisted("fileUrl") fileUrl: String,
    ): VideoPreViewModel
}

@Composable
fun videoPreViewModel(
    fileUrl: String= "",
    viewModelStoreOwner: ViewModelStoreOwner
): VideoPreViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity, ViewModelFactoryProvider::class.java
    ).videoPreViewModelFactory()
    return viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = VideoPreViewModel.provideFactory(
            factory,
            fileUrl,
        )
    )
}