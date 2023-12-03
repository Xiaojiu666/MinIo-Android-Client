package io.minio.android.workflow.home

import android.annotation.SuppressLint
import android.media.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.minio.android.R
import io.minio.android.base.LoadingWrapper
import io.minio.android.base.UiStateWrapper
import io.minio.android.base.ui.theme.*
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.messages.Bucket
import kotlinx.coroutines.launch


@Composable
fun HomeRouter(viewModel: HomeViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomePage(uiState)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage(uiState: UiStateWrapper) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackBarHostState = remember { SnackbarHostState() }
    val scaffoldState =
        rememberScaffoldState(drawerState = drawerState, snackbarHostState = snackBarHostState)
    var showBucketPop by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var bucket by remember {
        mutableStateOf(Bucket())
    }

    Scaffold(topBar = {
        HomeTopBar(
            bucket.name() ?: "",
            bucket.creationDate().toString(),
            onShowPop = {
                showBucketPop = !showBucketPop
            }, onMenuClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            })
    }, snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState,
            snackbar = {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        // Snackbar 操作按钮
                        IconButton(onClick = {
                            snackBarHostState.currentSnackbarData?.performAction()
                        }) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                        }
                    }
                ) {
                    Text("")
                }
            })
    }, drawerContent = {
        Text("Drawer Content")
    }, scaffoldState = scaffoldState, content = {
        uiState.LoadingWrapper<HomeViewModel.HomeUiState>(content = { uiState ->
            LaunchedEffect(uiState.snackBarHostMsg) {
                scaffoldState.snackbarHostState.showSnackbar(uiState.snackBarHostMsg)
            }


            uiState.selectorBucket?.let {
                bucket = it
            }
            Box(modifier = Modifier.padding(it)) {
                if (showBucketPop) {
                    Popup(
                        onDismissRequest = { showBucketPop = false }
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorBackground()),
                        ) {
                            items(uiState.buckets) {
                                itemBucket(it)
                            }
                        }
                    }
                }
                val pagerState = rememberPagerState()

                Column {
                    FolderTabs(uiState.foldPage.map { it.folderTitle }) {
                        coroutineScope.launch {
                            pagerState.scrollToPage(it)
                        }
                    }
                    HorizontalPager(
                        pageCount = uiState.foldPage.size,
                        state = pagerState,
                        userScrollEnabled = false
                    ) { currentPage ->
                        FolderPage(uiState.foldPage[currentPage].folderPageFolderList) {
                            uiState.onFolderSelector(it)
                        }
                    }
                }
            }
        }) {

        }
    })
}

@Composable
private fun FolderTabs(folderNames: List<String>, onItemClick: (Int) -> Unit) {
    LazyRow(modifier = Modifier.padding(10.dp)) {
        itemsIndexed(folderNames) { index, item ->
            FolderTabItem(item) {
                onItemClick(index)
            }
        }
    }
}

@Composable
private fun FolderPage(folderNames: List<FolderItemData?>, onItemClick: (FolderItemData) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        items(folderNames) {
            it?.let {
                FolderItem(it) {
                    onItemClick(it)
                }
            }
        }
    }
}

@Composable
private fun FolderTabItem(folderName: String, onItemClick: () -> Unit) {
    Text(modifier = Modifier.padding(4.dp).clickable {
        onItemClick()
    }, text = folderName, style = body2)
}


@Composable
private fun FolderItem(folderName: FolderItemData, onItemClick: (FolderItemData) -> Unit) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth().clickable {
        onItemClick(folderName)
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

        Image(
            modifier = Modifier.constrainAs(image) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }.size(36.dp),
            contentScale = ContentScale.Crop,
            painter = painterResource(fileIcon),
            contentDescription = null
        )

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

@Preview
@Composable
private fun preFolderItem() {
//    FolderItem(FolderItemData("你好", 0))
}

@Composable
private fun itemBucket(it: Bucket) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "${it.name()}",
            style = body1,
            color = colorTertiary()
        )

        Text(
            text = "${it.creationDate()}",
            style = body3,
            color = colorTertiary()
        )
    }
}


@Composable
fun HomeTopBar(title: String, subTitle: String, onShowPop: () -> Unit, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorPrimary()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = {
            onMenuClick()
        }) {
            Icon(Icons.Default.Menu, tint = colorSecondary(), contentDescription = null)
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column() {
                Text(text = title, style = body1, maxLines = 1, color = colorSecondary())

                Text(text = subTitle, style = body3, maxLines = 1, color = colorSecondary())
            }
            IconButton(modifier = Modifier.padding(8.dp), onClick = {
                onShowPop()
            }) {
                Icon(
                    Icons.Default.ArrowDropDown, tint = colorSecondary(), contentDescription = null
                )
            }
        }

        IconButton(modifier = Modifier.padding(horizontal = 8.dp), onClick = {

        }) {
            Icon(Icons.Default.Settings, tint = colorSecondary(), contentDescription = null)
        }
    }
}
