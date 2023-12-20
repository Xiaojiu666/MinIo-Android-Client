package io.minio.android.workflow.text

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
interface TxtPreViewModelFactory {
    fun create(
        @Assisted("fileUrl") fileUrl: String,
    ): TextPreViewModel
}

@Composable
fun textPreViewModel(
    fileUrl: String= "",
    viewModelStoreOwner: ViewModelStoreOwner
): TextPreViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity, ViewModelFactoryProvider::class.java
    ).txtPreViewModelFactory()
    return viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = TextPreViewModel.provideFactory(
            factory,
            fileUrl,
        )
    )
}