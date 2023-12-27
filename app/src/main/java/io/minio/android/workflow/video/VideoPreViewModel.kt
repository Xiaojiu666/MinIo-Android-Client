package io.minio.android.workflow.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.minio.android.base.UiStateWrapper
import io.minio.android.usecase.DownloadTxtUseCase
import io.minio.android.workflow.text.TextPreViewModel
import io.minio.android.workflow.text.TxtPreViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class VideoPreViewModel @AssistedInject constructor(
    @Assisted("fileUrl") private val fileUrl: String,
) : ViewModel() {

    companion object {
        fun provideFactory(
            assistedFactory: VideoPreViewModelFactory,
            fileUrl: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    fileUrl = fileUrl
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(
        VideoPreUiState(fileUrl)
    )
    val uiState = _uiState.asStateFlow()

    init {
        downloadTxt()
    }

    private fun downloadTxt() {
        viewModelScope.launch {
            updateTextPreUiState{
                it.copy(videoUrl = fileUrl)
            }
        }
    }

    private suspend fun updateTextPreUiState(block: (VideoPreUiState) -> VideoPreUiState) {
        val uiState = _uiState.value
        _uiState.emit(
            block(uiState)
        )
    }

    data class VideoPreUiState(
        val videoUrl: String,
    ) : UiStateWrapper
}