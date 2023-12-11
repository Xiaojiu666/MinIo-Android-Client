package io.minio.android.workflow.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.minio.android.base.LoadableState
import io.minio.android.base.PartUiStateWrapper
import io.minio.android.base.UiStateWrapper
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.usecase.MinIoManagerUseCase
import io.minio.android.usecase.MinIoUpLoadFileUseCase
import io.minio.android.util.processFileName
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
            selectorFilePath = "",
            titlePaths = listOf(),
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
                    emitHomeUiStateValue {
                        it.copy(
                            buckets = buckets,
                            titlePaths = listOf(selectorBucket.name()),
                            selectorBucket = selectorBucket,
                            pagerUiState = LoadableState.Success(PagerUiState(folderList = folder))
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
            val titlePaths = _uiState.value.titlePaths
            uiState.value.selectorBucket?.let { bucket ->
                when (folderItem.fileType) {
                    is FileType.Folder -> {
                        emitHomeUiStateValue {
                            it.copy(pagerUiState = LoadableState.Loading())
                        }
                        val folder =
                            minIoManagerUseCase.queryFoldersByPath(bucket, folderItem.realPath)
                        val newList = mutableListOf<String>()
                        titlePaths?.forEach {
                            newList.add(it)
                        }
                        newList.add(folderItem.fileName)
                        emitHomeUiStateValue {
                            it.copy(
                                titlePaths = newList,
                                pagerUiState = LoadableState.Success(PagerUiState(folderList = folder))
                            )
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
            var subTitlePath = _uiState.value.titlePaths?.subList(1, index + 1)
            println("subTitlePath $subTitlePath")
            selectorBucket?.let { bucket ->
                val filepath = subTitlePath?.joinToString { "/" } ?: ""
                val folder = minIoManagerUseCase.queryFoldersByPath(
                    bucket,
                    filepath
                )
                emitHomeUiStateValue {
                    it.copy(
                        titlePaths = subTitlePath,
                        pagerUiState = LoadableState.Success(PagerUiState(folderList = folder))
                    )
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
        val pagerUiState: LoadableState<PagerUiState> = LoadableState.Loading(),
        val snackBarHostMsg: String = "",
        val titlePaths: List<String>?,
    )

    data class PagerUiState(
        val folderList: List<FolderItemData>?
    )

}

