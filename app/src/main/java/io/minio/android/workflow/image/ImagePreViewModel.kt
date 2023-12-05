package io.minio.android.workflow.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.minio.android.base.UiStateWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ImagePreViewModel @Inject  constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ImagePreUiState(listOf())
    )
    val uiState =
        _uiState.asStateFlow()

    init {
        getMhReaderPageList()
    }

    private fun getMhReaderPageList() {
        viewModelScope.launch {
            updateMhReaderUiState {
                it.copy(comicList = listOf())
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