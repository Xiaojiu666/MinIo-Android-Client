package io.minio.android.workflow.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.minio.android.base.UiStateWrapper
import io.minio.android.entities.FolderItemData
import io.minio.android.entities.FolderPage
import io.minio.android.usecase.MinIoManagerUseCase
import io.minio.messages.Bucket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.file.Path
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
                val buckets = minIoManagerUseCase.queryBucketList()
                if (buckets.isNotEmpty()) {
                    val selectorBucket = buckets[0]
                    val value = HomeUiState(
                        buckets = buckets,
                        selectorBucket = selectorBucket,
                        foldPage = mutableListOf(),
                        onFolderSelector = ::onFolderSelector
                    )
                    _uiState.emit(value)
                } else {
                    _uiState.emit(UiStateWrapper.Error(NullPointerException(), ""))
                }
                (_uiState.value as HomeUiState).let { homeUiState ->
                    homeUiState.selectorBucket?.let {
                        val folder = minIoManagerUseCase.queryFolderByPath(it)
                        emitUiStateValue<HomeUiState> {
                            homeUiState.foldPage.add(folder)
                            it.copy(foldPage = homeUiState.foldPage)
                        }
                    }
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
                _uiState.emit(UiStateWrapper.Error(ex, ""))
            }
        }
    }


    private fun onFolderSelector(folderItem: FolderItemData) {
        viewModelScope.launch {
            (_uiState.value as HomeUiState).let { homeUiState ->
                homeUiState.selectorBucket?.let {
                    val folder = minIoManagerUseCase.queryFolderByPath(it, folderItem.path)
                    if (folder.folderPageFolderList.isEmpty()) {
                        emitUiStateValue<HomeUiState> {
                            it.copy(snackBarHostMsg = "There are no files in the current folder ~")
                        }
                    } else {
                        emitUiStateValue<HomeUiState> {
                            val newFolderPage = mutableListOf<FolderPage>()
                            newFolderPage.addAll(it.foldPage)
                            newFolderPage.add(folder)
                            it.copy(foldPage = newFolderPage)
                        }
                    }
                }
            }
        }
    }


    private fun <T : UiStateWrapper> emitUiStateValue(uiState: (T) -> T) {
        viewModelScope.launch {
            (_uiState.value as T).let {
                _uiState.emit(uiState(it))
            }
        }
    }


    data class HomeUiState(
        val buckets: List<Bucket>,
        val selectorBucket: Bucket?,
        val foldPage: MutableList<FolderPage>,
        val onFolderSelector: (FolderItemData) -> Unit,
        val snackBarHostMsg: String = ""
    ) : UiStateWrapper

}