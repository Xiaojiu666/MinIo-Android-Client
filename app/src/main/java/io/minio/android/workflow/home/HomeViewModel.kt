package io.minio.android.workflow.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.minio.android.base.UiStateWrapper
import io.minio.android.usecase.MinIoManagerUseCase
import io.minio.messages.Bucket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val minIoManagerUseCase: MinIoManagerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateWrapper>(
        UiStateWrapper.Loading
    )
    val uiState = _uiState.asStateFlow()

    init {
        initMinIoBuckets()
    }

    private fun initMinIoBuckets() {
        viewModelScope.launch {
            try {
                delay(2000)
                val buckets = minIoManagerUseCase.queryBucketList()
                if (buckets.isNotEmpty()) {
                    val value = HomeUiState(buckets = buckets, selectorBucket = buckets[0])
                    _uiState.emit(value)
                } else {
                    _uiState.emit(UiStateWrapper.Error(NullPointerException(), ""))
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
                _uiState.emit(UiStateWrapper.Error(ex, ""))
            }
        }
    }


    data class HomeUiState(val buckets: List<Bucket>, val selectorBucket: Bucket?) : UiStateWrapper
}