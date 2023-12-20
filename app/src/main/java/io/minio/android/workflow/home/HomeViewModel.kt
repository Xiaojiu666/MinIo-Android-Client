package io.minio.android.workflow.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.newStringBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.minio.android.base.PartUiStateWrapper
import io.minio.android.base.UiStateWrapper
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.repo.MinioApiRepo
import io.minio.android.usecase.MinIoManagerUseCase
import io.minio.android.usecase.MinIoUpLoadFileUseCase
import io.minio.android.util.processFileName
import io.minio.messages.Bucket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KSuspendFunction1

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val minIoManagerUseCase: MinIoManagerUseCase,
    private val minIoUpLoadFileUseCase: MinIoUpLoadFileUseCase,
    val minioApiRepo: MinioApiRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            onTopBarModelChange = ::onTopBarModelChange,
            topBarUiState = TopBarUiState(
                onBucketSelector = { },
                onUploadFile = ::onUploadFile,
                onDeleteFile = ::onDeleteFile
            ),
            folderPathUiState = FolderPathUiState(
                folderPaths = listOf(),
                onFolderTabSelector = ::onFolderTabSelector,
                onHomeTabClick = ::onHomeTabClick,
            ), pagerUiState = PagerUiState(
                onFolderClick = ::onFolderClick,
                onUpdateSelectorFolders = ::onUpdateSelectorFolders
            )
        )
    )


    val uiState = _uiState.asStateFlow()


    init {
        initMinIoBuckets()
    }

    private fun initMinIoBuckets() {
        viewModelScope.launch {
            try {
                minioApiRepo.downLoadTxtFile("http://59.110.154.87:9000/comic/Test/111/Hello word.txt")
                val buckets = minIoManagerUseCase.queryBucketList()
                if (buckets.isNotEmpty()) {
                    val selectorBucket = buckets[0]
                    emitTopBarUiStateValue {
                        it.copy(
                            buckets = buckets,
                            bucket = selectorBucket
                        )
                    }
                    emitHomeUiStateValue {
                        it.copy(showPagerLoading = true)
                    }
                    val folder = minIoManagerUseCase.queryFoldersByPath(selectorBucket)
                    emitFolderUiStateValue {
                        it.copy(folderList = folder)
                    }
                    emitHomeUiStateValue {
                        it.copy(showPagerLoading = false)
                    }
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    private fun onTopBarModelChange(topBarModel: TopBarModel) {
        viewModelScope.launch {
            _uiState.emit(
                uiState.value.copy(topBarModel = topBarModel)
            )
        }
    }

    private fun onFolderClick(folderItem: FolderItemData) {
        viewModelScope.launch {
            uiState.value.topBarUiState.bucket?.let { bucket ->
                when (folderItem.fileType) {
                    is FileType.Folder -> {
                        emitHomeUiStateValue {
                            it.copy(showPagerLoading = true)
                        }
                        val folder =
                            minIoManagerUseCase.queryFoldersByPath(bucket, folderItem.realPath)
                        val folderPath = mutableListOf<String>()
                        uiState.value.folderPathUiState.folderPaths.forEach {
                            folderPath.add(it)
                        }
                        folderPath.add(folderItem.fileName)
                        emitFolderPathUiStateValue {
                            it.copy(folderPaths = folderPath)
                        }
                        emitFolderUiStateValue {
                            it.copy(
                                folderList = folder,
                            )
                        }
                        emitHomeUiStateValue {
                            it.copy(showPagerLoading = false, topBarModel = TopBarModel.INCREASE)
                        }
                    }
                    is FileType.ImageFile -> {

                    }
                    is FileType.TextFile -> {}
                }
            }
        }
    }

    private fun onHomeTabClick() {
        viewModelScope.launch {
            _uiState.value.topBarUiState.bucket?.let { bucket ->
                val folder = minIoManagerUseCase.queryFoldersByPath(
                    bucket
                )
                emitFolderPathUiStateValue {
                    it.copy(folderPaths = listOf())
                }
                emitFolderUiStateValue {
                    it.copy(
                        folderList = folder,
                    )
                }
                emitHomeUiStateValue {
                    it.copy(topBarModel = TopBarModel.INCREASE)
                }
            }
        }
    }

    private fun onFolderTabSelector(index: Int) {
        viewModelScope.launch {
            _uiState.value.topBarUiState.bucket?.let { bucket ->
                emitHomeUiStateValue {
                    it.copy(showPagerLoading = true)
                }
                val subTitlePath =
                    _uiState.value.folderPathUiState.folderPaths.subList(0, index + 1)
                val folderPath = newStringBuilder()
                subTitlePath.forEach {
                    folderPath.append("$it/")
                }
                val folder = minIoManagerUseCase.queryFoldersByPath(
                    bucket,
                    folderPath.toString()
                )
                emitFolderPathUiStateValue {
                    it.copy(folderPaths = subTitlePath)
                }
                emitFolderUiStateValue {
                    it.copy(
                        folderList = folder,
                    )
                }
                emitHomeUiStateValue {
                    it.copy(showPagerLoading = false, topBarModel = TopBarModel.INCREASE)
                }
            }
        }
    }


    private suspend fun updatePagerData() {
        uiState.value.topBarUiState.bucket?.let { it1 ->
            emitHomeUiStateValue {
                it.copy(showPagerLoading = true)
            }
            val subTitlePath =
                _uiState.value.folderPathUiState.folderPaths
            val folderPath = newStringBuilder()
            subTitlePath.forEach {
                folderPath.append("$it/")
            }
            val folderList = minIoManagerUseCase.queryFoldersByPath(
                it1,
                folderPath.toString(),
                false
            )
            emitFolderUiStateValue {
                it.copy(
                    folderList = folderList, selectorFolders = mutableListOf()
                )
            }
            emitHomeUiStateValue {
                it.copy(showPagerLoading = false, topBarModel = TopBarModel.INCREASE)
            }
        }
    }

    private fun onUploadFile(filePath: String) {
        viewModelScope.launch {
            uiState.value.topBarUiState.bucket?.let {
                val originPath =
                    uiState.value.folderPathUiState.folderPaths.joinToString { "/" }
                minIoUpLoadFileUseCase.upLoadFile(
                    it,
                    filePath,
                    filePath.processFileName(),
                    originPath = originPath
                )
                updatePagerData()
            }
        }
    }

    private fun onDeleteFile() {
        viewModelScope.launch {
            uiState.value.topBarUiState.bucket?.let {
                val deleteFiles = uiState.value.pagerUiState.selectorFolders
                minIoManagerUseCase.deleteFile(it, deleteFiles)
                updatePagerData()
            }
        }
    }

    private fun onUpdateSelectorFolders(folderItemData: FolderItemData) {
        viewModelScope.launch {
            val selectorFolders = uiState.value.pagerUiState.selectorFolders
            val newList = mutableListOf<String>()
            newList.addAll(selectorFolders)
            if (newList.contains(folderItemData.realPath)) {
                newList.remove(folderItemData.realPath)
            } else {
                newList.add(folderItemData.realPath)
            }
            emitFolderUiStateValue {
                it.copy(
                    selectorFolders = newList
                )
            }
        }
    }


    private suspend fun emitFolderUiStateValue(pagerUiState: (PagerUiState) -> PagerUiState) {
        emitHomeUiStateValue {
            it.copy(
                pagerUiState = pagerUiState(
                    _uiState.value.pagerUiState
                )
            )
        }
    }

    private suspend fun emitTopBarUiStateValue(topBarUiState: (TopBarUiState) -> TopBarUiState) {
        emitHomeUiStateValue {
            it.copy(
                topBarUiState = topBarUiState(
                    _uiState.value.topBarUiState
                )
            )
        }
    }

    private suspend fun emitFolderPathUiStateValue(folderPathUiState: (FolderPathUiState) -> FolderPathUiState) {
        emitHomeUiStateValue {
            it.copy(
                folderPathUiState = folderPathUiState(
                    _uiState.value.folderPathUiState
                )
            )
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
        val topBarUiState: TopBarUiState,
        val folderPathUiState: FolderPathUiState,
        val pagerUiState: PagerUiState,
        val showPagerLoading: Boolean = false,
        val onTopBarModelChange: (TopBarModel) -> Unit,
        val topBarModel: TopBarModel = TopBarModel.INCREASE
    )

    data class TopBarUiState(
        val buckets: List<Bucket>? = null,
        val bucket: Bucket? = null,
        val onBucketSelector: () -> Unit,
        val onUploadFile: (String) -> Unit,
        val onDeleteFile: () -> Unit,
    )

    data class FolderPathUiState(
        val folderPaths: List<String>,
        val onFolderTabSelector: (Int) -> Unit,
        val onHomeTabClick: () -> Unit,
    )

    data class PagerUiState(
        val folderList: List<FolderItemData>? = null,
        val onFolderClick: (FolderItemData) -> Unit,
        val onUpdateSelectorFolders: (FolderItemData) -> Unit,
        val selectorFolders: MutableList<String> = mutableListOf(),
    ) {
        fun updatePagerUiState(it: FolderItemData): FolderItemData {
            val uiState = this
            var folderItemData = it
            folderItemData = if (it.realPath in uiState.selectorFolders) {
                folderItemData.copy(checked = true)
            } else {
                folderItemData.copy(checked = false)
            }
            return folderItemData
        }
    }
}

enum class TopBarModel {
    INCREASE, DELETE
}
