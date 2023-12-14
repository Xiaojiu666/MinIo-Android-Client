package io.minio.android.workflow.home

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.minio.android.R
import io.minio.android.base.LoadableLayout
import io.minio.android.base.ui.theme.body1
import io.minio.android.base.ui.theme.body2
import io.minio.android.base.ui.theme.body3
import io.minio.android.base.ui.theme.colorBackground
import io.minio.android.base.ui.theme.colorTertiary
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.util.getFileFromSAFUri
import io.minio.android.workflow.IMAGE_PRE_PAGE
import io.minio.android.workflow.home.ui.HomeTopBar
import io.minio.messages.Bucket
import kotlinx.coroutines.launch


@Composable
fun HomeRouter(viewModel: HomeViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomePage(uiState, onImageFileClick = { images, index ->
        navController.navigate("$IMAGE_PRE_PAGE\$?images=${images.joinToString(",")}&selectorIndex=$index")
    })
}


@Composable
fun HomePage(uiState: HomeViewModel.HomeUiState, onImageFileClick: (List<String>, Int) -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackBarHostState = remember { SnackbarHostState() }
    val scaffoldState =
        rememberScaffoldState(drawerState = drawerState, snackbarHostState = snackBarHostState)
    var showBucketPop by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it ->
        it?.let { imageUri ->
            uiState.topBarUiState?.let {
                it.onUploadFile(getFileFromSAFUri(context, imageUri)?.path ?: "")
            }
        }
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf()
    } else {
        arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
    }
    val requestPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            println("requestPermissions $result")
            if (result.all { entry -> entry.value }) {
                launcher.launch("*/*")
            }
        }


    Scaffold(topBar = {
        HomeTopBar(
            title = uiState.topBarUiState?.bucket?.name() ?: "",
            subTitle = uiState.topBarUiState?.bucket?.creationDate().toString(),
            onShowPop = {
                showBucketPop = !showBucketPop
            },
            onMenuClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            },
            onAddClick = {
                requestPermissions.launch(permissions)
            },
            topBarModel = uiState.topBarUiState?.topBarModel
        )
    }, snackbarHost = {
        SnackbarHost(hostState = snackBarHostState, snackbar = {
            Snackbar(modifier = Modifier.padding(16.dp), action = {
                IconButton(onClick = {
                    snackBarHostState.currentSnackbarData?.performAction()
                }) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                }
            }) {
                Text("")
            }
        })
    }, drawerContent = {
        Text("Drawer Content")
    }, scaffoldState = scaffoldState, content = { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (showBucketPop) {
                Popup(onDismissRequest = { showBucketPop = false }) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorBackground()),
                    ) {
                        uiState.topBarUiState?.buckets?.let { bucketList ->
                            items(bucketList) {
                                itemBucket(it)
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.baseline_home_24),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .padding(vertical = 4.dp)
                            .clickable {
                                uiState.folderPathUiState?.onHomeTabClick?.let { it() }
                            },
                        contentDescription = ""
                    )
                    uiState.folderPathUiState?.folderPaths?.map {
                        "$it >> "
                    }?.let {
                        FolderTabs(it) {
                            uiState.folderPathUiState.onFolderTabSelector(it)
                        }
                    }
                }

                LoadableLayout(modifier = Modifier.fillMaxWidth(),
                    loadableState = uiState.pagerUiState,
                    onRetryClick = {

                    },
                    emptyLayout = {}) { pageUiState ->
                    pageUiState.folderList?.let { folders ->
                        FolderPage(folders, onItemClick = { it, index ->
                            when (it.fileType) {
                                is FileType.Folder -> {
                                    pageUiState.onFolderClick(it)
                                }
                                is FileType.ImageFile -> {
                                    val imageList = folders.filter {
                                        it.fileType is FileType.ImageFile
                                    }.map {
                                        it.downloadUrl
                                    }
                                    onImageFileClick(imageList, index)
                                }
                                is FileType.TextFile -> {

                                }
                            }
                        }, onLongCLick = {
                            uiState.topBarUiState?.onFolderLongClick?.let { it(TopBarModel.DELETE) }
                        })
                    }
                }
            }
        }
    })
}


