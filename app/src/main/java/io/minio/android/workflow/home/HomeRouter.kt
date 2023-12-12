package io.minio.android.workflow.home

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.minio.android.R
import io.minio.android.base.LoadableLayout
import io.minio.android.base.ui.theme.*
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.workflow.IMAGE_PRE_PAGE
import io.minio.messages.Bucket
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun HomeRouter(viewModel: HomeViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomePage(uiState, onImageFileClick = { images, index ->
        navController.navigate("$IMAGE_PRE_PAGE\$?images=${images.joinToString(",")}&selectorIndex=$index")
    })
}

fun getFileFromSAFUri(context: Context, uri: Uri): File? {
    val contentResolver = context.contentResolver
    val cacheDir: File = context.cacheDir
    val cursor = contentResolver.query(uri, null, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            if (displayNameIndex != -1) {
                val fileName = it.getString(displayNameIndex)
                val destinationFile = File(cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        val buffer = ByteArray(4 * 1024)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                    }
                }
                return destinationFile
            }
        }
    }
    return null
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
        HomeTopBar(title = uiState.topBarUiState?.bucket?.name() ?: "",
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
            })
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
                        FolderPage(folders) { it, index ->
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
                        }
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
    folderNames: List<FolderItemData?>, onItemClick: (FolderItemData, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        itemsIndexed(folderNames) { index, floder ->
            floder?.let {
                FolderItem(it) {
                    onItemClick(it, index)
                }
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
            }, text = folderName, style = body2, textAlign = TextAlign.Center,
    )
}


@Composable
private fun FolderItem(folderName: FolderItemData, onItemClick: (FolderItemData) -> Unit) {
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .clickable {
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

@Preview
@Composable
private fun preFolderItem() {
//    FolderItem(FolderItemData("你好", 0))
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


@Composable
fun HomeTopBar(
    title: String,
    subTitle: String,
    onShowPop: () -> Unit,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit
) {
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
            modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
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
            onAddClick()
        }) {
            Icon(Icons.Default.Add, tint = colorSecondary(), contentDescription = null)
        }
    }
}
