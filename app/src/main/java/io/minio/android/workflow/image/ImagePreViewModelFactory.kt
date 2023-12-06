package io.minio.android.workflow.image

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
interface ImagePreViewModelFactory {
    fun create(
        @Assisted("imageString") imageString: String,
        @Assisted("selectorIndex")  selectorIndex: String
    ): ImagePreViewModel
}

@Composable
fun imagePreViewModel(
    imageString: String = "",
    selectorIndex: String= "",
    viewModelStoreOwner: ViewModelStoreOwner
): ImagePreViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity, ViewModelFactoryProvider::class.java
    ).imagePreViewModelFactory()
    return viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = ImagePreViewModel.provideFactory(
            factory,
            imageString,
            selectorIndex
        )
    )
}