@Composable
private fun FolderTabs(folderNames: List<String>, onItemClick: (Int) -> Unit) {
    LazyRow(modifier = Modifier.padding(4.dp)) {
        itemsIndexed(folderNames) { index, item ->
            FolderTabItem(item) {
                onItemClick(index)
            }
        }
    }
}

@Composable
private fun FolderPage(
    folderNames: List<FolderItemData?>,
    onItemClick: (FolderItemData, Int) -> Unit,
    onLongCLick: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        itemsIndexed(folderNames) { index, floder ->
            floder?.let {
                FolderItem(folderName = it, onItemClick = {
                    onItemClick(it, index)
                }, onLongCLick = {
                    onLongCLick()
                })
            }
        }
    }
}

@Composable
private fun FolderTabItem(folderName: String, onItemClick: () -> Unit) {
    Text(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable {
                onItemClick()
            },
        text = folderName, style = body2, textAlign = TextAlign.Center,
    )
}


@Composable
private fun FolderItem(
    folderName: FolderItemData,
    onItemClick: (FolderItemData) -> Unit,
    onLongCLick: () -> Unit = {},
) {
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .pointerInput(onItemClick, onLongCLick) {
            detectTapGestures(onLongPress = {
                onLongCLick()
            }, onTap = {
                onItemClick(folderName)
            })
        }) {
        val (image, title, subSize, createData, line) = createRefs()
        val fileIcon = when (folderName.fileType) {
            is FileType.Folder -> {
                R.drawable.baseline_folder_24
            }
            is FileType.ImageFile -> {
                R.drawable.baseline_image_24
            }
            is FileType.TextFile -> {
                R.drawable.baseline_text_snippet_24
            }
        }

        Image(modifier = Modifier
            .constrainAs(image) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
            .size(36.dp),
            contentScale = ContentScale.Crop,
            painter = painterResource(fileIcon),
            contentDescription = null)

        Text(modifier = Modifier.constrainAs(title) {
            start.linkTo(image.end, 8.dp)
            top.linkTo(parent.top, 4.dp)
        }, text = folderName.fileType.name, style = body2)

        when (folderName.fileType) {
            is FileType.Folder -> {
                Text(modifier = Modifier.constrainAs(subSize) {
                    start.linkTo(image.end, 8.dp)
                    top.linkTo(title.bottom)
                    bottom.linkTo(parent.bottom, 4.dp)
                }, text = "${folderName.fileType.subSize} 项", style = body3)

            }
            is FileType.ImageFile -> {

                Text(modifier = Modifier.constrainAs(subSize) {
                    start.linkTo(image.end, 8.dp)
                    top.linkTo(title.bottom)
                    bottom.linkTo(parent.bottom, 4.dp)
                }, text = folderName.fileType.fileSize, style = body3)

                Text(modifier = Modifier.constrainAs(createData) {
                    bottom.linkTo(image.bottom)
                    end.linkTo(parent.end)
                }, text = folderName.fileType.lastModifyData, style = body3)
            }
            is FileType.TextFile -> {

                Text(modifier = Modifier.constrainAs(subSize) {
                    start.linkTo(image.end, 8.dp)
                    top.linkTo(title.bottom)
                    bottom.linkTo(parent.bottom, 4.dp)
                }, text = folderName.fileType.fileSize, style = body3)

                Text(modifier = Modifier.constrainAs(createData) {
                    bottom.linkTo(image.bottom)
                    end.linkTo(parent.end)
                }, text = folderName.fileType.lastModifyData, style = body3)
            }
        }

    }
}

@Composable
private fun itemBucket(it: Bucket) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "${it.name()}", style = body1, color = colorTertiary()
        )

        Text(
            text = "${it.creationDate()}", style = body3, color = colorTertiary()
        )
    }
}


