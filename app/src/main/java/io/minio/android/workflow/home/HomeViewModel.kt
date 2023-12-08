package io.minio.android.workflow.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.minio.android.base.PartUiStateWrapper
import io.minio.android.base.UiStateWrapper
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.entities.FolderPage
import io.minio.android.usecase.MinIoManagerUseCase
import io.minio.android.usecase.MinIoUpLoadFileUseCase
import io.minio.android.util.processFileName
import io.minio.android.util.removeElementsAfterIndex
import io.minio.messages.Bucket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val minIoManagerUseCase: MinIoManagerUseCase,
    private val minIoUpLoadFileUseCase: MinIoUpLoadFileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            buckets = null,
            selectorBucket = null,
            pagerUiState = UiStateWrapper.Loading,
            onFolderSelector = ::onFolderSelector,
            onFolderTabSelector = ::onFolderTabSelector,
            onUploadFile = ::onUploadFile,

            )
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
                    val folder = minIoManagerUseCase.queryFolderByPath(selectorBucket)
                    val pages = mutableListOf<FolderPage>()
                    pages.add(folder)
                    val pagerUiState = PagerUiState(pages)

                    emitHomeUiStateValue {
                        it.copy(
                            buckets = buckets,
                            selectorBucket = selectorBucket,
                            pagerUiState = pagerUiState
                        )
                    }
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }


    private fun onFolderSelector(folderItem: FolderItemData, nextList: MutableList<FolderPage>) {
        viewModelScope.launch {
            uiState.value.selectorBucket?.let { bucket ->
                when (folderItem.fileType) {
                    is FileType.Folder -> {
                        emitHomeUiStateValue {
                            it.copy(pagerUiState = UiStateWrapper.Loading)
                        }
                        val folder =
                            minIoManagerUseCase.queryFolderByPath(bucket, folderItem.realPath)
                        val newFolderPage = mutableListOf<FolderPage>()
                        newFolderPage.addAll(nextList)
                        newFolderPage.add(folder)
                        emitHomeUiStateValue {
                            val pagerUiState = PagerUiState(foldPage = newFolderPage)
                            it.copy(pagerUiState = pagerUiState)
                        }
                    }
                    is FileType.ImageFile -> {

                    }
                    is FileType.TextFile -> {}
                }

            }
        }
    }

    private fun onFolderTabSelector(index: Int) {
        viewModelScope.launch {
            (_uiState.value.pagerUiState as PagerUiState).let { pagerUiState ->
                val list = pagerUiState.foldPage.removeElementsAfterIndex(index)
                emitPageUiStateValue {
                    it.copy(foldPage = list)
                }
            }
        }
    }

    private fun onUploadFile(filePath: String) {
        viewModelScope.launch {
            uiState.value.selectorBucket?.let {
                val result =
                    minIoUpLoadFileUseCase.upLoadFile(it, filePath, filePath.processFileName())
            }
        }
    }

    private suspend fun emitPageUiStateValue(uiState: (PagerUiState) -> PagerUiState) {
        (_uiState.value.pagerUiState as? PagerUiState)?.let { pagerUiState ->
            emitHomeUiStateValue {
                it.copy(pagerUiState = uiState(pagerUiState))
            }
        }
    }

    private suspend fun emitHomeUiStateValue(uiState: (HomeUiState) -> HomeUiState) {
        _uiState.emit(
            uiState(
                _uiState.value
            )
        )
    }


    data class HomeUiState(
        val buckets: List<Bucket>?,
        val selectorBucket: Bucket?,
        val onFolderSelector: (FolderItemData, MutableList<FolderPage>) -> Unit,
        val onFolderTabSelector: (Int) -> Unit,
        val onUploadFile: (String) -> Unit,
        val pagerUiState: UiStateWrapper,
        val snackBarHostMsg: String = "",
    )

    data class PagerUiState(val foldPage: MutableList<FolderPage>) : UiStateWrapper

}

