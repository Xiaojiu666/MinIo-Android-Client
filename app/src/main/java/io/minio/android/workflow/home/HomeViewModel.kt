package io.minio.android.workflow.home

import android.graphics.pdf.PdfDocument.Page
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.newStringBuilder
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

    private val _uiState = MutableStateFlow(HomeUiState())
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
                    val topBarUiState = TopBarUiState(
                        buckets = buckets,
                        bucket = selectorBucket,
                        onBucketSelector = { },
                        onUploadFile = ::onUploadFile
                    )
                    val folderPathUiState = FolderPathUiState(
                        listOf(), onFolderTabSelector = ::onFolderTabSelector
                    )
                    val pagerUiState =
                        LoadableState.Success(
                            PagerUiState(
                                folderList = folder,
                                onFolderClick = ::onFolderClick
                            )
                        )
                    emitHomeUiStateValue {
                        it.copy(
                            topBarUiState = topBarUiState,
                            folderPathUiState = folderPathUiState,
                            pagerUiState = pagerUiState
                        )
                    }
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    private fun onFolderClick(folderItem: FolderItemData) {
        viewModelScope.launch {
            uiState.value.topBarUiState?.bucket?.let { bucket ->
                when (folderItem.fileType) {
                    is FileType.Folder -> {
                        emitHomeUiStateValue {
                            it.copy(pagerUiState = LoadableState.Loading())
                        }
                        val folder =
                            minIoManagerUseCase.queryFoldersByPath(bucket, folderItem.realPath)
                        val folderPath = mutableListOf<String>()
                        uiState.value.folderPathUiState?.folderPaths?.forEach {
                            folderPath.add(it)
                        }
                        folderPath.add(folderItem.fileName)
                        emitHomeUiStateValue {
                            val pagerUiState = PagerUiState(
                                folderList = folder,
                                onFolderClick = ::onFolderClick
                            )
                            val folderPathUiState = it.folderPathUiState?.copy(
                                folderPaths = folderPath
                            )
                            it.copy(
                                pagerUiState = LoadableState.Success(pagerUiState),
                                folderPathUiState = folderPathUiState
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
            _uiState.value.topBarUiState?.bucket?.let { bucket ->
                val subTitlePath =
                    _uiState.value.folderPathUiState?.folderPaths?.subList(0, index + 1) ?: listOf()
                println("subTitlePath $subTitlePath")
                val folderPath = newStringBuilder()
                subTitlePath.forEach {
                    folderPath.append("$it/")
                }
                val folder = minIoManagerUseCase.queryFoldersByPath(
                    bucket,
                    folderPath.toString()
                )
                emitHomeUiStateValue {
                    val pagerUiState = PagerUiState(
                        folderList = folder,
                        onFolderClick = ::onFolderClick
                    )
                    val folderPathUiState = it.folderPathUiState?.copy(
                        folderPaths = subTitlePath ?: listOf()
                    )
                    it.copy(
                        pagerUiState = LoadableState.Success(pagerUiState),
                        folderPathUiState = folderPathUiState
                    )
                }
            }
        }
    }

    private fun onUploadFile(filePath: String) {
        viewModelScope.launch {
            uiState.value.topBarUiState?.bucket?.let {
                val originPath =
                    uiState.value.folderPathUiState?.folderPaths?.joinToString { "/" } ?: ""
                val result =
                    minIoUpLoadFileUseCase.upLoadFile(
                        it,
                        filePath,
                        filePath.processFileName(),
                        originPath = originPath
                    )
                println("result $result ,origin path ${originPath}")
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
        val topBarUiState: TopBarUiState? = null,
        val pagerUiState: LoadableState<PagerUiState> = LoadableState.Loading(),
        val folderPathUiState: FolderPathUiState? = null,
    )

    data class TopBarUiState(
        val buckets: List<Bucket>?,
        val bucket: Bucket,
        val onBucketSelector: () -> Unit,
        val onUploadFile: (String) -> Unit,
    )

    data class FolderPathUiState(
        val folderPaths: List<String>,
        val onFolderTabSelector: (Int) -> Unit
    )

    data class PagerUiState(
        val folderList: List<FolderItemData>?,
        val onFolderClick: (FolderItemData) -> Unit,
    )

}

