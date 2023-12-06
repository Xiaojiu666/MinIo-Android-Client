package io.minio.android.workflow.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.minio.android.base.UiStateWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ImagePreViewModel @AssistedInject constructor(
    @Assisted("imageString") private val imageString: String,
    @Assisted("selectorIndex") private val selectorIndex: String
) : ViewModel() {

    companion object {
        fun provideFactory(
            assistedFactory: ImagePreViewModelFactory,
            imageString: String,
            selectorIndex: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    imageString = imageString, selectorIndex = selectorIndex
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(
        ImagePreUiState(listOf())
    )
    val uiState = _uiState.asStateFlow()

    init {
        getMhReaderPageList()
    }

    private fun getMhReaderPageList() {
        viewModelScope.launch {
            updateMhReaderUiState {
                it.copy(comicList = imageString.split(","))
            }
        }
    }


    private suspend fun updateMhReaderUiState(block: (ImagePreUiState) -> ImagePreUiState) {
        val diaryEditUiState = _uiState.value
        _uiState.emit(
            block(diaryEditUiState)
        )
    }

    data class ImagePreUiState(
        val comicList: List<String>,
    ) : UiStateWrapper
}