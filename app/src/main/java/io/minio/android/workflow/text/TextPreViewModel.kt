package io.minio.android.workflow.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.minio.android.base.UiStateWrapper
import io.minio.android.usecase.DownloadTxtUseCase
import io.minio.android.workflow.image.ImagePreViewModel
import io.minio.android.workflow.image.ImagePreViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class TextPreViewModel @AssistedInject constructor(
    @Assisted("fileUrl") private val fileUrl: String,
    val downloadTxtUseCase: DownloadTxtUseCase,
) : ViewModel() {

    companion object {
        fun provideFactory(
            assistedFactory: TxtPreViewModelFactory,
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
        TextPreUiState("")
    )
    val uiState = _uiState.asStateFlow()

    init {
        downloadTxt()
    }

    private fun downloadTxt() {
        viewModelScope.launch {
           val txtContent = downloadTxtUseCase(fileUrl)
            updateTextPreUiState{
                it.copy(txtContent = txtContent)
            }
        }
    }

    private suspend fun updateTextPreUiState(block: (TextPreUiState) -> TextPreUiState) {
        val diaryEditUiState = _uiState.value
        _uiState.emit(
            block(diaryEditUiState)
        )
    }

    data class TextPreUiState(
        val txtContent: String,
    ) : UiStateWrapper
}