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
import java.nio.file.Path
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
            selectorFilePath = "",
            pagerUiState = UiStateWrapper.Loading,
            onFolderSelector = ::onFolderSelector,
            onFolderTabSelector = ::onFolderTabSelector,
            onUploadFile = ::onUploadFile
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
                    val folder = minIoManagerUseCase.queryFoldersByPath(selectorBucket)
                    val pagerUiState =
                        PagerUiState(listOf(selectorBucket.name()), folderList = folder)
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


    private fun onFolderSelector(folderItem: FolderItemData) {
        viewModelScope.launch {
            uiState.value.selectorBucket?.let { bucket ->
                when (folderItem.fileType) {
                    is FileType.Folder -> {
                        emitHomeUiStateValue {
                            it.copy(pagerUiState = UiStateWrapper.Loading)
                        }
                        val folder =
                            minIoManagerUseCase.queryFoldersByPath(bucket, folderItem.realPath)
                        emitPageUiStateValue {
                            val titlePaths = mutableListOf<String>()
                            it.titlePaths?.forEach {
                                titlePaths.add(it)
                            }
                            titlePaths.add(folderItem.fileName)
                            it.copy(titlePaths = titlePaths, folderList = folder)
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
            val selectorBucket = _uiState.value.selectorBucket
            (_uiState.value.pagerUiState as PagerUiState).let { pagerUiState ->
                val titlePaths = pagerUiState.titlePaths
                selectorBucket?.let {
                    if (titlePaths != null) {
                        val folder = minIoManagerUseCase.queryFoldersByPath(
                            it,
                            titlePaths?.subList(0, index)?.joinToString { "/" } ?: ""
                        )
                        emitPageUiStateValue {
                            it.copy(titlePaths = titlePaths, folderList = folder)
                        }
                    }
                }

            }
        }
    }

    private fun onUploadFile(filePath: String) {
        viewModelScope.launch {
            uiState.value.selectorBucket?.let {
                val result =
                    minIoUpLoadFileUseCase.upLoadFile(
                        it,
                        filePath,
                        filePath.processFileName(),
                        originPath =
                        uiState.value.selectorFilePath
                    )
                println("result $result ,origin path ${uiState.value.selectorFilePath}")
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
        println("uiState $uiState")
        _uiState.emit(
            uiState(
                _uiState.value
            )
        )
    }


    data class HomeUiState(
        val buckets: List<Bucket>?,
        val selectorBucket: Bucket?,
        val selectorFilePath: String,
        val onFolderSelector: (FolderItemData) -> Unit,
        val onFolderTabSelector: (Int) -> Unit,
        val onUploadFile: (String) -> Unit,
        val pagerUiState: UiStateWrapper,
        val snackBarHostMsg: String = "",
    )

    data class PagerUiState(
        val titlePaths: List<String>?,
        val folderList: List<FolderItemData>?
    ) : UiStateWrapper

}

